package main;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import GUI.ChatListener;
import networking.MessageSender;
import networking.NetworkListener;
import networking.Phonebook;
import storage.FileSyncManager;
import util.MessageUtils;

public class Raft implements Runnable, NetworkListener, ChatListener {

	// Raft specific
	private int role; // Roles: 0 - Follower | 1 - Candidate | 2 - Leader
	private int term; // Term Counter of the current Raft NEtwork of this Node
	private int cycle; // Number of Election Timeouts within this node
	private boolean didVote = false;
	private Client lastVote;
	private Phonebook phonebook = new Phonebook();
	private Client thisClient;
	private boolean twoLeaders;
	private long lastHeartbeat;
	// Raft specific Aggregation Functions
	private int votes;
	private int idCounter = 1;
	// Raft Timer
	Timer electionTimeout = new Timer("raftCycle-0");
	private boolean debug = false;

	TimerTask raftCycleManager = new TimerTask() {
		public void run() {
			if (lastHeartbeat < (System.currentTimeMillis() - 300)) {
				if (debug)
					System.out.println("started TimerTask");

				if (role == 0) {
					becomeCandidate();
				}
				// Multiple Heartbeats in one elevtionTimeout==> if Timer is over there is a
				// problem with the Leader and a new needs to be elected

			}
		}

	};
	// Leader Timer (HeartBeat & Tasklist)
	Timer heartbeatTimer = new Timer("Heartbeat");
	TimerTask heartbeat = new TimerTask() {
		public void run() {
			// System.out.println("Heartbeat"+thisClient.getIp()+":"+thisClient.getPort());
			if (role != 2) {
				heartbeatTimer.cancel();
			}
			// task list is accessed with every new Message manageTasklist();
			heartbeat();
			// Multiple Heartbeats in one elevtionTimeout ==> if Timer is over there is a
			// problem with the Leader and a new needs to be elected

		}
	};
	Queue<Message> q = new LinkedList<Message>(); // Store all Messages that need to be send out from Leader with
													// Hearthbeat
	private ArrayList<ChatMessage> messageCache = new ArrayList<ChatMessage>();
	private Map<Integer, Integer> messageResponseAggregator = new HashMap<Integer, Integer>();
	ArrayList<AwaitingResponse> taskList = new ArrayList<AwaitingResponse>(); // holds all response leader is waiting
	private boolean newMessage; // for being able to only process on message at a time
	// Utilities
	private MessageSender sender;
	private FileSyncManager fileWriter;

	// ##############################################################
	//
	// --------------- MAIN RAFT IMPLEMENTAION ----------------------------
	//
	// #############################################################
	public Raft(Client me) {
		sender = new MessageSender();
		thisClient = me;
		newMessage = true;
		twoLeaders = false;
		lastHeartbeat = 0;
		term = 0;
	}

	// EMPTY
	public void initialJoin() {

	}

	@Override
	public void run() {
		// Phonebook needs me already as an entry
		// I need to start recieving Heartbeats from Leader right away
		// needs to be already in phonbook of leader
		votes = 0;
		electionTimeout.scheduleAtFixedRate(raftCycleManager, 200, votingCycle()); // Initial Start of Raft Cycle
		if (debug)
			System.out.println("Started Raft");
	}

	// ##############################################################
	//
	// --------------- Log Replication ----------------------------
	//
	// #############################################################

