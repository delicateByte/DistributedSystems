import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class Raft {

	private String role;
	int term;
	String currentLeader;
	String clientList[][];
	Date lastVote;
	boolean didVote=false;
	private Sender sender;
	private int cycle;
	public Raft(Sender sender) {
	 this.sender = sender;
	}

	Timer electionTimeout = new Timer("raftCycle-0");
	TimerTask raftCycleManager = new TimerTask() {
		public void run() {
			try{
				Thread.sleep(Long.valueOf(ThreadLocalRandom.current().nextInt(1, 10))); //TODO: is time to short ?
			}catch(Exception e) {
				
			}
			if(sender.getLeader() == null ) {
				becomeCanidate();
			}
		}
		
	};
	
	
	
	public Message cacheChange(Message msg) {
		return msg;
	}
	public void restartElectionTimeout() {
		electionTimeout.cancel();
		electionTimeout.purge();
		electionTimeout = new Timer ("Raftcycle-"+cycle);
		electionTimeout.schedule(raftCycleManager, 10);
	}
	public void hearthbeatResetElectionTimout() {
		restartElectionTimeout();
		
	}
	private void startRaft() {
		
		
	}
	private long votingCycle() {
		int random = (ThreadLocalRandom.current().nextInt(10, 150) +150 );
		return  Long.valueOf(random);
	}
	
	private void becomeCanidate() {
		
	}
	public void voteLeader() {
		boolean didVote=true;
		electionTimeout.cancel();
		electionTimeout.purge();
		
	}
	public void setLeader() {
		didVote=false;
		term = term+1;
	}
	
	
}
