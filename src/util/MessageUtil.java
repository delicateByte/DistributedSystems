package util;

import main.Message;

public class MessageUtil {

	public static void printMessage(Message msg) {
		System.out.println("Message sender: " + msg.getSender());
		System.out.println("Message type: " + msg.getType());
		System.out.println("Message payload:");
		System.out.println(msg.getPayload());
	}
}
