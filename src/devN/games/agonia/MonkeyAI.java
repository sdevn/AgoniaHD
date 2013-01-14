package devN.games.agonia;

import java.util.Random;
import devN.games.Card;
import devN.games.Player;

public class MonkeyAI implements AgoniaAI
{
	private Random rand = new Random();
	private Player me;
	
	public MonkeyAI(Player p)
	{
		me = p;
	}

	@Override
	public Card willPlay()
	{
		for (Card c : me.getHand())
		{
			if (me.getGame().canPlay(c))
			{
				return new Card(c);
			}
		}

		return Card.NULL_CARD;
	}

	@Override
	public int getAceSuit()
	{
		return rand.nextInt(Card.MAX_SUIT);
	}

	@Override
	public Card playSeven()
	{
		for (Card c : me.getHand())
		{
			if (c.getRank() == 7)
			{
				return new Card(c);
			}
		}

		return Card.NULL_CARD;
	}

	@Override
	public int getMode()
	{
		return MODE_MONKEY;
	}
}