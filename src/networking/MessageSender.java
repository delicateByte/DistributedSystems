package networking;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import main.Client;
import main.Message;
import main.MessageType;
import util.MessageUtils;

public class MessageSender {
	
	/**
	 * Sends a network message to the recipient
	 * @param sender the sender in this form: 192.168.178.51-3538
	 * (ip + "-" + port)
	 * @param message the Message object that should be sent
	 * @param recipient the recipient in this form: 192.168.178.51-3538
	 * (ip + "-" + port)
	 * @return returns a response
	 * @throws Exception 
	 */
	public String sendMessage(Message message, Client client) throws Exception {
		try {
			Socket socket = new Socket(client.getIp(), client.getPort());
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			pw.write(message.getSender() + ";" + message.getPayload().replace(";", "\\;") + ";" + message.getType().name() + "\n");
			pw.flush();
			String response = br.readLine();
			br.close();
			socket.close();
			return response;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public String sendMessageAutoRetry(Message message, Client client, int maxRetries, String error) {
		return sendAgain(message, client, 0, maxRetries, error);
	}
	
	private String sendAgain(Message msg, Client target, int i, int maxRetries, String error) {
		i++;
		if(i > maxRetries){
			System.out.println("[RAFT] " + error);
			return "error";
		}
		try {
			return this.sendMessage(msg, target);
		} catch (Exception e) {
			return sendAgain(msg, target, i, maxRetries, error);
		}
	}

	
	/**
	 * Sends a network message to all participants
	 * @param message the Message object that should be sent
	 * @return returns if sending was error free (no confirmation if 
	 * the message was delivered successfully). 
	 */
	public List<Client> broadcastMessage(Message message, boolean retry, String error) {
		List<Client> errorClients = new ArrayList<Client>();
		for(Client c : Phonebook.getFullPhonebook()) {
			if(!c.getIp().equals(message.getSenderAsClient().getIp()) || c.getPort() != message.getSenderAsClient().getPort()) {
				try {
					sendMessage(message, c);
				}catch(Exception e) {
					errorClients.add(c);
				}
			}
		}
		//MessageUtils.printMessage(message);
		if(message.getType()!=MessageType.Heartbeat) {
			MessageUtils.printMessage(message);
		}
		
		if(retry) {
			for(Client c : errorClients) {
				this.sendMessageAutoRetry(message, c, 10, error);
			}
		}else {
			if(errorClients.size() != 0) {
				System.out.println("[RAFT] " + error);
			}
		}
		return errorClients;
	}
}
