package main;
import java.net.*;
import java.io.*;

public class Client  {
       private String ip;
       private int port;
       private String name;
       private  int rights;
       public client (String ip, int port) {
    	   this.ip = ip;
    	   this.port = port;
       }
	
	/* private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private Boolean connectionStatus =true;

        @Override
        public void run(){
            startConnection("192.168.43.20",1337);
            while(connectionStatus){
                try{
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    System.out.println(in.readLine());
                }catch (Exception e){

                }
            }
        }
        public void startConnection(String ip, int port) {
            try {
                clientSocket = new Socket(ip, port);
                System.out.println("test");
                System.out.println(clientSocket);

            }catch(Exception e){

            }

        }

        public String sendMessage(String msg) {
            out.println(msg);

            //String resp = in.readLine();
            return "hi";
        }

        public void stopConnection() {
            try{
                in.close();
                clientSocket.close();
            } catch (Exception e){

            }

        }
*/

}