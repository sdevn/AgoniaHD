package devN.games.agonia;

import devN.games.GameListener;
import devN.games.Player;

public interface AgoniaGameListener extends GameListener
{
	public void onAceSuitSelected(int selectedSuit);
	
	public void onPasso(Player who);
}
