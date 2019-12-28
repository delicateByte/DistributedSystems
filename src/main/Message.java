package main;

public class Message {
	private String sender;
	private Object payload;
	private MessageType type;
	
	public Message(String sender, Object payload, MessageType type) {
		this.sender = sender;
		this.payload = payload;
		this.type = type;
	}
	
	public String getSender() {
		return sender;
	}
	public Object getPayload() {
		return payload;
	}
	public MessageType getType() {
		return type;
	}
}
