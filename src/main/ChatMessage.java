package main;

public class ChatMessage {
	
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
}
