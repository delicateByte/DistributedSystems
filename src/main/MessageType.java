package main;

public enum MessageType {
	Heartbeat,
	NewMessageForwardedToLeader,
	NewMessageToCache,
	MessageCached,
	WriteMessage,
	Vote,
	RequestVoteForMe,
	AlreadyVoted
}
