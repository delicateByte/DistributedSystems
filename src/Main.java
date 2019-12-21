import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Thread listenerThread = new Thread( new Listener());
        Thread senderThread = new Thread( new Sender());

        listenerThread.start();
        senderThread.start();
    }


}




