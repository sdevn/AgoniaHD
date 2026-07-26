package devN.games.gamestub;

import android.content.Intent;

/** Offline no-op replacement for the old play-services GamesClient. */
public class GamesClient
{
	public boolean isConnected()
	{
		return false;
	}

	public GamesPlayer getCurrentPlayer()
	{
		return new GamesPlayer();
	}

	public String getCurrentPlayerId()
	{
		return "";
	}

	public Intent getLeaderboardIntent(String leaderboardId)
	{
		return null;
	}

	public void signOut(OnSignOutCompleteListener listener)
	{
		if (listener != null)
		{
			listener.onSignOutComplete();
		}
	}

	public void submitScore(String leaderboardId, long score)
	{ }

	public void sendReliableRealTimeMessage(RealTimeReliableMessageSentListener listener,
											byte[] message, String roomId, String participantId)
	{ }

	public void createRoom(RoomConfig config)
	{ }

	public void leaveRoom(RoomUpdateListener listener, String roomId)
	{ }
}
