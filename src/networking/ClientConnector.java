package networking;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import main.Client;
import main.Message;
import main.MessageType;

public class ClientConnector implements NetworkListener{
	
	private Client me;
	private MessageSender sender;
	private List<Client> joiners;
	
	private static final int START_PORT = 30300;
	
	public ClientConnector(Client me, MessageSender sender) {
		this.me = me;
		this.sender = sender;
		joiners = new ArrayList<Client>();
	}

	public Client getMe() {
		return me;
	}

	public MessageSender getSender() {
		return sender;
	}

	@Override
	public void onMessageReceived(Message message, PrintWriter response) {
		if(message.getType() == MessageType.WhichPort) {
			System.out.println("My friend is joining...");
			Client leader = Phonebook.getLeader();
			if(leader != null)
				response.write(leader.getIp() + "-" + leader.getPort() + "\n");
			else
				response.write("no leader, try later\n");
			response.flush();
		} else if(message.getType() == MessageType.WannaJoin) {
			for(int i = START_PORT; i < 65535; i++) {
				boolean free = true;
				for(Client c : Phonebook.getFullPhonebook()) {
					if(c.getPort() == i) {
						free = false;
						break;
					}
				}
				if(free) {
					Client joiner = message.getSenderAsClient();
					joiner.setPort(i);
					Phonebook.addNewNode(joiner);
					System.out.println("Giving new port " + i + " to new joiner");
					response.write(i + "\n");
					response.flush();
					break;
				}
			}
			
		}
	}
}
