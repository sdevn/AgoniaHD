package devN.games.gamestub;

/** Offline no-op replacement for the old play-services AppStateClient (cloud save). */
public class AppStateClient
{
	public static final int STATUS_OK = 0;
	public static final int STATUS_STATE_KEY_NOT_FOUND = 2002;

	public void loadState(OnStateLoadedListener listener, int stateKey)
	{ }

	public void updateState(int stateKey, byte[] data)
	{ }

	public void resolveState(OnStateLoadedListener listener, int stateKey, String resolvedVersion, byte[] data)
	{ }
}
