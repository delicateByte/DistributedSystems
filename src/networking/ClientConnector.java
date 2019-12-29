package networking;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import main.Client;
import main.Message;
import main.MessageType;

public class ClientConnector implements NetworkListener{
	
	private Client me;
	private OutgoingServer sender;
	private List<Client> joiners;
	
	public ClientConnector(Client me, OutgoingServer sender) {
		this.me = me;
		this.sender = sender;
		joiners = new ArrayList<Client>();
	}

	public Client getMe() {
		return me;
	}

	public OutgoingServer getSender() {
		return sender;
	}

	@Override
	public void onMessageReceived(Message message, PrintWriter response) {
		if(message.getType() == MessageType.WannaJoin) {
			Client leader = Phonebook.getLeader();
			response.write(leader.getIp() + "-" + leader.getPort());
			response.flush();
		}
	}
}
