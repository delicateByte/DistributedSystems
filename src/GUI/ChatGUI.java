package GUI;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatGUI extends JFrame{
	private String uuid;
	private String message;
	
	private JPanel container = new JPanel();
	private JTextField chatField = new JTextField();
	private JTextArea chatDisplay = new JTextArea();
	
	private ChatListener listener;
	
	// constructor that is used when no location is specified
	public ChatGUI(String uuid) {
		super();
		this.uuid = uuid;
		this.message = message;
		
		// Some sweet Java swing GUI shit
		this.setTitle("Distributed Systems Chat");
		this.setSize(600, 400);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		
		container.setLayout(new BorderLayout());
		container.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		chatDisplay.setEditable(false);
		container.add(chatDisplay, BorderLayout.CENTER);
		container.add(chatField, BorderLayout.PAGE_END);
		
		this.getContentPane().add(container);
		this.setVisible(true);
		
		// add chatField listener
		chatField.addActionListener(new ActionListener() {
			// on enter press in chatField
			@Override
			public void actionPerformed(ActionEvent e) {
				if(listener != null) {
					boolean success = listener.onMessageSend(chatField.getText());
					if(success) {
						chatField.setText("");
					} else {
						JOptionPane.showMessageDialog(null, "Please try again later.");
					}
				} else {
					JOptionPane.showMessageDialog(null, "Please try again later.");
				}
			}
		});
		
		// add window close listener
		this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            	if(listener != null) {
            		listener.onWindowClose();
            	}
            }
        });
		
		//TODO: Remove the test displays
		this.updateChat("[13:37 | Leethacker] Wer hat Bock zu zocken?");
		this.addChat("[13:37 | .xXUltimatePussyDestroyer|Xx.] Ich bin dabei");
	}
	
	// extended constructor when location is specified (uses the default constructor)
	public ChatGUI(String uuid, int locX, int locY, int width, int height) {
		this(uuid);
		this.setLocation(locX, locY);
		this.setSize(width, height);
	}
	
	/**
	 * updates the text
	 * @param newChat the new string that the TextArea should contain
	 */
	public void updateChat(String newChat) {
		chatDisplay.setText(newChat);
	}
	
	/**
	 * adds a new line to the current chat history
	 * @param newLine the line (without the \n) that should be added
	 */
	public void addChat(String newLine) {
		chatDisplay.setText(chatDisplay.getText() + "\n" + newLine);
	}
	
	
	/**
	 * displays a message in a popup
	 * @param message message to be displayed
	 */
	public void displayMessage(String message) {
		JOptionPane.showMessageDialog(null, message);
	}
	
	/**
	 * call this once after instantiating a ChatGUI and the ChatGUI will send 
	 * new events to the listener
	 * @param listener listener that the event should be sent to
	 */
	public void registerListener(ChatListener listener) {
		this.listener = listener;
		listener.onMessageSend(message);
	}
}
