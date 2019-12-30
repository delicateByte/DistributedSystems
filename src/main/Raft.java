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

import networking.MessageSender;
import networking.NetworkListener;
import networking.Phonebook;
import storage.FileSyncManager;


public class Raft implements Runnable, NetworkListener {

	// Raft specific
	private int role; // Roles: 0 - Follower | 1 - Candidate | 2 - Leader
	private int term; // Term Counter of the current Raft NEtwork of this Node
	private int cycle; // Number of Election Timeouts within this node
	private boolean didVote = false;
	private Client lastVote;
	private Phonebook phonebook = new Phonebook();
	private Client thisClient;
	private boolean twoLeaders;
	// Raft specific Aggregation Functions
	private int votes;
	// Raft Timer
	Timer electionTimeout = new Timer("raftCycle-0");
	TimerTask raftCycleManager = new TimerTask() {
		public void run() {
			System.out.println("started TimerTask");
			try {
				Thread.sleep(Long.valueOf(ThreadLocalRandom.current().nextInt(1, 10)));
			} catch (Exception e) {

			}
			if (role == 0) {
				resetVote();
				becomeCanidate();
			}
			// Multiple Heartbeats in one elevtionTimeout==> if Timer is over there is a
			// problem with the Leader and a new needs to be elected
		}

	};
	// Leader Timer (HeartBeat & Tasklist)
	Timer heartbeatTimer = new Timer("Heartbeat");
	TimerTask heartbeat = new TimerTask() {
		public void run() {
			//System.out.println("Heartbeat"+thisClient.getIp()+":"+thisClient.getPort());
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
		electionTimeout.scheduleAtFixedRate(raftCycleManager, 2, votingCycle()); // Initial Start of Raft Cycle
		System.out.println("Started Raft");
	}

	// ##############################################################
	//
	// --------------- Log Replication ----------------------------
	//
	// #############################################################

	public void newMessageForwardedToLeader(Message msg) {
		int id = ChatMessage.chatMessageStringToObject(msg.getPayload()).getId();
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
			newMessage = false;
			messageCache.add(ChatMessage.chatMessageStringToObject(msg.getPayload()));
			String payload = msg.getPayload();
			ChatMessage extractId = ChatMessage.chatMessageStringToObject(msg.getPayload());
			messageResponseAggregator.put(extractId.getId(), 0); // Adds new Key-value pair to the map that checks how
																	// many responses for a message have arrived
			Message cacheMessage = new Message(thisClient, payload, MessageType.NewMessageToCache);
			q.offer(cacheMessage);
			// sender.broadcastMessage(cacheMessage);
			AwaitingResponse newTask = new AwaitingResponse(thisClient, MessageType.MessageCached);
			newTask.setComparePayloads(msg.getPayload());
			addBroadcastResponseTask(newTask);
		} else if (role == 2 && !newMessage) {
			messageCache.add(ChatMessage.chatMessageStringToObject(msg.getPayload()));
			String payload = msg.getPayload();
			ChatMessage extractId = ChatMessage.chatMessageStringToObject(payload);
			messageResponseAggregator.put(extractId.getId(), 0);
		} else if (role == 2 && newMessage && !pipelineEmpty && id > idPipeline && !twoLeaders) {
			newMessage = false;
			String payload = msg.getPayload();
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

	public void gatherCacheResponses(Message msg) {
		AwaitingResponse cmp = new AwaitingResponse(msg.getSenderAsClient(), msg.getType());
		cmp.setComparePayloads(msg.getPayload());
		if (taskList.contains(cmp)) {
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
					for (ChatMessage m : messageCache) {
						if (m.getId() == ChatMessage.chatMessageStringToObject(msg.getPayload()).getId()) {
							messageCache.remove(m);
						}
					}
					addBroadcastResponseTask(newTask);
					q.offer(writeMessage);
					newMessage = true;
				}
			}
		}else {
			System.out.println("##################");
			System.out.println("Recieved Message But not in TaskList");
			System.out.println("##################");

		}
	}

	public void gatherWriteResponses(Message msg) {

	}

	public void cacheTheMessage(Message msg) {
		messageCache.add(ChatMessage.chatMessageStringToObject(msg.getPayload()));
		Message response = new Message(thisClient, msg.getPayload(), MessageType.MessageCached);
		sender.sendMessage(response, Phonebook.getLeader());
	}

