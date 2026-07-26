package devN.games.gamestub;

import java.util.List;

public interface Room
{
	String getRoomId();

	List<Participant> getParticipants();

	String getParticipantId(String playerId);
}
