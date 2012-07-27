package devN.games;

import java.util.ArrayList;
import java.util.List;
import devN.games.agonia.AgoniaAI;

public class Player implements AgoniaAI
{
	private static int cIDs = 0;

	protected Deck deck;
	protected String name;
	protected List<Card> hand;
	protected List<Card> owned;
	protected CardGame game;
	protected int score;
	protected int team;
	protected int id;
	
	protected AgoniaAI ai;

	public Player(String name, int team, Deck deck)
	{
		this(name, team);
		this.deck = deck;
	}
	
	public Player(String name, int team)
	{
		this.id = ++cIDs;
		this.name = name;
		this.score = 0;
		this.team = team;
		this.hand = new ArrayList<Card>();
		this.owned = new ArrayList<Card>(); 
	}
	
	public Player(String name, int team, CardGame game)
	{
		this(name, team);
		this.game = game;
	}

	public void addScore(int points)
	{
		this.score += points;
	}

	public void draw()
	{
		if (deck.hasCards())
		{
			hand.addAll(deck.draw());
		}
	}
	
	public void draw(int n)
	{
		hand.addAll(deck.draw(n));
	}
	
	public void draw(List<Card> cards)
	{
		hand.addAll(deck.draw(cards));
	}
	
	public boolean playCard(List<Card> cards)
	{
		if (hand.containsAll(cards))
		{
			hand.removeAll(cards);
			
			return true;
		}
		return false;
	}
	
	public boolean playCard(Card c)
	{
		if (hand.contains(c))
		{
			hand.remove(c);
			
			return true;
		}
		return false;
	}
	
	/**
	 * @return the deck
	 */
	public Deck getDeck()
	{
		return deck;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * @return the hand
	 */
	public List<Card> getHand()
	{
		return new ArrayList<Card>(this.hand);
	}

	/**
	 * @return the score
	 */
	public int getScore()
	{
		return this.score;
	}

	/**
	 * @return the team
	 */
	public int getTeam()
	{
		return this.team;
	}

	/**
	 * @return the id
	 */
	public int getId()
	{
		return this.id;
	}
	
	public AgoniaAI getAI()
	{
		return ai;
	}

	/**
	 * @return the game
	 */
	public CardGame getGame()
	{
		return game;
	}

	/**
	 * @param deck the deck to set
	 */
	public void setDeck(Deck deck)
	{
		this.deck = deck;
	}

	/**
	 * @param game the game to set
	 */
	public void setGame(CardGame game)
	{
		this.game = game;
	}

	public boolean playCard(int i)
	{
		if (i >= 0 && i < hand.size())
		{
			hand.remove(i);
			
			return true;
		}
		return false;
	}
	
	public Card getCard(int i)
	{
		if (i < 0 || i >= hand.size())
		{
			return new Card(-1, -1);
		}
		Card c = hand.get(i);
		
		return new Card(c.getSuit(), c.getRank());
	}
	
	public boolean hasCards()
	{
		return !hand.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return name + " | " + hand.size() + " cards | score: " + this.score;
	}
	
	public String handString()
	{
		StringBuilder builder = new StringBuilder(hand.size() * 5);
		
		for(Card c : hand)
		{
			builder.append(c.toString() + " ");
		}
		
		return builder.toString();
	}

	/**
	 * @return the owned
	 */
	public List<Card> getOwned()
	{
		return owned;
	}

	/**
	 * @param owned the owned to set
	 */
	public void setOwned(List<Card> owned)
	{
		this.owned = owned;
	}
	
	public void addOwned(List<Card> c)
	{
		this.owned.addAll(c);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}

		if (!(o instanceof Player))
		{
			return false;
		}

		Player that = (Player) o;

		return (this.getId() == that.getId());
	}

	
	public void setAI(AgoniaAI ai)
	{
		this.ai = ai;
	}

	@Override
	public Card willPlay()
	{
		return ai.willPlay();
	}

	/* (non-Javadoc)
	 * @see devN.games.agonia.AgoniaAI#getAceSuit()
	 */
	@Override
	public int getAceSuit()
	{
		return ai.getAceSuit();
	}

	@Override
	public Card playSeven()
	{
		return ai.playSeven();
	}

//	@Override
//	public Card askFor(int suit, int rank)
//	{
//		return ai.askFor(suit, rank);
//	}
}
