package devN.games.gamestub;

public interface RealTimeReliableMessageSentListener
{
	void onRealTimeMessageSent(int statusCode, int token, String recipientParticipantId);
}
