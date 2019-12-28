package main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Sender implements Runnable {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public Client getLeader() {
    	//TODO: Gibt den Leader in der COntacts Table zurück
    	Client client = new Client("127.0.0.1", 12345);
    	return client;
    }
    
    public void Heartbeat() {
    	// contains Hash of all MEssages 
    	// last message writen in clear
    	
    }
    
    public void startConnection(String ip, int port) {
        try {
			clientSocket = new Socket(ip, port);
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public String sendMessage(ChatMessage msg,String ip, int port, ChatMessageCommands Command ) {
        out.println(msg);
        String resp = "error";
		try {
			resp = in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return resp;
    }

    // return something
    public String sendMessageToAllNodes(ChatMessage msg, ChatMessageCommands cmd) {
    	return "TEMPLATE";
    }
    public void stopConnection() {
        try {
			in.close();
			out.close();
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}

