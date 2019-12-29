package main;

public enum MessageType {
	Heartbeat,
	NewMessageForwardedToLeader,
	NewMessageToCache,
	MessageCached,
	WriteMessage,
	Vote,
	RequestVoteForMe,
	AlreadyVoted,
	IAmTheSenat, //New Leader sends to all this that they add them as new leader
	RequestFullMessageHistoryFromAnotherNode,
	RequestActiveClientsListFromAnotherNode,
	NewClientInPhonebookSyncronizeWithAllClients //TODO: HOW TO DO THIS
	
}
