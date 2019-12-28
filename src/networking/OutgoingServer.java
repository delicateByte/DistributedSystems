package networking;

import main.Message;

public class OutgoingServer {
	
	/**
	 * Sends a network message to the recipient
	 * @param message the Message object that should be sent
	 * @param recipient the recipient in this form: 192.168.178.51-3538
	 * (ip + "-" + port)
	 * @return returns if sending was error free (no confirmation if 
	 * the message was delivered successfully). 
	 */
	public boolean sendMessage(Message message, String recipient) {
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
