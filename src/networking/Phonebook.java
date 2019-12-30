package networking;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
		
//		Iterator<Client> itrClient = phonebook.iterator();
//		Client leader = null;
//		while (itrClient.hasNext()) {
//			if (itrClient.next().getRights() == 2) {
//				System.out.println(itrClient.next());
//				leader = itrClient.next();
//			} else {
//				// 
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

	}
	public static void setRights(Client clt,int role) {
		if(phonebook.contains(clt)) {
			clt.setRights(role);
		} else {
			for(Client c : phonebook) {
				if(c.getIp().equals(clt.getIp()) && c.getPort() == clt.getPort()) {
					c.setRights(role);
				}
			}
		}

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
		Client leader = getLeader();
		if(leader!= null) {
			getLeader().setActive(false);
		}
	}
	
	public static void activateLeader() {
		Client leader = getLeader();
		if(leader!= null) {
			getLeader().setActive(true);
		}
	}
	
	public static ArrayList<Client> getFullPhonebook() {
		return phonebook;
	}
	
	public static int countPhonebookEntries() {
		return phonebook.size();
	}
	
	//return final merged Phonebook
	public static List<Client> mergeTwoPhonebooks(List<Client> pb1,List<Client> pb2) {
		pb1.addAll(pb2);
		return pb1;
	}
	
	public static String exportPhonebook() {
		List<String> teilStrings = new ArrayList<String>();
		
		for(Client c : phonebook) {
			List<String>teilTeilStrings = new ArrayList<String>();
			teilTeilStrings.add(c.getIp());
			teilTeilStrings.add(""+c.getPort());
			teilTeilStrings.add(""+c.getRights());
//			teilTeilStrings.add(c.getName().replace(";", "\\;").replace("-", "\\-"));
			teilStrings.add(String.join(";", teilTeilStrings));
		}
		
		return String.join("-", teilStrings);
	}
	
	public static List<Client> importPhonebook(String impString) {
		String[] teilStrings = impString.split("(?<!\\\\)-");
		List<Client> newPhonebook = new ArrayList<Client>();
		for(String teilString : teilStrings) {
			String[] clientInfos = teilString.split("(?<!\\\\);");
			Client client = new Client(clientInfos[0], Integer.parseInt(clientInfos[1]));
			client.setRights(Integer.parseInt(clientInfos[2]));
//			client.setName(clientInfos[3].replace("\\;", ";").replace("\\-", "-"));
			newPhonebook.add(client);
		}
		return mergeTwoPhonebooks(phonebook, newPhonebook);
	}
	// function for marshal and unmarshal phonebook
}
