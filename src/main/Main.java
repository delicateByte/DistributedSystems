package main;

import java.util.ArrayList;
import main.ChatMessage;
import files.FileSyncManager;

public class Main {

    public static void main(String[] args) {
        Thread listenerThread = new Thread( new Listener());
        Thread senderThread = new Thread( new Sender());

//        listenerThread.start();
//        senderThread.start();
        
        FileSyncManager.syncToFile("192.168.178.51-2314", new ArrayList<ChatMessage>());
    }
}




