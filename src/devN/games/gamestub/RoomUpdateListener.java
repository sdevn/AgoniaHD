package devN.games.gamestub;

public interface RoomUpdateListener
{
	void onRoomCreated(int statusCode, Room room);

	void onJoinedRoom(int statusCode, Room room);

	void onLeftRoom(int statusCode, String roomId);

	void onRoomConnected(int statusCode, Room room);
}