	public void newMessageForwardedToLeader(Message msg) {
		ChatMessage cMessage = ChatMessage.chatMessageStringToObject(msg.getPayload());
		if (debug) {
			System.out.println(msg.getPayload() + "in MessageForward");
		}
		cMessage.setId(idCounter);
		idCounter++;
		int id = cMessage.getId();
		int idPipeline;
		boolean pipelineEmpty;
		if (!messageCache.isEmpty()) {
			idPipeline = messageCache.get(0).getId();
			pipelineEmpty = false;
		} else {
			pipelineEmpty = true;
			idPipeline = 0;
		}
		if (role == 2 && newMessage && !pipelineEmpty && id < idPipeline && !twoLeaders
				|| role == 2 && newMessage && pipelineEmpty && !twoLeaders) {
			// ####################################################
			if (Phonebook.countPhonebookEntries() == 1) {
				newMessage = false;
				FileSyncManager.addMessage(ChatMessage.chatMessageStringToObject(msg.getPayload()));
				FileSyncManager.save(thisClient.getIp() + "-" + thisClient.getPort());
				newMessage = true;
				// #######################################################################################
			} else {
				newMessage = false;
				messageCache.add(ChatMessage.chatMessageStringToObject(msg.getPayload()));
				String payload2 = msg.getPayload();
				messageResponseAggregator.put(id, 1); // Adds new Key-value pair to the map that checks how
														// many responses for a message have arrived
				Message cacheMessage2 = new Message(thisClient, payload2, MessageType.NewMessageToCache);
				q.offer(cacheMessage2);
				// sender.broadcastMessage(cacheMessage);
				AwaitingResponse newTask = new AwaitingResponse(thisClient, MessageType.MessageCached);
				newTask.setComparePayloads(msg.getPayload());
				addBroadcastResponseTask(newTask);
			}
		} else if (role == 2 && !newMessage) {
			messageCache.add(ChatMessage.chatMessageStringToObject(msg.getPayload()));
			String payload = msg.getPayload();
			ChatMessage extractId = ChatMessage.chatMessageStringToObject(payload);
			messageResponseAggregator.put(extractId.getId(), 1);
		} else if (role == 2 && newMessage && !pipelineEmpty && id > idPipeline && !twoLeaders) {
			newMessage = false;
			String payload = msg.getPayload();
			messageCache.add(ChatMessage.chatMessageStringToObject(msg.getPayload()));
			ChatMessage extractId = ChatMessage.chatMessageStringToObject(msg.getPayload());
			Message cacheMessage = new Message(thisClient, payload, MessageType.NewMessageToCache);
			q.offer(cacheMessage);
			// sender.broadcastMessage(cacheMessage);
			AwaitingResponse newTask = new AwaitingResponse(thisClient, MessageType.MessageCached);
			newTask.setComparePayloads(msg.getPayload());
			addBroadcastResponseTask(newTask);

		}
	}

	public void checkIfMessageInPipline() {
		if (newMessage && !messageCache.isEmpty() && !twoLeaders) {
			newMessage = false;
			ChatMessage payload = messageCache.get(0);
			Message cacheMessage = new Message(thisClient, ChatMessage.chatMessageObjectToString(payload),
					MessageType.NewMessageToCache);
			q.offer(cacheMessage);
			// sender.broadcastMessage(cacheMessage);
			AwaitingResponse newTask = new AwaitingResponse(thisClient, MessageType.MessageCached);
			newTask.setComparePayloads(ChatMessage.chatMessageObjectToString(payload));
			addBroadcastResponseTask(newTask);
		}
	}

	public boolean findTask(AwaitingResponse r) {
		for (AwaitingResponse a : taskList) {
			// TODO: FIX THE OR to an AND
			if (a.getComparePayloads().equals(r.getComparePayloads())
					|| a.getType() == r.getType() && a.getResponder().getIp().equals(r.getResponder().getIp())
							&& r.getResponder().getPort() == a.getResponder().getPort()) {
				return true;
			}
		}
		return false;

	}

	public void gatherCacheResponses(Message msg) {
		AwaitingResponse cmp = new AwaitingResponse(msg.getSenderAsClient(), msg.getType());
		cmp.setComparePayloads(msg.getPayload());
//		System.out.println(cmp.getType()+msg.getPayload());

		if (findTask(cmp)) {
			findAndDeleteTask(msg.getSenderAsClient(), msg.getType(), msg.getPayload());
			int msgId = ChatMessage.chatMessageStringToObject(msg.getPayload()).getId();
			if (messageResponseAggregator.containsKey(msgId)) {
				messageResponseAggregator.put(msgId, messageResponseAggregator.get(msgId) + 1);
				if (messageResponseAggregator.get(msgId) > (Phonebook.countPhonebookEntries() / 2)) {
					Message writeMessage = new Message(thisClient, msg.getPayload(), MessageType.WriteMessage);
					AwaitingResponse newTask = new AwaitingResponse(thisClient, MessageType.MessageWritten);
					newTask.setComparePayloads(msg.getPayload());
					FileSyncManager.addMessage(ChatMessage.chatMessageStringToObject(msg.getPayload()));
					FileSyncManager.save(thisClient.getIp() + "-" + thisClient.getPort());
					Iterator<ChatMessage> it = messageCache.iterator();
					while(it.hasNext()) {
						ChatMessage m = it.next();
						if (m.getId() == ChatMessage.chatMessageStringToObject(msg.getPayload()).getId()) {
							it.remove();
						}
					}
					addBroadcastResponseTask(newTask);
					q.offer(writeMessage);
					newMessage = true;
				}
			}
		} else {
			if (debug) {
				System.out.println("##################");
				System.out.println("Recieved Message But not in TaskList");
				System.out.println("##################");
			}

		}
	}

