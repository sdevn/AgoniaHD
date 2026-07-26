package devN.games.gamestub;

public interface OnStateLoadedListener
{
	void onStateLoaded(int statusCode, int stateKey, byte[] localData);

	void onStateConflict(int stateKey, String resolvedVersion, byte[] localData, byte[] serverData);
}
