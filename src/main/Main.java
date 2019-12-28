package main;

import java.util.ArrayList;
import java.util.List;

import files.FileSyncManager;

public class Main {

    public static void main(String[] args) {
        Thread listenerThread = new Thread( new Listener());
        Thread senderThread = new Thread( new Sender());

//        listenerThread.start();
//        senderThread.start();	
        
    }
}




