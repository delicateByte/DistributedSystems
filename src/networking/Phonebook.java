package networking;

import java.util.ArrayList;
import java.util.Iterator;

import main.Client;

public class Phonebook {
	// Contacts table
	private static ArrayList<Client> phonebook = new ArrayList<Client>();

	public static Client getLeader() {
		for(Client c : phonebook) {
			if(c.getRights() == 2)
				return c;
		}
		// code below is only executed when no leader was found
		//TODO: Do something in this line because leader is not found
		return null;
		
//		Iterator<Client> itrClient = phonebook.iterator();
//		Client leader = null;
//		while (itrClient.hasNext()) {
//			if (itrClient.next().getRights() == 2) {
//				System.out.println(itrClient.next());
//				leader = itrClient.next();
//			} else {
//				// TODO: HOW HANDLE IF NO LEADER IS FOUND AT A POINT IN TIME
//			}
//		}
//		return leader;
	}

	public static void newLeader(Client clt) {
		if(phonebook.contains(clt)) {
			clt.setRights(2);
		} else {
			for(Client c : phonebook) {
				if(c.getIp().equals(clt.getIp()) && c.getPort() == clt.getPort()) {
					c.setRights(2);
				}
			}
		}
//		Iterator<Client> itrClient = phonebook.iterator();
//		while (itrClient.hasNext()) {
//			if (itrClient.next() == clt) {
//				itrClient.next().setRights(2);
//			} else {
//				// TODO: HOW HANDLE IF NO LEADER IS FOUND AT A POINT IN TIME
//			}
//		}
	}

	public static void addNewNode(Client client) {
		phonebook.add(client);
	}

	public static void deleteClient(Client client) {
		if(phonebook.contains(client)) {
			phonebook.remove(client);
		}else {
			Iterator<Client> it = phonebook.iterator();
			while(it.hasNext()) {
				Client c = it.next();
				if(c.getIp().equals(client.getIp()) && c.getPort() == client.getPort()) {
					it.remove();
				}	
			}
		}
	}

	public static void deactivateClient(Client c) {
		c.setActive(false);
	}
	public static void activateClient(Client c) {
		c.setActive(true);
	}
	
	public static void deactivateLeader() {
		getLeader().setActive(false);
	}
	
	public static void activateLeader() {
		getLeader().setActive(true);
	}
	
	public static ArrayList<Client> getFullPhonebook() {
		return phonebook;
	}
	
	public static int countPhonebookEntries() {
		return phonebook.size();
	}
	
	//return final merged Phonebook
	public static ArrayList<Client> mergeTwoPhonebooks(ArrayList<Client> pb1,ArrayList<Client> pb2) {
		pb1.addAll(pb2);
		return pb1;
	}
}
