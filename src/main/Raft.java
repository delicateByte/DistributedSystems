package main;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import files.FileSyncManager;

public class Raft implements Runnable {

	private String role;
	int term;
	String currentLeader;
	String clientList[][];
	Date lastVote;
	ChatMessageCommands enumToCompare;
	boolean didVote = false;
	private Thread sender;
	private int cycle;
	private ArrayList<ChatMessage> messageCache = new ArrayList<ChatMessage>();
	private FileSyncManager fileWriter;

	public Raft(Thread sender) {
		this.sender = sender;
		
	}
	
	
	public void LogReplication(ChatMessage msg) {
		if(role == "Leader") {
			// chache Message
			// send msg to all Clients
			// await responses
			// send to all the write Command
			// 
		}else {
			if(msg.getCommand() == enumToCompare.NewMessageToCache) {
			
			}else if(msg.getCommand() == enumToCompare.NewMessageToCache)
			// TODO: write to file
			// check if writen to file 
			// Delete from list 
			// send response to Leader 
				await 
			Iterator<ChatMessage> itrMsg = messageCache.iterator();
			boolean msgFound = false;
			while (itrMsg.hasNext()) {
				if (itrMsg.next().getText() == msg.getText()) {
					msgFound = true;
				}
			}
			if (msgFound) {
				// TODO: Write new Message to file 
			} else {
				messageCache.add(msg);
			}
			return msg;
		}
		}
	}
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
	
	public ChatMessage manageMessage(ChatMessage msg) {
		if(role == "Leader") {
			LogReplication(msg);
		}else {
			Client leader = this.sender.getLeader();
			this.sender.sendMessage(msg,leader.getIp(), leader.getPort(), "FORWARD")
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
