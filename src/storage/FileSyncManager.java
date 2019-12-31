package storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import main.ChatMessage;

public class FileSyncManager {
	
	private static final int MAX_MESSAGES = 20;
	
	private static BufferedWriter bw;
	private static BufferedReader br;
	private static List<ChatMessage> messages;
	
	/**
	 * Initializes the messages message list with 0 elements
	 */
	public static void initBlank() {
		messages = new ArrayList<ChatMessage>();
	}
	
	/**
	 * Initializes the message list with the content of the respective file
	 * @param identifier a unique id for this instance (for example IP+Port)
	 */
	public static void initFromFile(String identifier) {
		initBlank();
		messages = syncFromFile(identifier);
		sort();
	}
	
	/**
	 * Saves the current message list to the respective file
	 * @param identifier
	 */
	public static void save(String identifier) {
		sort();
		syncToFile(identifier, messages);
	}
	
	/**
	 * Adds a message to the message list for saving and syncing
	 * @param message the message to be added
	 */
	public static void addMessage(ChatMessage message) {
		messages.add(message);
		if(messages.size() > MAX_MESSAGES)	// remove topmost message if we have more 
			messages.remove(0);			  	// messages than MAX_MESSAGES
		sort();
		
//		for(ChatMessage m  : messages) {
//			System.out.println("====== " + ChatMessage.chatMessageObjectToString(m));
//		}
	}
	
	public static List<ChatMessage> getMessages() {
		return messages;
	}
	
	private static void sort() {
		Collections.sort(messages);
	}
	

	public static String exportHistory() {
		return getString(messages);
	}
	
	/**
	 * Syncs the content of the the arraylist to a file 
	 * @param identifier a unique id for this instance (for example IP+Port)
	 * @param messages the arraylist that contains the messages
	 */
	private static void syncToFile(String identifier, List<ChatMessage> messages) {
		try {	
			bw = new BufferedWriter(new FileWriter("./" + identifier + ".ciao"));
			bw.write(getString(messages));
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String getString(List<ChatMessage> messages) {
		List<String> strings = new ArrayList<String>();
		String messageString = "";
		for(ChatMessage message : messages) {
			messageString = ChatMessage.chatMessageObjectToString(message);
			strings.add(messageString.replace("-", "\\-"));
		}
		return String.join("-", strings);
	}
	
	public static List<ChatMessage> saveFromString(String input){
		List<ChatMessage> toReturn = new ArrayList<ChatMessage>();
		
		List<String> messageStrings = Arrays.asList(input.split("(?<!\\\\)-"));
		
		for(String messageString : messageStrings) {
			messageString = messageString.replace("\\-", "-");
			messages.add(ChatMessage.chatMessageStringToObject(messageString));
		}
		return toReturn;
	}

	private static List<ChatMessage> syncFromFile(String identifier) {
		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		try {
			br = new BufferedReader(new FileReader(new File("./" + identifier + ".ciao")));
			String fileIn = br.readLine();
			saveFromString(fileIn);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return messages;
	}
}
