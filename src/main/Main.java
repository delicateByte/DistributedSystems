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
        
//        List<ChatMessage> messages = new ArrayList<ChatMessage>();
//        ChatMessage temp = new ChatMessage(3, "Lorem Ipsum xyz", "192.168.178.51-2314");
//        messages.add(temp);
//        temp = new ChatMessage(4, "Lorem Sips|um xyz", "192.168.178.51-2313");
//        messages.add(temp);
//        temp = new ChatMessage(4, "sldfj;sidjflskdf", "192.168.178.51-2314");
//        messages.add(temp);
//        messages = FileSyncManager.syncFromFile("192.168.178.51-2314");
//        FileSyncManager.syncToFile("192.168.178.51-2314", messages);
        
    }
}




