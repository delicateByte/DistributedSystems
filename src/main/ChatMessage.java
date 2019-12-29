package main;

public class ChatMessage implements Comparable<ChatMessage>{
	
	String text;
	String sender;
	int id;
	
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
	public static String chatMessageObjectToString(ChatMessage msg) {
		return "RESULT";
	}
	public static ChatMessage chatMessageStringToObject(String str) {
		return null;
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
