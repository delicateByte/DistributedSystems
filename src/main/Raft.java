package main;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import networking.NetworkListener;
import networking.OutgoingServer;
import networking.Phonebook;
import storage.FileSyncManager;

//TODO: Two Leader = leader with Higher term takes it & Merge Client Lists 
//TODO: Task List with all open Tasks
// TODO: Filewrite 
public class Raft implements Runnable, NetworkListener {

	// Raft specific
	private int role; // Roles: 0 - Follower | 1 - Candidate | 2 - Leader
	private int term; // Term Counter of the current Raft NEtwork of this Node
	private int cycle; // Number of Election Timeouts within this node
	private ArrayList<ChatMessage> messageCache = new ArrayList<ChatMessage>();
	private boolean didVote = false;
	private Client lastVote;
	private Phonebook phonebook = new Phonebook();

	// Raft Timer
	Timer electionTimeout = new Timer("raftCycle-0");
	TimerTask raftCycleManager = new TimerTask() {
		public void run() {
			try {
				Thread.sleep(Long.valueOf(ThreadLocalRandom.current().nextInt(1, 10)));
			} catch (Exception e) {

			}
			if (role == 1) {
				resetVote();
			}
			phonebook.deactivateLeader();
			becomeCanidate();
			// Multiple Heartbeats in one elevtionTimeout==> if Timer is over there is a
			// problem with the Leader and a new needs to be elected
		}

	};
	// Leader Timer (HeartBeat & Tasklist)
	Timer heartbeatTimer = new Timer("Heartbeat");
	TimerTask heartbeat = new TimerTask() {
		public void run() {
			if (role == 2) {
				heartbeatTimer.cancel();
				heartbeatTimer.purge();
			}
			manageTasklist();
			heartbeat();
			// Multiple Heartbeats in one elevtionTimeout ==> if Timer is over there is a
			// problem with the Leader and a new needs to be elected

		}
	};

	// Utilities
	private OutgoingServer sender;
	private FileSyncManager fileWriter;

	// ##############################################################
	//
	// --------------- MAIN RAFT IMPLEMENTAION ----------------------------
	//
	// #############################################################
	public Raft() {
		sender = new OutgoingServer();
		
	}

	public void initialJoin() {

	}

	@Override
	public void run() {
		// Phonebook needs me already as an entry
		//I need to start recieving Heartbeats from Leader right away
		// needs to be already in phonbook of leader
		electionTimeout.schedule(raftCycleManager, votingCycle()); // Initial Start of Raft Cycle

	}

	// ##############################################################
	//
	// --------------- Log Replication ----------------------------
	//
	// #############################################################

	public void LogReplication(Message msg) {
		if (role == 2) {
			// chache Message
			// send msg to all Clients
			// await responses
			// send to all the write Command
			//
		} else {
			if (msg.getType() == MessageType.NewMessageToCache) {

			} else if (msg.getType() == MessageType.NewMessageToCache) {
				// TODO: write to file
				// check if writen to file
				// Delete from list
				// send response to Leader

			}
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
	// votes reset with new Vote for me request with a higher term then mine
	public void restartElectionTimeout() {
		electionTimeout.cancel();
		electionTimeout.purge();
		electionTimeout = new Timer("Raftcycle-" + cycle);
		electionTimeout.schedule(raftCycleManager, 10);
	}

	public void stopElectionTimeout() {
		electionTimeout.cancel();
		electionTimeout.purge();
	}

	public void voteLeader(Message msg) {
		if(Integer.parseInt(msg.getPayload(),10) > term) {
			didVote = true;
			term = Integer.parseInt(msg.getPayload(),10);
			lastVote = msg.getSenderAsClient();
			Message ballot = new Message(null, msg.getPayload(), MessageType.Vote);      // TODO: BEngin wollte sender bei message fixen,oder ?
			sender.sendMessage(ballot, msg.getSenderAsClient());
		}
		restartElectionTimeout();
	}

	public void resetVote() {
		role = 0;
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
		// check if still leader
	}

	private void heartbeat() {

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
		role = 1;
		voteLeader();
		term = term + 1;
		int votes = 1;
		Message voteForMeMessage = new Message("192.168.178.51-3538", "Vote for me I am the best and I hate the AfD",
				MessageType.RequestVoteForMe);
		sender.broadcastMessage(voteForMeMessage);

		//TODO: Check if responses are in
		votes = 1;
		if(phonebook.countPhonebookEntries()==0)
		if (votes < (phonebook.countPhonebookEntries() / 2)) {
			becomeLeader();
		}
	}

	private void becomeLeader() {
		// TODO: Node Inaugration
		// init Tasklist
		// start Heatbeat Timer
		// send Heartbeat
		// send I AM THE SENATE Message
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
			break;
		case MessageCached:
			break;
		case NewMessageForwardedToLeader:
			break;
		case NewMessageToCache:
			break;
		case RequestVoteForMe:
			break;
		case Vote:
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
