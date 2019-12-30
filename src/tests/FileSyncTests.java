package tests;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import main.ChatMessage;
import storage.FileSyncManager;

class FileSyncTests {

	@Test
	void saveAndLoad() {
		// initialize and add arbitrary messages
		FileSyncManager.initBlank();
		FileSyncManager.addMessage(new ChatMessage(3, "Lorem Ip|sum xyz", "192.168.178.51-2314", System.currentTimeMillis()));
		FileSyncManager.addMessage(new ChatMessage(3, "Lorem Ip-sum xyz", "192.168.178.52-2314", System.currentTimeMillis()));
		FileSyncManager.addMessage(new ChatMessage(3, "Lorem Ip-sum x|yz", "192.168.178.51-2314", System.currentTimeMillis()));
		FileSyncManager.addMessage(new ChatMessage(3, "Lorem Ipsum xyz", "192.168.178.51-2315", System.currentTimeMillis()));
		String id = "192.168.178.51-2314";
		List<ChatMessage> originalMessages = FileSyncManager.getMessages();

		// save and load messages for comparison
		FileSyncManager.save(id);
		FileSyncManager.initFromFile(id);
		List<ChatMessage> loadedMessages = FileSyncManager.getMessages();

		if (originalMessages.size() != loadedMessages.size()) {
			fail("not same size lists");
		} else {
			// for each message in the lists, compare each field
			for (int i = 0; i < originalMessages.size(); i++) {
				ChatMessage orig = originalMessages.get(i);
				ChatMessage loaded = loadedMessages.get(i);
				assertEquals(orig.getId(), loaded.getId());
				assertEquals(orig.getSender(), loaded.getSender());
				assertEquals(orig.getText(), loaded.getText());
			}
		}
	}

}
