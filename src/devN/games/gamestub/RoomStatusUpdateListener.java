package devN.games.gamestub;

import java.util.List;

public interface RoomStatusUpdateListener
{
	void onRoomConnecting(Room room);

	void onRoomAutoMatching(Room room);

	void onPeerInvitedToRoom(Room room, List<String> participantIds);

	void onPeerDeclined(Room room, List<String> participantIds);

	void onPeerJoined(Room room, List<String> participantIds);

	void onPeerLeft(Room room, List<String> participantIds);

	void onConnectedToRoom(Room room);

	void onDisconnectedFromRoom(Room room);

	void onPeersConnected(Room room, List<String> participantIds);

	void onPeersDisconnected(Room room, List<String> participantIds);
}
