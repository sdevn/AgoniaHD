package devN.games.agonia;

import devN.games.GameListener;
import devN.games.Player;

public interface AgoniaGameListener extends GameListener
{
	public void onAceSuitSelected(int selectedSuit);
	
	public void onPasso(Player who);
	
	/** v2.4 added to implement game history dialog */
	public void onDeckFinished(int deckFinishOption);
}
