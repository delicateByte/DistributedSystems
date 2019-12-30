package tests;

import GUI.ChatListener;

public class ChatMessageGenerator {
	
	private static Thread spammer;
	private static boolean running = false;
	private static String[] dict = {"heeey", "how are you", "wyd", "ciao", "fml :/", "who wants to play fortnite with me?", "the answer is darude sandstorm :D"};
	
	public static void spamThis(ChatListener listener) {
		running = true;
		spammer = new Thread(new Runnable() {
			@Override
			public void run() {
				while(running) {
					try {
						Thread.sleep(randInt(500, 2000));
						listener.onMessageSend(dict[randInt(0, dict.length)]);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		spammer.start();
	}
	
	public static void stopIt() {
		running = false;
	}
	
	private static int randInt(int min, int max) {
		return (int)(Math.random()*(max-min)+min);
	}
}
