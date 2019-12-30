package main;

public class AwaitingResponse {
	private Client responder;
	private MessageType type;
	private String comparePayloads ;
	// NOTE: AWAIT with leader/myself as client = broadcast response await
	public AwaitingResponse(Client c, MessageType t) {
		responder = c;
		type = t;
	}

	public Client getResponder() {
		return responder;
	}

	public void setResponder(Client responder) {
		this.responder = responder;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public String getComparePayloads() {
		return comparePayloads;
	}

	public void setComparePayloads(String comparePayloads) {
		this.comparePayloads = comparePayloads;
	}
	
}
