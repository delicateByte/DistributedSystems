package main;

import java.util.ArrayList;
import java.util.List;

import storage.FileSyncManager;

public class Main {
	// ARGS: IP ; PORT ; MODE
    public static void main(String[] args) {
        Thread listenerThread = new Thread( new Listener());
        Thread senderThread = new Thread( new Sender());
       // FileSyncManager.initFromFile(null);                              <------------------------------------bengin pls help
    	if(args[2]== "CONNECT") {
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

    	}else if(args[2]=="INIT") {
    		  /*Create a Neyetwork
    		- Port as arg   ( do you thing)  <---------------------------------------------------------- bengin
    		- add myself to phonebook <------------------------------------------------------------------------- bengin 
    		*/
    		// Thread raftThread = new Thread(new Raft(senderThread));
    	}
		//  raftThread.start();



//        listenerThread.start();
//        senderThread.start();	
        
    }
}




