package main;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import networking.NetworkListener;
import networking.OutgoingServer;
import storage.FileSyncManager;

public class Raft implements Runnable, NetworkListener {

	// Raft specific
	private int role; // Roles: 0 - Follower | 1 - Candidate | 2 - Leader
	private int term; // Term Counter of the current Raft NEtwork of this Node
	private int cycle; // Number of Election Timeouts within this node
	private ArrayList<ChatMessage> messageCache = new ArrayList<ChatMessage>();
	private boolean didVote = false;
	private Date lastVote;

	// Raft Timer
	Timer electionTimeout = new Timer("raftCycle-0");
	TimerTask raftCycleManager = new TimerTask() {
		public void run() {
			try {
				Thread.sleep(Long.valueOf(ThreadLocalRandom.current().nextInt(1, 10))); // TODO: is time to short ?
			} catch (Exception e) {

			}
			if (sender.getLeader() == null) {
				becomeCanidate();
			}
		}

	};

	// Utilities
	private OutgoingServer sender;
	private FileSyncManager fileWriter;

	// Raft Thread Constructor
	public Raft() {
		sender = new OutgoingServer();
		electionTimeout.schedule(raftCycleManager, votingCycle()); // Initial Start of Raft Cycle
	}

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

	public Message manageMessage(Message msg) {
		if (role == 2) {
			LogReplication(msg);
		} else {
			Client leader = this.sender.getLeader();
			//TODO: bei der Stelle weiﬂ ich nicht ganz wie ich das am besten w‰re
			this.sender.sendMessage(msg, leader.getIp(), leader.getPort(), MessageType.NewMessageForwardedToLeader);
		}
		return msg;
	}

	public void restartElectionTimeout() {
		electionTimeout.cancel();
		electionTimeout.purge();
		electionTimeout = new Timer("Raftcycle-" + cycle);
		electionTimeout.schedule(raftCycleManager, 10);
	}

	public void hearthbeatResetElectionTimout() {
		restartElectionTimeout();
	}

	private long votingCycle() {
		int random = (ThreadLocalRandom.current().nextInt(10, 150) + 150);
		return Long.valueOf(random);
	}

	private void becomeCanidate() {
		role = 1;
		// Vote for myself
		didVote = true;
		term = term + 1;
		int votes = 1;
		Message voteForMeMessage = new Message("192.168.178.51-3538", "Vote for me I am the best and I hate the AfD",
				MessageType.RequestVoteForMe);
		sender.broadcastMessage(voteForMeMessage);

		// Check if responses are in

	}

	public void voteLeader() {
		boolean didVote = true;
		electionTimeout.cancel();
		electionTimeout.purge();

	}

	public void setLeader() {
		didVote = false;
		term = term + 1;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessageReceived(Message message) {
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
		}

	}

}
