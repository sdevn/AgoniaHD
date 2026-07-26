package devN.games.gamestub;

public interface RealTimeMessage
{
	byte[] getMessageData();

	String getSenderParticipantId();
}
