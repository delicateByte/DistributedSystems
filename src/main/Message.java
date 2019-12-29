package main;

public class Message {
	private String sender;
	private String payload;
	private MessageType type;
	
	public Message(String sender, String payload, MessageType type) {
		this.sender = sender;
		this.payload = payload;
		this.type = type;
	}
	
	public String getSender() {
		return sender;
	}
	public Client getSenderAsClient() {
		return new Client("aas", 1);   // TODO:  <-------------------------bengin could you do that please
	}
	public String getPayload() {
		return payload;
	}
	public MessageType getType() {
		return type;
	}
}
