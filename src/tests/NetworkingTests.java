package tests;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import main.Client;
import main.Message;
import main.MessageType;
import networking.IncomingServer;
import networking.NetworkListener;
import networking.OutgoingServer;
import util.MessageUtil;

public class NetworkingTests implements NetworkListener {

	private Message msg;
	private boolean everythingOkay;
	
	@Test
	void testSendAndReceive() {
		Message message = new Message("192.168.178.51-3504", "test", MessageType.WriteMessage);
		msg = message;
		MessageUtil.printMessage(msg);
		
		IncomingServer in = new IncomingServer("192.168.178.51-3503");
		in.registerListener(this);
		
		OutgoingServer out = new OutgoingServer();
		everythingOkay = false;
		out.sendMessage(message, new Client("localhost", 3503));
		
		try {
			Thread.sleep(3000);
			if(everythingOkay) 
				assertTrue(true);
			else
				fail();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessageReceived(Message message) {
		if(message.getSender().equals(msg.getSender()) &&
				message.getPayload().equals(msg.getPayload()) &&
				message.getType() == msg.getType()) {
			everythingOkay = true;
			MessageUtil.printMessage(message);
		}
	}
}
	