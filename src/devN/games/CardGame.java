package devN.games;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class CardGame
{
	public final static int DONT_DEAL = -1;
	
	protected List<Card> SPECIAL_CARDS;
	
	protected int START_DEAL_COUNT;
	protected int STACK_TOP_COUNT;
	protected Deck deck;
	protected List<Player> players = new ArrayList<Player>();;
	protected List<Card> stackTop;
	
	/** v2.4b changed to Set<> to prevent duplicates */
	protected static Set<GameListener> gameListeners = new HashSet<GameListener>();
	
	public CardGame(Deck d, List<Player> players, int startDeal, int stackTopSize)
	{
//		startDeal = DONT_DEAL;
		
		init(d, players, startDeal);

//		List<Card> debugDraw = new ArrayList<Card>();
//		debugDraw.add(new Card(0, 7));
//		debugDraw.add(new Card(1, 7));
//		debugDraw.add(new Card(2, 7));
//		debugDraw.add(new Card(3, 7));
//
//		players.get(0).draw(debugDraw.subList(0, 3));
//		players.get(1).draw(debugDraw);
		
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
		
		informInit(stackTop);
	}

	public CardGame(Deck d, List<Player> players, int startDeal, List<Card> stackTopCards)
	{
		stackTop = d.draw(stackTopCards);
		STACK_TOP_COUNT = stackTop.size();
		
		init(d, players, startDeal);
		
		informInit(stackTop);
	}
	
	private void init(Deck d, List<Player> players, int startDeal)
	{
		SPECIAL_CARDS = new ArrayList<Card>();
		setSpecialCards();
		START_DEAL_COUNT = startDeal;
		deck = d;
		this.players = players;
		deck.shuffle();

		for (Player p : players)
		{
			p.setDeck(deck);
			p.setGame(this);
		}

		if (startDeal != DONT_DEAL)
		{
			for (Player p : players)
			{
				draw(p, START_DEAL_COUNT, true);
			}
		}
	}
	
	protected void informInit(List<Card> stackTopCards)
	{
		for (GameListener listener : gameListeners)
		{
			listener.initGameParams(players.size(), stackTopCards);
		}
	}
	
	protected void informDraw(Player who, int n)
	{
		for (GameListener listener : gameListeners)
		{
			listener.onDrawFromDeck(who, n);
		}
	}
	
	protected void informPlay(Player who, Card c)
	{
		for (GameListener listener : gameListeners)
		{
			listener.onPlay(who, c);
		}
	}
	
	protected void informPlay(Player who, List<Card> cards)
	{
		for (GameListener listener : gameListeners)
		{
			listener.onPlay(who, cards);
		}
	}
	
	protected void informPlay(Player who, int cCards)
	{
		for (GameListener listener : gameListeners)
		{
			listener.onPlay(who, cCards);
		}
	}
	
	protected void informGameFinish()
	{
		for (GameListener listener : gameListeners)
		{
			listener.onGameFinished(players);
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
	
	/** v2.3 */
	public abstract Player getWinner();
	
	public boolean isSpecial(Card c)
	{
		return SPECIAL_CARDS.contains(c);
	}

	public Deck getDeck()
	{
		return deck;
	}

	public List<Card> getStackTop()
	{
		return stackTop;
	}
	
	public Card getTop(int index)
	{
		return stackTop.get(index);
	}

	public Player getPlayerById(int id)
	{
		for (Player p : players)
		{
			if (p.getId() == id)
			{
				return p;
			}
		}
		
		return null;
	}

	public List<Player> getPlayers()
	{
		return players;
	}

	public void setTop(List<Card> top)
	{
		this.stackTop = top;
	}
	
	public void setTop(int index, Card c)
	{
		stackTop.set(index, c);
	}

	public int START_DEAL_COUNT()
	{
		return START_DEAL_COUNT;
	}

	public int STACK_TOP_COUNT()
	{
		return STACK_TOP_COUNT;
	}

	public void setDeck(Deck deck)
	{
		this.deck = deck;
		
		for (Player p : players)
		{
			p.setDeck(deck);
		}
	}
	
	public void play(Player p, Card c)
	{
		boolean has, can;
		
		has = p.hasCards();
		can = canPlay(c);
		
		if (has && can)
		{
			p.playCard(c);
			informPlay(p, c);
		}
	}
	
	public static void addGameListener(GameListener listener)
	{
		gameListeners.add(listener);
	}
	
	public static void unregisterGameListener(GameListener listener)
	{
		gameListeners.remove(listener);
	}

//	/**
//	 * Saved in form:
//	 * [deck] 
//	 * [players_size] [player0] ... [playerN] [stackTop_size] [stackTop_card0] ... [stackTop_cardM]
//	 * 
//	 * where N [players_size] is and M is [stackTop_size]
//	 */
//	@Override
//	public void save(DataOutputStream d) throws IOException
//	{
//		// players
//		d.writeInt(players.size());
//		for (Player p : players)
//		{
//			p.save(d);
//		}
//		
//		// stackTop
//		d.writeInt(stackTop.size());
//		for (Card c : stackTop)
//		{
//			c.save(d);
//		}
//		
//		deck.save(d);
//	}
//
//	@Override
//	public void load(DataInputStream d) throws IOException
//	{
//		int i;
//		List<Player> tempPlayers = new ArrayList<Player>();
//		stackTop = new ArrayList<Card>();
//		
//		// player
//		i = d.readInt();
//		while (i > 0)
//		{
//			tempPlayers.add(new Player(d));
//			--i;
//		}
//		
//		i = 0;
//		for (Player p : tempPlayers)
//		{
//			deck.cards().clear();
//			deck.put(p.getHand(), false);
//			players.get(i).draw(p.getHand());
//			i++;
//		}
//		
//		
//		// stackTop
//		i = d.readInt();
//		while (i > 0)
//		{
//			stackTop.add(new Card(d));
//			--i;
//		}
//		
//		deck.load(d);
//	}
}