	public void gatherWriteResponses(Message msg) {
		AwaitingResponse cmp = new AwaitingResponse(msg.getSenderAsClient(), msg.getType());
		cmp.setComparePayloads(msg.getPayload());
		if (taskList.contains(cmp)) {
			findAndDeleteTask(msg.getSenderAsClient(), msg.getType(), msg.getPayload());
		}
	}

	public void cacheTheMessage(Message msg) {
		messageCache.add(ChatMessage.chatMessageStringToObject(msg.getPayload()));
		Message response = new Message(thisClient, msg.getPayload(), MessageType.MessageCached);
		sender.sendMessageAutoRetry(response, Phonebook.getLeader(), 20, "could not send to LEader");
	}

	public void writeTheMesssage(Message msg) {
		ChatMessage extract = ChatMessage.chatMessageStringToObject(msg.getPayload());
		idCounter = extract.getId() + 1;
		FileSyncManager.addMessage(extract);
		Iterator<ChatMessage> chdMsgitr = messageCache.iterator();
		while (chdMsgitr.hasNext()) {
			ChatMessage m = chdMsgitr.next();
			if (m.getId() == extract.getId()) {
				messageCache.remove(m);
			}

		}
		FileSyncManager.save(thisClient.getIp() + "-" + thisClient.getPort());
		Message response = new Message(thisClient, msg.getPayload(), MessageType.MessageWritten);
		try {
			sender.sendMessageAutoRetry(response, Phonebook.getLeader(), 5,
					"Did not Confirm message written to leader");
		} catch (Exception e) {

		}
	}

	// ##############################################################
	//
	// --------------- Voting ----------------------------
	//
	// #############################################################
	public void hearthbeatResetElectionTimout() {
		restartElectionTimeout();
	}

	public void syncRoleWithPhonebook() {
		Phonebook.setRights(thisClient, role);
	}

	public void restartElectionTimeout() {
		stopElectionTimeout();
		TimerTask raftCycleReset = new TimerTask() {
			public void run() {
				if (lastHeartbeat < (System.currentTimeMillis() - 300)) {
					if (debug)
						System.out.println("Executing TimerTask as Role:" + role);

					if (role != 2) {
						// resetVote();

						becomeCandidate();
					}

				}
			}
		};
		electionTimeout = new Timer("Raftcycle-" + cycle);
		// System.out.println("TimerReseted" + "Raftcycle-" + cycle);
		electionTimeout.scheduleAtFixedRate(raftCycleReset, 0, votingCycle());
		raftCycleManager = raftCycleReset;
	}

	public void stopElectionTimeout() {
		electionTimeout.cancel();
		electionTimeout.purge();
		cycle = cycle + 1;

	}

	public void voteLeader(Message msg) {
		if (debug)
			System.out.println(Integer.parseInt(msg.getPayload(), 10) + ">" + term + " "
					+ (Integer.parseInt(msg.getPayload(), 10) > term));
		if (Integer.parseInt(msg.getPayload(), 10) > term) {
			restartElectionTimeout();
			if (debug)
				System.out.println("I Voted for somebody");
			resetVote();
			didVote = true;
			term = Integer.parseInt(msg.getPayload(), 10);
			lastVote = msg.getSenderAsClient();
			Message ballot = new Message(thisClient, msg.getPayload(), MessageType.Vote);
			sender.sendMessageAutoRetry(ballot, msg.getSenderAsClient(), 10, "Vote failed");
		} else {
//			Message m=new Message(thisClient,term+"",MessageType.AlreadyVoted);
//			try {
//				sender.sendMessage(m, msg.getSenderAsClient());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			if (debug)
				System.out.println("restarting ELT");
			restartElectionTimeout();
		}
	}

