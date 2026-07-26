package devN.games.gamestub;

import android.app.Activity;

/**
 * Offline stand-in for the deprecated BaseGameUtils BaseGameActivity
 * (Google Play Game Services). Never signs in: beginUserInitiatedSignIn()
 * reports failure immediately and the stub clients are permanently
 * disconnected no-ops, so all online UI paths stay dormant.
 */
public abstract class BaseGameActivity extends Activity
{
	public static final int CLIENT_NONE		= 0;
	public static final int CLIENT_GAMES	= 1;
	public static final int CLIENT_PLUS		= 2;
	public static final int CLIENT_APPSTATE	= 4;
	public static final int CLIENT_ALL		= CLIENT_GAMES | CLIENT_PLUS | CLIENT_APPSTATE;

	private final GamesClient gamesClient = new GamesClient();
	private final AppStateClient appStateClient = new AppStateClient();

	protected BaseGameActivity()
	{ }

	protected BaseGameActivity(int requestedClients)
	{ }

	protected GamesClient getGamesClient()
	{
		return gamesClient;
	}

	protected AppStateClient getAppStateClient()
	{
		return appStateClient;
	}

	protected boolean isSignedIn()
	{
		return false;
	}

	protected void beginUserInitiatedSignIn()
	{
		onSignInFailed();
	}

	public void onSignInFailed()
	{ }

	public void onSignInSucceeded()
	{ }
}
