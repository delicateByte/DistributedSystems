package networking;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import main.Message;
import main.MessageType;

public class OutgoingServer {
	
	/**
	 * Sends a network message to the recipient
	 * @param sender the sender in this form: 192.168.178.51-3538
	 * (ip + "-" + port)
	 * @param message the Message object that should be sent
	 * @param recipient the recipient in this form: 192.168.178.51-3538
	 * (ip + "-" + port)
	 * @return returns if sending was error free (no confirmation if 
	 * the message was delivered successfully). 
	 */
	public boolean sendMessage(Message message, String recipient) {
		String ip = recipient.split("-")[0];
		int port = Integer.parseInt(recipient.split("-")[1]);
		try {
			Socket socket = new Socket(ip, port);
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			pw.write(message.getSender() + ";" + message.getPayload() + ";" + message.getType().name());
			pw.flush();
			pw.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * Sends a network message to all participants
	 * @param message the Message object that should be sent
	 * @return returns if sending was error free (no confirmation if 
	 * the message was delivered successfully). 
	 */
	public boolean broadcastMessage(Message message) {
		return true;
	}
}
