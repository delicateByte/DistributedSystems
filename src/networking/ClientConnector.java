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
		if(message.getType() == MessageType.WannaJoin) {
			System.out.println("My friend is joining...");
			Client leader = Phonebook.getLeader();
			if(leader != null)
				response.write(leader.getIp() + "-" + leader.getPort() + "\n");
			else
				response.write("no leader, try later\n");
			response.flush();
		}
	}
}
