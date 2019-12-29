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

//TODO: Two Leader = leader with Higher term takes it & Merge Client Lists 
//TODO: Task List with all open Tasks
// TODO: Filewrite 
//TODO: Phonebook snyc 
// TODO: Find doubles in Tasklist
public class Raft implements Runnable, NetworkListener {

	// Raft specific
	private int role; // Roles: 0 - Follower | 1 - Candidate | 2 - Leader
	private int term; // Term Counter of the current Raft NEtwork of this Node
	private int cycle; // Number of Election Timeouts within this node
	private boolean didVote = false;
	private Client lastVote;
	private Phonebook phonebook = new Phonebook();
	private Client thisClient; // TODO: Bengin how can i get tis info
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
			// TODO: phonebook.deactivateLeader();
			// Multiple Heartbeats in one elevtionTimeout==> if Timer is over there is a
			// problem with the Leader and a new needs to be elected
		}

	};
	// Leader Timer (HeartBeat & Tasklist)
	Timer heartbeatTimer = new Timer("Heartbeat");
	TimerTask heartbeat = new TimerTask() {
		public void run() {
//			System.out.println("Heartbeat"+thisClient.getIp()+":"+thisClient.getPort());
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
	private Map<Integer,Integer> messageResponseAggregator = new HashMap<Integer,Integer>();
	ArrayList<AwaitingResponse> taskList = new ArrayList<AwaitingResponse>(); // holds all response leader is waiting
																				// for
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
		if (role == 2) {
			messageCache.add(ChatMessage.chatMessageStringToObject(msg.getPayload()));
			String payload = msg.getPayload();
			ChatMessage extractId = ChatMessage.chatMessageStringToObject(msg.getPayload());
			messageResponseAggregator.put(extractId.getId(),0); // Adds new Key-value pair to the map that checks how many responses for a message have arrived
			Message cacheMessage = new Message(thisClient,payload,MessageType.NewMessageToCache);
			q.offer(cacheMessage);
			//sender.broadcastMessage(cacheMessage);
			AwaitingResponse newTask= new AwaitingResponse(thisClient, MessageType.MessageCached);
			newTask.setComparePayloads(msg.getPayload());
			addBroadcastResponseTask(newTask);
		} else {
						}
		}
	
	public void gatherCacheResponses(Message msg) {
		AwaitingResponse cmp= new AwaitingResponse(msg.getSenderAsClient(), msg.getType());
		cmp.setComparePayloads(msg.getPayload());
		if(taskList.contains(cmp)) {
			findAndDeleteTask(msg.getSenderAsClient(), msg.getType(), msg.getPayload()); // TODO:handle failure of this function
			if(true) {
				
			}
		}
	}
	public void gatherWriteResponses(Message msg) {
		
	}

	// ##############################################################
	//
	// --------------- Voting ----------------------------
	//
	// #############################################################
	public void hearthbeatResetElectionTimout() {
		restartElectionTimeout();
	}

	public void restartElectionTimeout() {
		stopElectionTimeout();
		TimerTask raftCycleReset = new TimerTask() {
			public void run() {
				//System.out.println("started TimerTask");
				try {
					Thread.sleep(Long.valueOf(ThreadLocalRandom.current().nextInt(1, 10)));
				} catch (Exception e) {

				}
				if (role == 0) {
					resetVote();
					becomeCanidate();
				}}};
		electionTimeout = new Timer("Raftcycle-" + cycle);
		System.out.println("New Timer"+"Raftcycle-" + cycle);
		electionTimeout.scheduleAtFixedRate(raftCycleReset, 0, votingCycle());
	}

	public void stopElectionTimeout() {
		electionTimeout.cancel();
		cycle=cycle+1;
	}

	public void voteLeader(Message msg) {
		if (Integer.parseInt(msg.getPayload(), 10) > term) {
			resetVote();
			didVote = true;
			term = Integer.parseInt(msg.getPayload(), 10);
			lastVote = msg.getSenderAsClient();
			Message ballot = new Message("null", msg.getPayload(), MessageType.Vote); // TODO: BEngin wollte sender bei
			// message fixen,oder ?
			sender.sendMessage(ballot, msg.getSenderAsClient());
		}
		restartElectionTimeout();
	}

	public void resetVote() {
		role = 0;
		votes = 0;
		didVote = false;
	}

	private long votingCycle() {
		int random = (ThreadLocalRandom.current().nextInt(10, 150) + 150);
		return Long.valueOf(random);
	}

	// ##############################################################
	//
	// --------------- Leader Capabilities ----------------------------
	//
	// #############################################################

	private void manageTasklist() {

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
	private void findAndDeleteTask(Client clnt, MessageType type,String payload) {
		Iterator<AwaitingResponse> itrTaskList = taskList.iterator();
		while (itrTaskList.hasNext()) {
			AwaitingResponse task = itrTaskList.next();
			if (task.getResponder() == clnt && task.getType() == type && task.getComparePayloads()==payload) {
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
		if (q.isEmpty()) {
			sendNormalHeartbeat();
		} else {
			Message nextMessage = q.poll();
			if(nextMessage.getSenderAsClient() ==thisClient) {
				// broadcasts
			}else {
				// single sends
			}
			switch (nextMessage.getType()) {
			case AlreadyVoted:
				// Do nothing
				// delete TAsk
				break;
			case Heartbeat:
				// TODO: Two LeaderS Problem should not occur
				break;
			case IAmTheSenat:

				break;
			case MessageCached:
				break;
			case NewClientInPhonebookSyncronizeWithAllClients:
				break;
			case NewMessageForwardedToLeader:
				break;
			case NewMessageToCache:
				break;
			case RequestActiveClientsListFromAnotherNode:
				break;
			case RequestFullMessageHistoryFromAnotherNode:
				break;
			case RequestVoteForMe:
				// payload = payload that is returned with VOTE
				break;
			case Vote:
				break;
			case WannaJoin:
				break;
			case WhichPort:
				break;
			case WriteMessage:
				break;
			case HeartbeatResponse:
				break;
			default:
				break;

			}
		}
	}

	public void sendNormalHeartbeat() {
		String payload = "test";
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
		phonebook.newLeader(clnt);
	}

	private void becomeCanidate() {
		resetVote();
		role = 1;
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
		role=2;
		taskList.clear();
		stopElectionTimeout();
		Phonebook.newLeader(thisClient);
		heartbeatTimer = new Timer("Heartbeat-" + term);
		heartbeatTimer.scheduleAtFixedRate(heartbeat, 2, 35);
		Message IAmTheSenate = new Message(thisClient, term + "-" + this.thisClient + "-" + "Leader",
				MessageType.IAmTheSenat);
		sender.broadcastMessage(IAmTheSenate);

	}

	public void stopHeartbeat() {
		heartbeatTimer.cancel();
	}

	// ##############################################################
	//
	// --------------- Message Management ----------------------------
	//
	// #############################################################

	@Override
	public void onMessageReceived(Message message, PrintWriter response) {
		// TODO was soll passieren wenn du eine message bekommst?
		switch (message.getType()) {
		case AlreadyVoted:
			break;
		case Heartbeat:
			if (role != 2) {
				restartElectionTimeout();
			}
			break;
		case MessageCached:
			gatherCacheResponses(message);
			break;
		case NewMessageForwardedToLeader:
			if(role ==2) {
				newMessageForwardedToLeader(message);
			}else {
				sender.sendMessage(message, Phonebook.getLeader());
			}
			break;
		case NewMessageToCache:
			break;
		case RequestVoteForMe:
			break;
		case Vote:
			checkVote();
			break;
		case WriteMessage:
			break;
		case IAmTheSenat:
			// extract client from Message
			newLeaderChosen(message.getSenderAsClient());
			break;
		}
	}

}
