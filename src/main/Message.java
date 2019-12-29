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

	public Message(Client sender, String payload, MessageType type) {
		this.sender = sender.getIp() + "-" + sender.getPort();
		this.payload = payload;
		this.type = type;
	}

	public String getSender() {
		return sender;
	}
	public Client getSenderAsClient() {
		return new Client(sender.split("-")[0], Integer.parseInt(sender.split("-")[1]));
	}
	public String getPayload() {
		return payload;
	}
	public MessageType getType() {
		return type;
	}
}
