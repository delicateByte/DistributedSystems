package main;

import java.util.ArrayList;
import java.util.List;

import networking.ClientConnector;
import networking.IncomingServer;
import networking.MessageSender;
import networking.Phonebook;
import storage.FileSyncManager;
import util.NetworkUtils;

public class Main {

	/*
	 * ARGS for first client: <own-port> ARGS for second client: <friend-ip>
	 * <friend-port>
	 */

	public static void main(String[] args) {
		MessageSender sender = new MessageSender();

		FileSyncManager.initBlank();

		if (args.length == 1) {
			Client me = new Client(NetworkUtils.getIP(), Integer.parseInt(args[0]));
			Phonebook.addNewNode(me);

			// networking
			IncomingServer in = new IncomingServer(me.getPort());
			ClientConnector cc = new ClientConnector(me, sender);
			in.registerListener(cc);

			System.out.println("Initalized network with me as only participant.");
			System.out.println("Join Me: " + me.getIp() + " " + me.getPort());
			
			// raft
			Raft myRaft = new Raft(me);
			Thread raftThread = new Thread(myRaft);
//			in.registerListener(myRaft);
//			raftThread.start();
		} else if (args.length == 2) {
			// ask friend for the leader id
			Client me = new Client(NetworkUtils.getIP(), Integer.parseInt(args[1]));
			Client friend = new Client(args[0], Integer.parseInt(args[1]));
			Message whichPort = new Message(me, "pleeeeease", MessageType.WhichPort);
			System.out.println("Sending join request to friend " + friend.getIp() + "-" + friend.getPort());
			String leaderInfo = sender.sendMessage(whichPort, friend);
			System.out.println("leader-response: " + leaderInfo);

			// contact leader for a free port
			while(leaderInfo.contentEquals("no leader, try later")) {
				try {
					Thread.sleep(1000);
					leaderInfo = sender.sendMessage(whichPort, friend);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			Client leader = new Client(leaderInfo.split("-")[0], Integer.parseInt(leaderInfo.split("-")[1]));
			Message wannaJoin = new Message(me, "pleeeeease", MessageType.WannaJoin);
			int port = Integer.parseInt(sender.sendMessage(wannaJoin, leader));
			System.out.println("port-response: " + port);
			
			//setup with new port
			me.setPort(port);
			Phonebook.addNewNode(me);
			Phonebook.addNewNode(leader);
			Phonebook.newLeader(leader);
			
			IncomingServer in = new IncomingServer(port);
			ClientConnector cc = new ClientConnector(me, sender);
			in.registerListener(cc);
			
			//send ich bin ready
			Message ready = new Message(me, "", MessageType.ReadyForRaft);
			sender.sendMessage(ready, leader);
			
			
			// raft
			Raft myRaft = new Raft(me);
			Thread raftThread = new Thread(myRaft);
			in.registerListener(myRaft);
			raftThread.start();
			
			
			/*
			 * One Joins a new Network - start Program with IP & Port of friend - I wanna
			 * join - response Leader is this ask him OR Try again later there is none -
			 * Leader response none = wait and try again please - LEader response Yes , take
			 * this Port and join me brother - LEader sends a broadcast to all and says
			 * please add - Await response of all - if no response from client xyz retry
			 * with next heartbeat - if HEartbeat from LEader -> start Raft - Here take the
			 * current phonebook - take current Messages - noraml raft stuff
			 */
			// Thread raftThread = new Thread(new Raft(senderThread));
		} else {
			System.out.println("Wrong number of arguments.");
		}
	}
}
