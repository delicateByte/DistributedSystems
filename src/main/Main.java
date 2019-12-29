package main;

import java.util.ArrayList;
import java.util.List;

import networking.ClientConnector;
import networking.IncomingServer;
import networking.OutgoingServer;
import networking.Phonebook;
import storage.FileSyncManager;
import util.NetworkUtils;

public class Main {
	
	/* ARGS for first client:
	 * <own-port>
	 * ARGS for second client:
	 * <friend-ip> <friend-port>
	 */
	
    public static void main(String[] args) {
    	OutgoingServer sender = new OutgoingServer();
        
        if(args.length == 1) {
        	// storage and phonebook
        	FileSyncManager.initBlank();
        	Client me = new Client(NetworkUtils.getIP(), Integer.parseInt(args[0]));
        	Phonebook.addNewNode(me);
        	System.out.println(MessageType.WannaJoin.toString());
        	//raft
        	Raft myRaft = new Raft();
        	Thread raftThread = new Thread(myRaft);
        	
        	//networking
        	IncomingServer in = new IncomingServer(me.getPort());
        	in.registerListener(myRaft);
        	ClientConnector cc = new ClientConnector(me, sender);
        	in.registerListener(cc);
        	
        	System.out.println("Initalized network with me as only participant.");
        	System.out.println("Join Me: " + me.getIp() + " " + me.getPort());
//        	raftThread.start();
        } else if(args.length == 2) {
        	/*
    		One Joins a new Network
    		- start Program with IP & Port of friend
    		- I wanna join 
    		- response Leader is this ask him OR Try again later there is none
    		- Leader response none = wait and try again please 
    		- LEader response Yes , take this Port and join me brother 
    		- LEader sends a broadcast to all and says please add
    		- Await response of all 
    		- if no response from client xyz  retry with next heartbeat
    		- if HEartbeat from LEader -> start Raft 
    		- Here take the current phonebook
    		- take current Messages
    		- noraml raft stuff
*/
    		// Thread raftThread = new Thread(new Raft(senderThread));
        }else {
        	System.out.println("Wrong number of arguments.");
        }
    }
}




