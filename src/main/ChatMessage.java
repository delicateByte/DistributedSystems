package main;

public class ChatMessage implements Comparable<ChatMessage>{
	
	String text;
	String sender;
	int id;
	long timestamp;
	
	public ChatMessage(int id, String text, String sender, Long timestamp) {
		this.text = text;
		this.sender = sender;
		this.id = id;
		this.timestamp = timestamp;
//		System.out.println(id);
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
	public void setId(int id) {
		this.id = id;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public static String chatMessageObjectToString(ChatMessage message) {
		String messageString = 	message.getId() + ";"
				+	message.getSender().replace(";", "\\;") + ";" 
				+ 	message.getText().replace(";", "\\;") + ";"
				+ 	message.getTimestamp();
		return messageString;
	}
	public static ChatMessage chatMessageStringToObject(String messageString) {
		String[] messageMeta = messageString.split("(?<!\\\\);");
		// escapes are in the messageMeta \; 
		ChatMessage message = new ChatMessage(Integer.parseInt(messageMeta[0]), 
				messageMeta[2].replace("\\;", ";"), 
				messageMeta[1].replace("\\;", ";"),
				Long.parseLong(messageMeta[3]));
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
