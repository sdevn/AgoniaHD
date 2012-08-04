package devN.games.agonia;

import java.util.Arrays;
import java.util.List;
import devN.games.Card;
import devN.games.CardGame;
import devN.games.Deck;
import devN.games.Player;

public class Agonia extends CardGame
{
	protected final static int DRAW_WITH_SEVEN = 2;
	public final static int START_DEAL_COUNT = 7;
	protected final static int ACE_SCORE = 20;
	
	protected Player lastDraw; // Last player who draw card (non-Special draw)
	protected Player turn;
	protected boolean isNineSpecial;
	
	public Agonia(Deck d, Player p, Player cp)
	{
		super(d, p, cp, 
		      START_DEAL_COUNT, 	
		      1); 	// STACK_TOP_COUNT
		
		turn = p;
		lastDraw = null;
		isNineSpecial = F.getBoolean(F.KEY_IS_NINE_SPECIAl, true);
	}

	@Override
	public boolean canPlay(Card c)
	{
		Card top = getTop(0);

		if (top.getRank() == 1 && c.getRank() == 1)
		{
			return false;
		}

		if (c.sameSuit(top) || c.sameRank(top) || c.getRank() == 1)
		{
			return true;
		}
	
		return false;
	}

	private void handleAce(Card c)
	{
		throw new AcePlayed();
	}

	private void handleEight()
	{
		switchTurn();
	}

	private void handleNine()
	{
		if (isNineSpecial)
		{
		    switchTurn();
		}
	}

	private void handleSeven(Card c)
	{
		throw new SevenPlayed(c.getSuit());
	}

	public void handleSpecial(Player p, Card c)
	{
		switch (c.getRank())
		{
			case 1:
			{
				handleAce(c);				
				break;
			}
			case 7:
			{
				handleSeven(c);
				break;
			}
			case 8:
			{
				handleEight();
				break;
			}
			case 9:
			{
				handleNine();
				break;
			}
		}
	}

	@Override
	public boolean canPlay(Card c, int stackTopIndex)
	{
		if (this.STACK_TOP_COUNT() < stackTopIndex)
		{
			return false;
		}

		return canPlay(c);
	}

	@Override
	public boolean canDraw(Player p)
	{
		return !p.equals(lastDraw) && p.equals(turn);
	}

	@Override
	public void setSpecialCards()
	{
		Card[] c =
			{ 
			// Ace
			new Card(Card.iCLUBS, 1), new Card(Card.iDIAMONDS, 1),
			new Card(Card.iHEARTS, 1), new Card(Card.iSPADES, 1),

			// Seven
			new Card(Card.iCLUBS, 7), new Card(Card.iDIAMONDS, 7),
			new Card(Card.iHEARTS, 7), new Card(Card.iSPADES, 7),

			// Eight
			new Card(Card.iCLUBS, 8), new Card(Card.iDIAMONDS, 8),
			new Card(Card.iHEARTS, 8), new Card(Card.iSPADES, 8),

			// Nine
			new Card(Card.iCLUBS, 9), new Card(Card.iDIAMONDS, 9),
			new Card(Card.iHEARTS, 9), new Card(Card.iSPADES, 9)
			};

		this.SPECIAL_CARDS.addAll(Arrays.asList(c));
	}

	@Override
	public void draw(Player p)
	{
		lastDraw = p;
		p.draw();
	}

	/* (non-Javadoc)
	 * @see devN.games.CardGame#play(devN.games.Player, devN.games.Card)
	 */
	@Override
	public void play(Player p, Card c)
	{
		super.play(p, c);
				
		if (!Card.NULL_CARD.equals(c))
		{
			getTop(0).setRank(c.getRank());
			getTop(0).setSuit(c.getSuit());
			
			if (isSpecial(c))
			{
				handleSpecial(p, c);
			}
		}
		switchTurn();
		//TODO: Other exception should throwed here (in the end of methode)
		if (p.getHand().isEmpty()
		&& getTop(0).getRank() != 8
		&& (getTop(0).getRank() != 9 || !isNineSpecial)
		&& getTop(0).getRank() != 1)
		{
			throw new GameFinished();
		}
	}
	
	public Player whoPlayNext()
	{
		if (turn.equals(super.p1))
		{
			return super.p2;
		}
		return super.p1;
	}
	
	public void switchTurn()
	{
		turn = whoPlayNext();
		lastDraw = null;
	}
	
	public int scoreOf(List<Card> cards) 
	{
		int score = 0;
		
		for (Card c : cards) 
		{
			if (c.getRank() == 1) 
			{
				score += ACE_SCORE;
			} 
			else 
			{
				score += c.getValue();
			}
		}
		
		return score;
		
	}
}
