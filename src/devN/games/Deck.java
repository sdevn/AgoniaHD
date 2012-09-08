package devN.games;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck
{
	public static final int SIZE = 52;
	// public static final int SIZE_EX = 54;

	protected List<Card> deck;

	/**
	 * Creates a new deck of cards. The deck is not shuffled.
	 */
	public Deck()
	{
		this.deck = new ArrayList<Card>(SIZE);

		for (int i = 0; i < SIZE; i++)
		{
			this.deck.add(new Card(i % Card.MAX_SUIT, i % Card.MAX_RANK + 1));
		}
	}

	/**
	 * Draw the top card from the deck.
	 */
	public List<Card> draw()
	{
		List<Card> drawed;
		int size;
		
		if (this.deck.isEmpty())
		{
			return new ArrayList<Card>();
		}

		size = this.deck.size();
		
		List<Card> cards = this.deck.subList(size - 1, size);
		drawed = new ArrayList<Card>(cards);
		cards.clear();

		return drawed;
	}

	public List<Card> draw(int n)
	{
		List<Card> drawed;
		int size, toDraw;
		
		if (this.deck.isEmpty() || n == 0)
		{
			return new ArrayList<Card>();
		}

		size = this.deck.size();
		toDraw = (size - n) > 0 ? size - n : 0;
		
		List<Card> cards = this.deck.subList(toDraw, size);
		drawed = new ArrayList<Card>(cards);
		cards.clear();

		return drawed;

	}

	public List<Card> draw(List<Card> cards)
	{
		List<Card> drawed;
		
		if (this.deck.isEmpty())
		{
			return new ArrayList<Card>();
		}
		
		drawed = new ArrayList<Card>();
		for (Card c : cards)
		{
			if (this.deck.contains(c))
			{
				drawed.add(new Card(c));
				this.deck.remove(c);
			}
		}

		return drawed;
	}
	
	public boolean hasCards()
	{
		return !(this.deck.isEmpty());
	}

	public boolean hasCards(int n)
	{
		return this.deck.size() > n;
	}
	
	public boolean isEmpty()
	{
		return deck.isEmpty();
	}

	public int size()
	{
		return this.deck.size();
	}
	
	public void sort()
	{
		Collections.sort(deck);
	}
	
	/**
	 * Shuffles the deck.
	 */
	public void shuffle()
	{
		Collections.shuffle(this.deck);
	}
	
	@Override
	public String toString()
	{
		return "Deck\n" + deck + "\n";
	}

	public void put(Card c, boolean suffleAfter)
	{
		deck.add(c);
		
		if (suffleAfter)
		{
			shuffle();
		}
	}
	
	public void put(List<Card> c, boolean suffleAfter)
	{
		deck.addAll(c);
		
		if (suffleAfter)
		{
			shuffle();
		}
	}

	/**
	 * @return the deck
	 */
	public List<Card> cards()
	{
		return deck;
	}

	/**
	 * @param deck the deck to set
	 */
	public void setCards(List<Card> deck)
	{
		this.deck = deck;
	}

}
