package devN.games.gamestub;

public interface Participant
{
	String getParticipantId();

	String getDisplayName();

	boolean isConnectedToRoom();
}
