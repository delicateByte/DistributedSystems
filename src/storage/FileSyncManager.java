package storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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
	}
	
	public static List<ChatMessage> getMessages() {
		return messages;
	}
	
	private static void sort() {
		Collections.sort(messages);
	}
	

	/**
	 * Syncs the content of the the arraylist to a file 
	 * @param identifier a unique id for this instance (for example IP+Port)
	 * @param messages the arraylist that contains the messages
	 */
	private static void syncToFile(String identifier, List<ChatMessage> messages) {
		if (bw == null) {
			try {
				bw = new BufferedWriter(new FileWriter("./" + identifier + ".ciao"));
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		try {
			List<String> strings = new ArrayList<String>();
			String messageString = "";
			for(ChatMessage message : messages) {
				messageString = ChatMessage.chatMessageObjectToString(message);
				strings.add(messageString.replace("-", "\\-"));
			}
			bw.write(String.join("-", strings));
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<ChatMessage> syncFromFile(String identifier) {
		if(br == null) {
			try {
				br = new BufferedReader(new FileReader(new File("./" + identifier + ".ciao")));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		try {
			String fileIn = br.readLine();
			List<String> messageStrings = Arrays.asList(fileIn.split("(?<!\\\\)-"));
			
			for(String messageString : messageStrings) {
				messageString = messageString.replace("\\-", "-");
				messages.add(ChatMessage.chatMessageStringToObject(messageString));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return messages;
	}
}
