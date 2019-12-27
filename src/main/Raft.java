package main;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import files.FileSyncManager;

public class Raft implements Runnable {
	// Raft specific
	private int role; 					// Roles: 0 - Follower | 1 - Candidate | 2 - Leader
	private int term;					// Term Counter of the current Raft NEtwork of this Node
	private int cycle;					// Number of Election Timeouts within this node
	private ArrayList<ChatMessage> messageCache = new ArrayList<ChatMessage>();
	private boolean didVote = false;
	private Date lastVote;
	
	// Raft Timer 
	Timer electionTimeout = new Timer("raftCycle-0");
	TimerTask raftCycleManager = new TimerTask() {
		
		public void run() {
			try {
				Thread.sleep(Long.valueOf(ThreadLocalRandom.current().nextInt(1,10))); // TODO: is time to short ?
	}catch(Exception e){

	}if(sender.getLeader()==null){becomeCanidate();}}

	};
	
	// Utilities
	private Sender sender;
	private FileSyncManager fileWriter;
	

	public Raft(Sender sender) {
		this.sender = sender;

	}

	public void LogReplication(ChatMessage msg) {
		if(role == 2) {
			// chache Message
			// send msg to all Clients
			// await responses
			// send to all the write Command
			// 
		}else {
			if(msg.getCommand() == ChatMessageCommands.NewMessageToCache) {
			
			}else if(msg.getCommand() == ChatMessageCommands.NewMessageToCache) {
			// TODO: write to file
			// check if writen to file 
			// Delete from list 
			// send response to Leader  
		
		}
			}
	}


	public ChatMessage manageMessage(ChatMessage msg) {
		if(role == 2) {
			LogReplication(msg);
		}else {
			Client leader = this.sender.getLeader();
			this.sender.sendMessage(msg,leader.getIp(), leader.getPort(), ChatMessageCommands.NewMessageForwardedToLeader);
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

	private void startRaft() {

	}

	private long votingCycle() {
		int random = (ThreadLocalRandom.current().nextInt(10, 150) + 150);
		return Long.valueOf(random);
	}

	private void becomeCanidate() {

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
