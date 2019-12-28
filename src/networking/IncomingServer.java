package networking;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import main.Message;
import main.MessageType;

public class IncomingServer {
	private String ip;
	private int port;
	private NetworkListener listener;
	
	/**
	 * starts an incomingserver for the raft node
	 * @param identifier the id in this form: 192.168.178.51-3538
	 * (ip + "-" + port)
	 */
	public IncomingServer(String identifier) {
		this.ip = identifier.split("-")[0];
		this.port = Integer.parseInt(identifier.split("-")[1]);
		
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
							if(listener != null) {
								String[] infos = messageString.split("(?<!\\\\);");
								Message message = new Message(
										infos[0],
										infos[1].replace("\\;", ";"),
										MessageType.valueOf(infos[2]));
								listener.onMessageReceived(message);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
			acceptorThread.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void registerListener(NetworkListener listener) {
		this.listener = listener;
	}
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
}
