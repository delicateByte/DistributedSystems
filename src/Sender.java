import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Sender implements Runnable {

        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public void startConnection(String ip, int port) {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        public String sendMessage(String msg,String ip, String port, String Command ) {
            out.println(msg);
            String resp = in.readLine();
            return resp;
        }

        public void stopConnection() {
            in.close();
            out.close();
            clientSocket.close();
        }
    }





}
