package devN.games;

import java.util.List;

public interface GameListener
{
	/**
	 * Informs the listener with initial game parameters
	 * @param infos game-specific infos to be passed
	 */
	public void initGameParams(int cPlayers, List<Card> stackTopCards);
	
	/**
	 * @param n
	 * 		how many cards are drawed
	 */
	public void onDrawFromDeck(Player who, int n);
	
	public void onPlay(Player who, Card c);

	public void onPlay(Player who, List<Card> cards);
	
	public void onPlay(Player who, int cCards);
}
