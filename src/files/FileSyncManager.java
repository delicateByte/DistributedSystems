package files;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import main.ChatMessage;

public class FileSyncManager {
	static BufferedWriter bw;


	/**
	 * Syncs the content of the the arraylist to a file 
	 * @param identifier a unique id for this instance (for example IP+Port)
	 * @param messages the arraylist that contains the mesages
	 */
	public static void syncToFile(String identifier, ArrayList<ChatMessage> messages) {
		if (bw == null) {
			try {
				bw = new BufferedWriter(new FileWriter("./" + identifier + ".ciao"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		try {
			bw.write("AC");
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<String> syncFromFile() {
		return null;
	}
}
