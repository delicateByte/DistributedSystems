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
	public static String chatMessageObjectToString(ChatMessage message) {
		String messageString = 	message.getId() + ";"
				+	message.getSender().replace(";", "\\;") + ";" 
				+ 	message.getText().replace(";", "\\;") + "";
		return messageString;
	}
	public static ChatMessage chatMessageStringToObject(String messageString) {
		String[] messageMeta = messageString.split("(?<!\\\\);");
		// escapes are in the messageMeta \; 
		ChatMessage message = new ChatMessage(Integer.parseInt(messageMeta[0]), 
				messageMeta[2].replace("\\;", ";"), 
				messageMeta[1].replace("\\;", ";"));
		return message;
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