	public void resetVote() {
		role = 0;
		syncRoleWithPhonebook();
		votes = 0;
		didVote = false;
	}

	private long votingCycle() {
		int random = (ThreadLocalRandom.current().nextInt(0, 150) + 150);
		return Long.valueOf(random);
	}

	private void leaderStepDown(Client c) {
		if (debug)
			System.out.println("Delected Leader");
		role = 0;
		syncRoleWithPhonebook();
		Phonebook.newLeader(c);
		taskList.clear();
		heartbeatTimer.cancel();
		restartElectionTimeout();

	}
	// ##############################################################
	//
	// --------------- Leader Capabilities ----------------------------
	//
	// #############################################################

	private void manageTasklist() {

	}

	private void twoLeaders(Client c) {
		Message konter = new Message(thisClient,
				"1" + "-" + term + "-" + thisClient.getIp() + "-" + thisClient.getPort(),
				MessageType.ResolveTwoLeaders);
		sender.sendMessageAutoRetry(konter, c, 10, "Konter failed");
	}

	private void resolveTwoLeaders(Message msg) {
		int msglvl = Integer.parseInt(msg.getPayload().substring(0, 2));
		twoLeaders = true;
		switch (msglvl) {
		case 1:

			if (Integer.parseInt(msg.getPayload().split(":")[1], 10) < term) {
				Message konter = new Message(thisClient,
						"2" + ":" + term + ":" + thisClient.getIp() + ":" + thisClient.getPort(),
						MessageType.ResolveTwoLeaders);
				sender.sendMessageAutoRetry(konter, msg.getSenderAsClient(), 10, "Step down request failed ");
			} else if (Integer.parseInt(msg.getPayload().split(":")[1], 10) >= term) {
				Message konter = new Message(thisClient, "4" + Phonebook.exportPhonebook(),
						MessageType.ResolveTwoLeaders);
				sender.sendMessageAutoRetry(konter, msg.getSenderAsClient(), 10, "Stepping down notification failed");
				leaderStepDown(msg.getSenderAsClient());

			}
			break;
		case 2:
			leaderStepDown(msg.getSenderAsClient());
			Message konter = new Message(thisClient, "4" + Phonebook.exportPhonebook(), MessageType.ResolveTwoLeaders);
			sender.sendMessageAutoRetry(konter, msg.getSenderAsClient(), 10, "Phonebook could not me shared");
			break;
//		case 3:
//			Message konter = new Message(thisClient, "4"+Phonebook.exportPhonebook(),
//					MessageType.ResolveTwoLeaders);
//			sender.sendMessage(konter, msg.getSenderAsClient());
//			break;
		case 4:
			if (debug)
				System.out.println("MErging two leaders");
			Phonebook.importPhonebook(msg.getPayload().substring(1, msg.getPayload().length()));
			Message IAmTheSenate = new Message(thisClient,
					term + "-" + this.thisClient.getIp() + "-" + thisClient.getPort() + "-" + "Leader",
					MessageType.IAmTheSenat);
			sender.broadcastMessage(IAmTheSenate, true,
					"One client did not approve of my leadership (not received). Execute order 66.");
			Message newBroadcastSyncPhonebook = new Message(thisClient, Phonebook.exportPhonebook(),
					MessageType.NewClientInPhonebookSyncronizeWithAllClients);
			q.offer(newBroadcastSyncPhonebook);
			// TODO: MAYBE Clear Cache of stepping Down
			// TODO: MAYBE send full file to every node
			// TODO : MAYBE send Cache to every Node
			twoLeaders = false;
			break;

		default:

			break;

		}
	}

	private void addTask(AwaitingResponse task) {
		taskList.add(task);
	}

