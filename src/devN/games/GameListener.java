package devN.games;

import java.util.List;

public interface GameListener
{
	/**
	 * Informs the listener with initial game parameters
	 * @param infos game-specific infos to be passed
	 */
	public void initGameParams(int cPlayers, List<Card> stackTopCards);
	
	public void onGameFinished(List<Player> players);

	/**
	 * @param n
	 * 		how many cards are drawed
	 */
	public void onDrawFromDeck(Player who, int n);
	
	public void onPlay(Player who, Card c);

	public void onPlay(Player who, List<Card> cards);
	
	/**
	 * Use it only when the played cards is hidden from other players
	 */
	public void onPlay(Player who, int cCards);
	
	public void onSwitchTurn(Player prev, Player cur);
}
