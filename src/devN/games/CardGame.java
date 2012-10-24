package devN.games;

import java.util.ArrayList;
import java.util.List;

public abstract class CardGame
{
	public final static int DONT_DEAL = -1;
	
	protected List<Card> SPECIAL_CARDS;
	
	protected int START_DEAL_COUNT;
	protected int STACK_TOP_COUNT;
	protected Deck deck;
	protected Player p1, p2;
	protected List<Card> stackTop;
	protected List<GameListener> listeners = new ArrayList<GameListener>();
	
	public CardGame(Deck d, Player p1, Player p2, int startDeal, int stackTopSize)
	{
//		startDeal = DONT_DEAL;
//		
		init(d, p1, p2, startDeal);
//
//		List<Card> debugDraw = new ArrayList<Card>();
//		debugDraw.add(new Card(0, 7));
//		debugDraw.add(new Card(1, 7));
//		
//		p1.draw(debugDraw);
//		
//		debugDraw = new ArrayList<Card>();
//		debugDraw.add(new Card(2, 7));
//		debugDraw.add(new Card(3, 7));
//		p2.draw(debugDraw);
//		
//		p1.draw(5);
//		p2.draw(5);

		STACK_TOP_COUNT = stackTopSize;		
		
		if (startDeal == DONT_DEAL) 
		{
			stackTop = new ArrayList<Card>();
			for (int i = 0; i < stackTopSize; i++)
			{
				stackTop.add(deck.draw().get(0));
			}
		}
		else 
		{
			stackTop = deck.draw(STACK_TOP_COUNT);
		}
	}

	public CardGame(Deck d, Player p1, Player p2, int startDeal, List<Card> stackTopCards)
	{
		stackTop = d.draw(stackTopCards);
		STACK_TOP_COUNT = stackTop.size();
		
		init(d, p1, p2, startDeal);
	}
	
	private void init(Deck d, Player p1, Player p2, int startDeal)
	{
		SPECIAL_CARDS = new ArrayList<Card>();
		setSpecialCards();
		START_DEAL_COUNT = startDeal;
		deck = d;
		this.p1 = p1;
		this.p2 = p2;
		
		this.p1.setDeck(deck);
		this.p2.setDeck(deck);
		
		this.p1.setGame(this);
		this.p2.setGame(this);
		
		deck.shuffle();
		
		if (startDeal != DONT_DEAL)
		{
			p1.draw(START_DEAL_COUNT);
			p2.draw(START_DEAL_COUNT);
		}
	}
	
	public abstract void handleSpecial(Player p, Card c);
	
	public abstract boolean canPlay(Card c);
	
	public abstract boolean canPlay(Card c, int stackTopIndex);
	
	public abstract boolean canDraw(Player p);
	
	public abstract void draw(Player p);
	
	public abstract void draw(Player p, int n);
	
	/**
	 * 
	 * @param specialOp means that this draw is not follows the regular rules. (maybe
	 * 			caused by a special card)
	 */
	public abstract void draw(Player p, int n, boolean specialOp);
	
	public abstract void setSpecialCards();
	
	public boolean isSpecial(Card c)
	{
		return SPECIAL_CARDS.contains(c);
	}

	/**
	 * @return the deck
	 */
	public Deck getDeck()
	{
		return deck;
	}

	/**
	 * @return the top card
	 */
	public List<Card> getStackTop()
	{
		return stackTop;
	}
	
	public Card getTop(int index)
	{
		return stackTop.get(index);
	}

	/**
	 * @return the player
	 */
	public Player getPlayer(int id)
	{
		if (id == p1.getId())
		{
			return p1;
		}
		if (id == p2.getId())
		{
			return p2;
		}
		return null;
	}

	/**
	 * @param top
	 *            the top card to set
	 */
	public void setTop(List<Card> top)
	{
		this.stackTop = top;
	}
	
	public void setTop(int index, Card c)
	{
		stackTop.set(index, c);
	}

	/**
	 * @return the sTART_DEAL_COUNT
	 */
	public int START_DEAL_COUNT()
	{
		return START_DEAL_COUNT;
	}

	/**
	 * @return the sTACK_TOP_COUNT
	 */
	public int STACK_TOP_COUNT()
	{
		return STACK_TOP_COUNT;
	}

	/**
	 * @param deck the deck to set
	 */
	public void setDeck(Deck deck)
	{
		this.deck = deck;
		this.p1.setDeck(deck);
		this.p2.setDeck(deck);
	}
	
	public void play(Player p, Card c)
	{
		boolean has, can;
		
		has = p.hasCards();
		can = canPlay(c);
		
		if (has && can)
		{
			p.playCard(c);
		}
	}
	
	public void addGameListener(GameListener listener)
	{
		listeners.add(listener);
	}
}
