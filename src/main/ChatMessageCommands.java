package main;

public enum ChatMessageCommands {
	Heartbeat,
	NewMessageForwardedToLeader,
	NewMessageToCache,
	MessageCached,
	WriteMessage,
	Vote,
	RequestVoteForMe,
	AlreadyVoted,
	IamTheSenat, //New Leader sends to all this that they add them as new leader
	RequestFullMessagesFromAnotherNode
}
