package main;

public class ChatMessage implements Comparable<ChatMessage>{
	
	String text;
	String sender;
	int id;
	
	// was ist ID, Was ist sender ?
	public ChatMessage(int id, String text, String sender) {
		super();
		this.text = text;
		this.sender = sender;
		this.id = id;
	}
	
	public String getText() {
		return text;
	}
	public String getSender() {
		return sender;
	}
	public int getId() {
		return id;
	}

	@Override
	public int compareTo(ChatMessage otherMessage) {
		if(this.getId() > otherMessage.getId())
			return 1;
		else if(this.getId() < otherMessage.getId())
			return -1;
		else
			return 0;
	}
}
