import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Thread eve = new Thread( new Listener());
        Thread alice = new Thread( new Sender());
        Thread bob = new Thread( new Listener());

        eve.start();
        alice.start();
        bob.start();

                try {

                    Scanner scanner = new Scanner(System.in);
                    while(true) {
                        System.out.println("Please enter command: ");
                        String cmd = scanner.nextLine();
                        if(cmd.equals("close")) {
                            eve.close();
                            System.out.println("Listener closed successfully");
                            break;
                        } else if(cmd.equals("msg")) {
                            System.out.println("Enter Address to be sent:    Command~IP~PORT~Message ");
                            alice.send(scanner.nextLine());
                            System.out.println("Sent successfully");
                        }
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }


        }




