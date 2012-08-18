package devN.games.agonia;

import devN.games.AIPlayer;
import devN.games.Card;

public interface AgoniaAI extends AIPlayer
{
	public static final int MODE_MONKEY 	= 0;
	public static final int MODE_EASY 		= 1;
	public static final int MODE_MODERATE 	= 2;
	public static final int MODE_MEGAMIND 	= 3;
	
	public int getAceSuit();
	
	public Card playSeven();
}