	public void writeTheMesssage(Message msg) {
		ChatMessage extract = ChatMessage.chatMessageStringToObject(msg.getPayload());
		FileSyncManager.addMessage(extract);
		for (ChatMessage m : messageCache) {
			if (m.getId() == extract.getId()) {
				messageCache.remove(m);
			}
		}
		FileSyncManager.save(thisClient.getIp() + "-" + thisClient.getPort());
		Message response = new Message(thisClient, msg.getPayload(), MessageType.MessageWritten);
		sender.sendMessage(response, Phonebook.getLeader());
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
				// System.out.println("started TimerTask");
				try {
					Thread.sleep(Long.valueOf(ThreadLocalRandom.current().nextInt(1, 10)));
				} catch (Exception e) {

				}
				if (role == 0) {
					resetVote();
					becomeCanidate();
				}
			}
		};
		electionTimeout = new Timer("Raftcycle-" + cycle);
		System.out.println("New Timer" + "Raftcycle-" + cycle);
		electionTimeout.scheduleAtFixedRate(raftCycleReset, 0, votingCycle());
	}

	public void stopElectionTimeout() {
		electionTimeout.cancel();
		cycle = cycle + 1;
	}

	public void voteLeader(Message msg) {
		if (Integer.parseInt(msg.getPayload(), 10) > term) {
			resetVote();
			didVote = true;
			term = Integer.parseInt(msg.getPayload(), 10);
			lastVote = msg.getSenderAsClient();
			Message ballot = new Message("null", msg.getPayload(), MessageType.Vote);
			sender.sendMessage(ballot, msg.getSenderAsClient());
		}
		restartElectionTimeout();
	}

	public void resetVote() {
		role = 0;
		syncRoleWithPhonebook();
		votes = 0;
		didVote = false;
	}

	private long votingCycle() {
		int random = (ThreadLocalRandom.current().nextInt(10, 150) + 150);
		return Long.valueOf(random);
	}

	private void leaderStepDown(Client c) {
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
		sender.sendMessage(konter, c);

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
				sender.sendMessage(konter, msg.getSenderAsClient());
			} else if (Integer.parseInt(msg.getPayload().split(":")[1], 10) >= term) {
				Message konter = new Message(thisClient, "4" + Phonebook.exportPhonebook(),
						MessageType.ResolveTwoLeaders);
				sender.sendMessage(konter, msg.getSenderAsClient());
				leaderStepDown(msg.getSenderAsClient());

			}
			break;
		case 2:
			leaderStepDown(msg.getSenderAsClient());
			Message konter = new Message(thisClient, "4" + Phonebook.exportPhonebook(), MessageType.ResolveTwoLeaders);
			sender.sendMessage(konter, msg.getSenderAsClient());
			break;
