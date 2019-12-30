package networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;

import main.Client;
import main.Message;

public class MessageSender {
	
	/**
	 * Sends a network message to the recipient
	 * @param sender the sender in this form: 192.168.178.51-3538
	 * (ip + "-" + port)
	 * @param message the Message object that should be sent
	 * @param recipient the recipient in this form: 192.168.178.51-3538
	 * (ip + "-" + port)
	 * @return returns a response
	 */
	public String sendMessage(Message message, Client client) {;
		try {
			System.out.println("Message"+message);
			System.out.println("To"+client);

			Socket socket = new Socket(client.getIp(), client.getPort());
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			pw.write(message.getSender() + ";" + message.getPayload().replace(";", "\\;") + ";" + message.getType().name() + "\n");
			pw.flush();
			String response = br.readLine();
			br.close();
			socket.close();
			return response;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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
