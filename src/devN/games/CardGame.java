package devN.games;

import java.util.ArrayList;
import java.util.List;

public abstract class CardGame
{
	protected List<Card> SPECIAL_CARDS;
	
	protected int START_DEAL_COUNT;
	protected int STACK_TOP_COUNT;
	protected Deck deck;
	protected Player p1, p2;
	protected List<Card> stackTop;
	
	/**
	 * @param d
	 * @param p1
	 * @param p2
	 * @param startDeal
	 * @param stackTopSize
	 */
	public CardGame(Deck d, Player p1, Player p2, int startDeal, int stackTopSize)
	{
		SPECIAL_CARDS = new ArrayList<Card>();
		setSpecialCards();

		START_DEAL_COUNT = startDeal;
		STACK_TOP_COUNT = stackTopSize;
		deck = d;
		this.p1 = p1;
		this.p2 = p2;
		this.p1.setDeck(deck);
		this.p2.setDeck(deck);
		this.p1.setGame(this);
		this.p2.setGame(this);
		
		deck.shuffle();
		
		p1.draw(START_DEAL_COUNT);
		p2.draw(START_DEAL_COUNT);
		stackTop = deck.draw(STACK_TOP_COUNT);		
	}

	/**
	 * @param d
	 * @param p1
	 * @param p2
	 * @param startDeal
	 * @param stackTopCards
	 */
	public CardGame(Deck d, Player p1, Player p2, int startDeal, List<Card> stackTopCards)
	{
		SPECIAL_CARDS = new ArrayList<Card>();
		setSpecialCards();
		START_DEAL_COUNT = startDeal;
		deck = d;
		this.p1 = p1;
		this.p2 = p2;
		this.p1.setDeck(deck);
		this.p2.setDeck(deck);
		
		deck.shuffle();

		stackTop = deck.draw(stackTopCards);

		STACK_TOP_COUNT = stackTop.size();

		p1.draw(START_DEAL_COUNT);
		p2.draw(START_DEAL_COUNT);
	}
	
	public abstract void handleSpecial(Player p, Card c);
	
	public abstract boolean canPlay(Card c);
	
	public abstract boolean canPlay(Card c, int stackTopIndex);
	
	public abstract boolean canDraw(Player p);
	
	public abstract void draw(Player p);
	
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
		if (p.hasCards() && canPlay(c))
		{
			p.playCard(c);
		}
	}
}
