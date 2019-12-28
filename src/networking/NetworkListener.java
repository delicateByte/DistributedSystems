package networking;

import main.Message;

public interface NetworkListener {
	public void onMessageReceived(Message message);
}