//		case 3:
//			Message konter = new Message(thisClient, "4"+Phonebook.exportPhonebook(),
//					MessageType.ResolveTwoLeaders);
//			sender.sendMessage(konter, msg.getSenderAsClient());
//			break;
		case 4:
			System.out.println("MErging two leaders");
			Phonebook.importPhonebook(msg.getPayload().substring(1, msg.getPayload().length()));
			Message IAmTheSenate = new Message(thisClient, term + "-" + this.thisClient.getIp() +"-"+thisClient.getPort()+ "-" + "Leader",
					MessageType.IAmTheSenat);
			sender.broadcastMessage(IAmTheSenate);
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
			task.setResponder(c);
			addTask(task);
		}
	}

	private void findAndDeleteTask(Client clnt, MessageType type) {
		Iterator<AwaitingResponse> itrTaskList = taskList.iterator();
		while (itrTaskList.hasNext()) {
			AwaitingResponse task = itrTaskList.next();
			if (task.getResponder() == clnt && task.getType() == type) {
				itrTaskList.remove();
			} else {
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
			if (task.getResponder() == clnt && task.getType() == type && task.getComparePayloads() == payload) {
				itrTaskList.remove();
			} else {
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
			System.out.println("send new Message"+nextMessage.getType());
			if (broadcast) {
				sender.broadcastMessage(nextMessage);
			} else {
				sender.sendMessage(nextMessage, reciepient);
			}
		}
	}

	public void sendNormalHeartbeat() {
		String payload = thisClient.getIp() + "-" + thisClient.getPort() + "-" + term;
		Message heartbeat = new Message(thisClient, payload, MessageType.Heartbeat);
		sender.broadcastMessage(heartbeat);
	}
	// ##############################################################
	//
	// --------------- Follower Capabilities ----------------------------
	//
	// #############################################################

	// after Recieving a I am Your Leader MEssage
	public void newLeaderChosen(Client clnt) {
		Phonebook.newLeader(clnt);
		if(role !=0) {
			role =0;
			syncRoleWithPhonebook();
		}
	}

	private void becomeCanidate() {
		resetVote();
		role = 1;
		syncRoleWithPhonebook();
		didVote = true;
		term = term + 1;
		votes = 1;
//		System.out.println("Voted for ME");
		checkVote();
		Message voteForMeMessage = new Message("192.168.178.51-3538",
				"Vote for me I am the best and I am better the AfD-N0de", MessageType.RequestVoteForMe);
		sender.broadcastMessage(voteForMeMessage);
		restartElectionTimeout();

	}

	private void checkVote() {
		System.out.println("Check Vote");
		if (Phonebook.countPhonebookEntries() == 0) {
			System.out.println("ERROR- Empty phonebook");
		} else {
			if (votes > (Phonebook.countPhonebookEntries() / 2)) {
				System.out.println("Majority Reached");
				becomeLeader();
			}
			System.out.println(votes + "(votes) VS (nodes)" + (Phonebook.countPhonebookEntries() / 2));

		}

	}

	private void becomeLeader() {
		System.out.println("Elected Leader");
		role = 2;
		taskList.clear();
  		stopElectionTimeout();
		Phonebook.newLeader(thisClient);
		heartbeatTimer = new Timer("Heartbeat-" + term);
		heartbeatTimer.scheduleAtFixedRate(heartbeat, 2, 35);
		Message IAmTheSenate = new Message(thisClient, term + "-" + this.thisClient.getIp()+"-"+thisClient.getPort() + "-" + "Leader",
				MessageType.IAmTheSenat);
		sender.broadcastMessage(IAmTheSenate);

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
		System.out.println("New Message of Type "+message.getType());
		/*switch (message.getType()) {
		case AlreadyVoted:
			break;
		case Heartbeat:
			if (role != 2) {
				restartElectionTimeout();
				role=0;
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
	
			break;
		case NewMessageForwardedToLeader:
			if (role == 2) {
				newMessageForwardedToLeader(message);
			} else {
				sender.sendMessage(message, Phonebook.getLeader());
			}
			break;
		case NewMessageToCache:
			if (role == 2) {
				gatherCacheResponses(message);
				if (!twoLeaders) {
					twoLeaders(message.getSenderAsClient());
				}
			} else {

			}
			break;
		case RequestVoteForMe:
			if (role == 2) {
				Message hb = new Message(thisClient, term + "-" + this.thisClient + "-" + "Leader",
						MessageType.Heartbeat);
				sender.sendMessage(hb,message.getSenderAsClient() );
			} else {

			}
			break;
		case Vote:
			if(role ==1) {
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
			if(role == 2) {
				if (!twoLeaders) {
				twoLeaders(message.getSenderAsClient());
			}
				
			}else {
				newLeaderChosen(message.getSenderAsClient());
				
			}
			break;
		case ReadyForRaft:
			if (role == 2) {
				Message newBroadcastSyncPhonebook = new Message(thisClient, Phonebook.exportPhonebook(),
						MessageType.NewClientInPhonebookSyncronizeWithAllClients);
				q.offer(newBroadcastSyncPhonebook);
			} else {
				Phonebook.importPhonebook(message.getPayload());
			}
			break;
		case HeartbeatResponse:
			gatherHeartbeatResponse(message);
			break;
		case MessageWritten:
			if(role==2) {
				gatherWriteResponses(message);
			}
			break;
		case NewClientInPhonebookSyncronizeWithAllClients:
			if(role==2) {
				if (!twoLeaders) {
					twoLeaders(message.getSenderAsClient());
				}
			}else {
				System.out.println("Importing Phonebook");
				Phonebook.importPhonebook(message.getPayload());
			}
			break;
//		case RequestActiveClientsListFromAnotherNode:
//			break;
		case RequestFullMessageHistoryFromAnotherNode:
			if(role ==2){
				
			}else {
				//TODO: BENGIN wollen wir einen ganzen Chache Transfer ?benötige dann FileSyncMessanger import / export functions
			}
			break;
		case ResolveTwoLeaders:
			if(role==2) {
				resolveTwoLeaders(message);
			}
			break;
//		case WannaJoin:
//			break;
//		case WhichPort:
//			break;
		default:
			break;

		}*/
	}

}
