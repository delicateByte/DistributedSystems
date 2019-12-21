package GUI;

public interface ChatListener {
	
	/**
	 * implements the send logic
	 * @param message the message that the GUI wants to send
	 * @return boolean that indicates if sending was successful.
	 */
	public boolean onMessageSend(String message);
	
	/**
	 * called when the user closes the GUI window
	 */
	public void onWindowClose();
}
