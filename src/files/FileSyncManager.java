package files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import main.ChatMessage;

public class FileSyncManager {
	static BufferedWriter bw;
	static BufferedReader br;
	static ArrayList<ChatMessage> cachedMessages = new ArrayList<ChatMessage>();

	/**
	 * Syncs the content of the the arraylist to a file 
	 * @param identifier a unique id for this instance (for example IP+Port)
	 * @param messages the arraylist that contains the messages
	 */
	public static void syncToFile(String identifier, List<ChatMessage> messages) {
		if (bw == null) {
			try {
				bw = new BufferedWriter(new FileWriter("./" + identifier + ".ciao"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		try {
			List<String> strings = new ArrayList<String>();
			String messageString = "";
			for(ChatMessage message : messages) {
				messageString = 	message.getId() + ";"
						+	message.getSender().replace(";", "\\;") + ";" 
						+ 	message.getText().replace(";", "\\;") + "";
				strings.add(messageString.replace("-", "\\-"));
			}
			bw.write(String.join("-", strings));
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<ChatMessage> syncFromFile(String identifier) {
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
				String[] messageMeta = messageString.split("(?<!\\\\);");
				// escapes are in the messageMeta \; 
				ChatMessage message = new ChatMessage(Integer.parseInt(messageMeta[0]), 
						messageMeta[2].replace("\\;", ";"), 
						messageMeta[1].replace("\\;", ";"));
				messages.add(message);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return messages;
	}
}