	private void addBroadcastResponseTask(AwaitingResponse task) {
		for (Client c : phonebook.getFullPhonebook()) {
			if (c.getPort() != thisClient.getPort()
					|| c.getIp() != thisClient.getIp() && c.getPort() == thisClient.getPort()) {
				task.setResponder(c);
				addTask(task);
			}

		}
	}

	private void findAndDeleteTask(Client clnt, MessageType type) {
		Iterator<AwaitingResponse> itrTaskList = taskList.iterator();
		while (itrTaskList.hasNext()) {
			AwaitingResponse task = itrTaskList.next();
			System.out.println(
					task.getResponder().getIp().equals(clnt.getIp()) + clnt.getIp() + task.getResponder().getIp());
			if (task.getResponder().getIp().equals(clnt.getIp()) && task.getResponder().getPort() == (clnt.getPort())
					&& task.getType() == type) {
				itrTaskList.remove();
			} else {
				if (debug)
					System.out.println("ERROR- No Task like that");
				try {
					Thread.sleep(100000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}
	}

	private void findAndDeleteTask(Client clnt, MessageType type, String payload) {
		Iterator<AwaitingResponse> itrTaskList = taskList.iterator();
		while (itrTaskList.hasNext()) {
			AwaitingResponse task = itrTaskList.next();
			System.out.println(
					task.getResponder().getIp().equals(clnt.getIp()) + clnt.getIp() + task.getResponder().getIp());
			if (task.getResponder().getIp().equals(clnt.getIp()) && task.getResponder().getPort() == (clnt.getPort())
					&& task.getType() == type) {
				itrTaskList.remove();
			} else {
				if (debug)
					System.out.println("ERROR- No Task like that");
				try {
					Thread.sleep(100000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}
	}

	private void heartbeat() {
		checkIfMessageInPipline();
		lastHeartbeat = System.currentTimeMillis();
		if (q.isEmpty() || twoLeaders) {
			sendNormalHeartbeat();
		} else {
			Message nextMessage = q.poll();
			boolean broadcast = true;
			Client reciepient = null;
			switch (nextMessage.getType()) {
			case Heartbeat:
				broadcast = true;
				break;
//			case MessageCached:
//				break;
			case NewClientInPhonebookSyncronizeWithAllClients:
				broadcast = true;
				break;
//			case NewMessageForwardedToLeader:
//				break;
			case NewMessageToCache:
				broadcast = true;
				break;
			case WriteMessage:
				broadcast = true;
				break;
//			case ReadyForRaft:
//
//				break;
//			case RequestActiveClientsListFromAnotherNode:
//				//unused
//				break;
//			case RequestFullMessageHistoryFromAnotherNode:
//				//unused
//				break;
//			case RequestVoteForMe:
//				 payload = payload that is returned with VOTE
//				break;
//			case Vote:
//				break;
//			case WannaJoin:
//				break;
//			case WhichPort:
//				break;
////			case AlreadyVoted:
////			// Do nothing
////			// delete TAsk
//			break;
//			case HeartbeatResponse:
//				break;
			default:
				break;

			}
			if (debug)
				System.out.println("send new Message" + nextMessage.getType());
			if (broadcast) {
				sender.broadcastMessage(nextMessage, true, "Heartbeat could not be sent to a client");
			} else {
				try {
					sender.sendMessage(nextMessage, reciepient);
				} catch (Exception e) {
					q.offer(nextMessage);
					if (debug)
						System.out.println("[RAFT] Heartbeat could not be sent");
				}
			}
		}
	}

	public void sendNormalHeartbeat() {
		String payload = thisClient.getIp() + "-" + thisClient.getPort() + "-" + term;
		Message heartbeat = new Message(thisClient, payload, MessageType.Heartbeat);
		sender.broadcastMessage(heartbeat, false, "");
	}
	// ##############################################################
	//
	// --------------- Follower Capabilities ----------------------------
	//
	// #############################################################

	// after Recieving a I am Your Leader MEssage
	public void newLeaderChosen(Client clnt) {
		Phonebook.newLeader(clnt);
		if (role != 0) {
			role = 0;
			syncRoleWithPhonebook();
		}
	}

	private void becomeCandidate() {
		if (debug) {
			System.out.println("President Elect");
		}
		resetVote();
		role = 1;
		syncRoleWithPhonebook();
		didVote = true;
		term = term + 1;
		votes = 1;
//		System.out.println("Voted for ME");
		checkVote();
		if (role != 2) {
			Message voteForMeMessage = new Message(thisClient, term + "", MessageType.RequestVoteForMe);
			sender.broadcastMessage(voteForMeMessage, false, "VoteForMe could not be broadcasted to all");
			// restartElectionTimeout();
		}

	}

	private void checkVote() {
		if (debug)
			System.out.println("Check if Majority Term:" + term);
		// System.out.println(Phonebook.exportPhonebook());
		if (Phonebook.countPhonebookEntries() == 0) {
			System.out.println("ERROR- Empty phonebook");
		} else {
			if (votes > (Phonebook.countPhonebookEntries() / 2)) {
				if (debug)
					System.out.println("Majority Reached");
				becomeLeader();
			}
			if (debug)
				System.out.println(
						votes + "(votes) VS (nodes for Majority)" + (Phonebook.countPhonebookEntries() / 2) + 1);
		}

	}

	private void becomeLeader() {
		if (debug)
			System.out.println("Elected Leader");
		role = 2;
		raftCycleManager.cancel();

		taskList.clear();
		stopElectionTimeout();

		Phonebook.newLeader(thisClient);
		heartbeatTimer = new Timer("Heartbeat-" + term);
		heartbeatTimer.scheduleAtFixedRate(heartbeat, 2, 35);
		Message IAmTheSenate = new Message(thisClient,
				term + "-" + thisClient.getIp() + "-" + thisClient.getPort() + "-" + "Leader", MessageType.IAmTheSenat);
		sender.broadcastMessage(IAmTheSenate, true, "Could not tell this client that I am senate");

	}

	public void stopHeartbeat() {
		heartbeatTimer.cancel();
	}

	public void gatherHeartbeatResponse(Message m) {

	}
	// ##############################################################
	//
	// --------------- Message Management ----------------------------
	//
	// #############################################################

	@Override
	public void onMessageReceived(Message message, PrintWriter response) {
		if (message.getType() != MessageType.Heartbeat) {
			if (debug) {
				System.out.println("New Message -----------------------------");
				MessageUtils.printMessage(message);
			}
		}
		switch (message.getType()) {
		case TakeHistory:
			FileSyncManager.saveFromString(message.getPayload());
			break;
		case TakeIdCounter:
			idCounter = Integer.parseInt(message.getPayload());
			break;

		case AlreadyVoted:
			if (debug)
				System.out.println(message.getPayload() + "vs mine:" + term);
			break;
		case Heartbeat:
			if (role != 2) {
				lastHeartbeat = System.currentTimeMillis();
				// System.out.println("restart ELT" + lastHeartbeat);
				restartElectionTimeout();
				role = 0;
				syncRoleWithPhonebook();
			}
			if (role == 2) {
				// if event is triggered do not trigger again
				if (!twoLeaders) {
					twoLeaders(message.getSenderAsClient());
				}
			}
			break;
		case MessageCached:
			if (role == 2) {
				gatherCacheResponses(message);
				if (!twoLeaders) {
					twoLeaders(message.getSenderAsClient());
				}
			} else {

			}
			break;
		case NewMessageForwardedToLeader:
			if (role == 2) {
				newMessageForwardedToLeader(message);
			} else {
				sender.sendMessageAutoRetry(message, Phonebook.getLeader(), 10,
						"Message could not be forwarded to leader");
			}
			break;
		case NewMessageToCache:
			if (role == 2) {

			} else {
				cacheTheMessage(message);
			}
			break;
		case RequestVoteForMe:
			if (role == 2) {
				Message hb = new Message(thisClient,
						term + "-" + this.thisClient.getIp() + "-" + thisClient.getPort() + "-" + "Leader",
						MessageType.Heartbeat);
				try {
					sender.sendMessage(hb, message.getSenderAsClient());
				} catch (Exception e) {
					System.out.println("[RAFT] 'I am already leader' message failed");
				}
			} else {
				if (debug)
					System.out.println("Iwanna vote");
				raftCycleManager.cancel();
				voteLeader(message);
				role = 0;
				syncRoleWithPhonebook();
				restartElectionTimeout();

			}
			break;
		case Vote:
			if (debug)
				System.out.println("recieved Vote");
			votes++;
			if (role == 1) {
				checkVote();
			}
			break;
		case WriteMessage:
			if (role == 2) {
				if (!twoLeaders) {
					twoLeaders(message.getSenderAsClient());
				}
			} else {
				writeTheMesssage(message);
			}
			break;
		case IAmTheSenat:
			// extract client from Message
			lastHeartbeat = System.currentTimeMillis();
			if (debug)
				System.out.println("Got New Leader");
			if (role == 2) {
				if (!twoLeaders) {
					twoLeaders(message.getSenderAsClient());
				}

			} else {
				newLeaderChosen(message.getSenderAsClient());

				role = 0;
				syncRoleWithPhonebook();
			}
			break;
		case ReadyForRaft:
			if (role == 2) {
				Message newBroadcastSyncPhonebook = new Message(thisClient, Phonebook.exportPhonebook(),
						MessageType.NewClientInPhonebookSyncronizeWithAllClients);
				q.offer(newBroadcastSyncPhonebook);

				Message idCounterMsg = new Message(thisClient, "" + idCounter, MessageType.TakeIdCounter);
				sender.sendMessageAutoRetry(idCounterMsg, message.getSenderAsClient(), 10,
						"could not deliver idCounter");

				Message historyMsg = new Message(thisClient, FileSyncManager.exportHistory(), MessageType.TakeHistory);
				sender.sendMessageAutoRetry(historyMsg, message.getSenderAsClient(), 10, "could not deliver history");
			} else {
				Phonebook.importPhonebook(message.getPayload());
			}
			break;
		case HeartbeatResponse:
			gatherHeartbeatResponse(message);
			break;
		case MessageWritten:
			if (role == 2) {
				gatherWriteResponses(message);
			}
			break;
		case NewClientInPhonebookSyncronizeWithAllClients:
			if (role == 2) {
				if (!twoLeaders) {
					twoLeaders(message.getSenderAsClient());
				}
			} else {
				if (debug)
					System.out.println("Importing Phonebook");
				Phonebook.importPhonebook(message.getPayload());
			}
			break;
//		case RequestActiveClientsListFromAnotherNode:
//			break;
		case RequestFullMessageHistoryFromAnotherNode:
			if (role == 2) {

			} else {
				// TODO: BENGIN wollen wir einen ganzen Chache Transfer ?ben�tige dann
				// FileSyncMessanger import / export functions
			}
			break;
		case ResolveTwoLeaders:
			if (role == 2) {
				resolveTwoLeaders(message);
			}
			break;
//		case WannaJoin:
//			break;
//		case WhichPort:
//			break;
		default:
			break;
		}
	}

	@Override
	public boolean onMessageSend(String message) {
		// TODO implement what happens when a message should be sent
		if (role == 2) {
			ChatMessage msg = new ChatMessage(0, message, thisClient.getIp() + "-" + thisClient.getPort(),
					System.currentTimeMillis());
			String payload = ChatMessage.chatMessageObjectToString(msg);
			if (debug) {
				System.out.println(payload);
			}
			Message m = new Message(thisClient, payload, MessageType.NewMessageForwardedToLeader);
			newMessageForwardedToLeader(m);
		} else {
			ChatMessage msg = new ChatMessage(0, message, thisClient.getIp() + "-" + thisClient.getPort(),
					System.currentTimeMillis());
			String payload = ChatMessage.chatMessageObjectToString(msg);
			if (debug) {
				System.out.println(payload);
			}
			sender.sendMessageAutoRetry(new Message(thisClient, payload, MessageType.NewMessageForwardedToLeader),
					Phonebook.getLeader(), 10, "Couldn't forward message");
		}
		return false;
	}

	@Override
	public void onWindowClose() {
		// TODO implement wha thappens when the window is to be closed

	}

}
