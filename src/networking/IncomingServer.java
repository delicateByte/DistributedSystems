package networking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import main.Message;
import main.MessageType;
import util.MessageUtils;

public class IncomingServer {
	private int port;
	private List<NetworkListener> listeners;
	
	/**
	 * starts an incomingserver for the raft node
	 * @param identifier the id in this form: 192.168.178.51-3538
	 * (ip + "-" + port)
	 */
	public IncomingServer(int port) {
		listeners = new ArrayList<NetworkListener>();
		
		try {
			ServerSocket ss = new ServerSocket(port);
			
			Thread acceptorThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while(true) {
						try {
							Socket connector = ss.accept();
							BufferedReader inFromClient =
									    new BufferedReader(new InputStreamReader(connector.getInputStream()));
							String messageString = inFromClient.readLine();
							if(listeners.size() != 0) {
								String[] infos = messageString.split("(?<!\\\\);");
								Message message = new Message(
										infos[0],
										infos[1].replace("\\;", ";"),
										MessageType.valueOf(infos[2]));
								PrintWriter writer = new PrintWriter(connector.getOutputStream());
							//	MessageUtils.printMessage(message);
								for(NetworkListener l : listeners) {
									l.onMessageReceived(message, writer);
								}
								writer.write("end\n");
								writer.flush();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
			acceptorThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void registerListener(NetworkListener listener) {
		listeners.add(listener);
	}
	
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
}
