package devN.games.agonia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import devN.games.Card;
import devN.games.CardGame;
import devN.games.Deck;
import devN.games.Player;

public class Agonia extends CardGame implements Serializable
{
	private static final long serialVersionUID = 5007865192211492281L;
	protected final static int DRAW_WITH_SEVEN = 2;
	public final static int START_DEAL_COUNT = 7;
	private final static int ACE_SCORE = 20;
	private final static int DECK_FINISH_RECYCLE = 0;
	private final static int DECK_FINISH_PICK_NEW = 1; 
	private final static int DECK_FINISH_FINISH_GAME = 2;
	
	protected Player lastDraw; // Last player who draw card (non-Special draw)
	protected Player turn;
	protected boolean isNineSpecial;
	protected boolean isGameFinished = false;
	private int deckFinishOption;
	
	public Agonia(Deck d, Player p, Player cp)
	{
		super(d, p, cp, 
		      START_DEAL_COUNT, 	
		      1); 	// STACK_TOP_COUNT
	
		init();
	}
	
	public Agonia(Deck d, Player p, Player cp, int customInitDeal)
	{
		super(d, p, cp, 
			customInitDeal, 	
			1); 	// STACK_TOP_COUNT

		init();
	}

	private void init()
	{
		turn = p1;
		lastDraw = null;
		isNineSpecial = F.getBoolean(F.KEY_IS_NINE_SPECIAl, true);
		deckFinishOption = Integer.parseInt(F.getString(F.KEY_DECK_FINISH, "0"));
	}

	@Override
	public boolean canPlay(Card c)
	{
		Card top = getTop(0);
		
		if (isGameFinished)
		{
			return false;
		}

		if (c.getRank() == 1 && (top.getRank() == 1 || turn.getHand().size() == 1))
		{// ohi Ace se Ace kai ohi Ace os teleutaio filo
			return false;
		}

		if (c.sameSuit(top) || c.sameRank(top) || c.getRank() == 1)
		{
			return true;
		}
	
		return false;	// NULL_CARD ?
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
		return !p.equals(lastDraw) && p.equals(turn) && !isGameFinished;
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
		onDrawFromDeck();
	}
	
	@Override
	public void draw(Player p, int n)
	{
		lastDraw = p;
		p.draw(n);
		onDrawFromDeck();
	}
	
	private void onDrawFromDeck()
	{
		if (deck.isEmpty())
		{
			switch (deckFinishOption)
			{
			case DECK_FINISH_RECYCLE:
				{
				Deck recycled = new Deck();
				List<Card> top = new ArrayList<Card>(1);
				top.add(getTop(0));
				recycled.draw(p1.getHand());
				recycled.draw(p2.getHand());
				recycled.draw(top);
				
				deck.put(recycled.deck(), true);
				}
				break;

			case DECK_FINISH_PICK_NEW:
				{
				deck.put(new Deck().deck(), true);	
				}
				break;
			
			case DECK_FINISH_FINISH_GAME:
				{
				finishGame();
				}
				break;

			default:
				break;
			}
		}
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
		&& (getTop(0).getRank() != 9 || !isNineSpecial))
		{
			finishGame();
		}
	}
	
	private void finishGame()
	{
		isGameFinished = true;
		throw new GameFinished();		
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

	public boolean isGameFinished()
	{
		return isGameFinished;
	}
}
