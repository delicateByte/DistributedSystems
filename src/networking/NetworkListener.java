package networking;

import java.io.PrintWriter;

import main.Message;

public interface NetworkListener {
	public void onMessageReceived(Message message, PrintWriter response);
}
