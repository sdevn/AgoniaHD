package devN.games.gamestub;

import android.os.Bundle;

public final class RoomConfig
{
	private RoomConfig()
	{ }

	public static Bundle createAutoMatchCriteria(int minAutoMatchPlayers, int maxAutoMatchPlayers, long exclusiveBitMask)
	{
		return new Bundle();
	}

	public static Builder builder(RoomUpdateListener listener)
	{
		return new Builder();
	}

	public static final class Builder
	{
		public Builder setMessageReceivedListener(RealTimeMessageReceivedListener listener)
		{
			return this;
		}

		public Builder setRoomStatusUpdateListener(RoomStatusUpdateListener listener)
		{
			return this;
		}

		public Builder setAutoMatchCriteria(Bundle criteria)
		{
			return this;
		}

		public RoomConfig build()
		{
			return new RoomConfig();
		}
	}
}
