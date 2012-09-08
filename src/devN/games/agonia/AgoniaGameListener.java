package devN.games.agonia;

import devN.games.GameListener;

public interface AgoniaGameListener extends GameListener
{
	public void onDeckFinish(int DeckFinishOption);
}
