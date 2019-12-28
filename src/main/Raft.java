package main;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import storage.FileSyncManager;

public class Raft implements Runnable {
	
	// Raft specific
	private int role; // Roles: 0 - Follower | 1 - Candidate | 2 - Leader
	private int term; // Term Counter of the current Raft NEtwork of this Node
	private int cycle; // Number of Election Timeouts within this node
	private ArrayList<ChatMessage> messageCache = new ArrayList<ChatMessage>();
	private boolean didVote = false;
	private Date lastVote;

	// Raft Timer
	Timer electionTimeout = new Timer("raftCycle-0");
	TimerTask raftCycleManager = new TimerTask() 
	{
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
	private Sender sender;
	private FileSyncManager fileWriter;

	
	// Raft Thread Constructor
	public Raft(Sender sender) {
		this.sender = sender;
		electionTimeout.schedule(raftCycleManager, votingCycle()); // Initial Start of Raft Cycle
	}

	
	
	public void LogReplication(ChatMessage msg) {
		if (role == 2) {
			// chache Message
			// send msg to all Clients
			// await responses
			// send to all the write Command
			//
		} else {
			if (msg.getCommand() == ChatMessageCommands.NewMessageToCache) {

			} else if (msg.getCommand() == ChatMessageCommands.NewMessageToCache) {
				// TODO: write to file
				// check if writen to file
				// Delete from list
				// send response to Leader

			}
		}
	}

	public ChatMessage manageMessage(ChatMessage msg) {
		if (role == 2) {
			LogReplication(msg);
		} else {
			Client leader = this.sender.getLeader();
			this.sender.sendMessage(msg, leader.getIp(), leader.getPort(),
					ChatMessageCommands.NewMessageForwardedToLeader);
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
		term = term +1;
		int votes = 1;
		ChatMessage voteForMeMessage = new ChatMessage(0, "Vote for me I am the best and I hate the AfD", "TESTIP+TESTPORT", ChatMessageCommands.RequestVoteForMe);
		sender.sendMessageToAllNodes(voteForMeMessage, ChatMessageCommands.RequestVoteForMe);
	
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

}
