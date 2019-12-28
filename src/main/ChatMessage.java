package main;

public class ChatMessage {
	
	String text;
	String sender;
	int id;
	ChatMessageCommands command;
	
	// was ist ID, Was ist sender ?
	public ChatMessage(int id, String text, String sender, ChatMessageCommands cmd) {
		super();
		this.text = text;
		this.sender = sender;
		this.id = id;
		this.command = cmd;
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

	public ChatMessageCommands getCommand() {
		// TODO Auto-generated method stub
		return command;
	}
}
