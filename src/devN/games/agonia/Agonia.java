package devN.games.agonia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import devN.games.Card;
import devN.games.CardGame;
import devN.games.Deck;
import devN.games.GameListener;
import devN.games.Player;

public class Agonia extends CardGame
{
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
	private boolean aceOnAce;
	private boolean aceFinish;
	private int deckFinishOption;

	/**
	 * 
	 * @param firstPlayerId the id of player that was playing first in previous game
	 */
	public Agonia(Deck d, List<Player> players, boolean shufflePlayers, int firstPlayerId)
	{
		super(d, players,
		      START_DEAL_COUNT, 	
		      1); 	// STACK_TOP_COUNT
	
		init(shufflePlayers, firstPlayerId);
	}
	
	public Agonia(Deck d, List<Player> players, int customInitDeal)
	{
		super(d, players, 
			customInitDeal, 	
			1); 	// STACK_TOP_COUNT

		init(false, -1);
	}

	private void init(boolean shufflePlayers, int firstPlayerId)
	{
		if (shufflePlayers)
		{
			initPlayers(-1);
		}
		else 
		{
			initPlayers(firstPlayerId);
		}
		
		turn = players.get(0);
		lastDraw = null;
		deckFinishOption = Integer.parseInt(F.getString(F.KEY_DECK_FINISH, "0"));
		aceOnAce = F.getBoolean(F.KEY_ACE_ON_ACE, false);
		aceFinish = F.getBoolean(F.KEY_ACE_FINISH, false);
	}
	
	private void initPlayers(int firstPlayerId)
	{
		Player prev1st = getPlayerById(firstPlayerId);
		int n = players.size();
		int i = players.indexOf(prev1st); // index of first player

		if (i == -1)
		{
			Random r = new Random();
			i = r.nextInt(n); // index of first player
		}
		else 
		{
			i = (i + 1) % n;
		}

		for (int j = 0; i < n; j++, i++)
		{// the players turn goes anti-clockwise 
			Player p = players.remove(i);
			players.add(j, p);
		}
	}

	@Override
	public boolean canPlay(Card c)
	{
		Card top = getTop(0);
		
		if (isGameFinished)
		{
			return false;
		}

		if (c.getRank() == 1 && top.getRank() == 1
		&& turn.getHand().size() != 1) 
		{// Ace on ace option
			return aceOnAce;
		}
		
		if (c.getRank() == 1 && turn.getHand().size() == 1)
		{// Ace finish option
			return aceFinish && (aceOnAce || top.getRank() != 1);
		}

		if (c.sameSuit(top) || c.sameRank(top) || c.getRank() == 1)
		{
			return true;
		}
	
		return false;	// NULL_CARD ?
	}

	private void handleAce(Player p, Card c)
	{
		if (aceFinish && p.getHand().isEmpty())
		{// game will be finished, no point to pop-up a dialog
			return;
		}
		
		throw new AcePlayed();
	}

	private void handleEight()
	{
		switchTurn(false);
	}

	private void handleNine()
	{
		if (isNineSpecial)
		{
			turn = whoPlayNext();
			lastDraw = null;
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
				handleAce(p, c);				
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
		// prepei na arhikopoieitai edo ki ohi stin init(), logo tou oti
		// h setSpecialCards() kaleite ap ton super constructor, diladi prin
		// tin init()
		isNineSpecial = F.getBoolean(F.KEY_IS_NINE_SPECIAl, true);

		List<Card> specialCards = new ArrayList<Card>();
		
		specialCards.addAll(Arrays.asList(
			// Ace
			new Card(Card.iCLUBS, 1), new Card(Card.iDIAMONDS, 1),
			new Card(Card.iHEARTS, 1), new Card(Card.iSPADES, 1),

			// Seven
			new Card(Card.iCLUBS, 7), new Card(Card.iDIAMONDS, 7),
			new Card(Card.iHEARTS, 7), new Card(Card.iSPADES, 7),

			// Eight
			new Card(Card.iCLUBS, 8), new Card(Card.iDIAMONDS, 8),
			new Card(Card.iHEARTS, 8), new Card(Card.iSPADES, 8)
			));

		if (isNineSpecial)
		{
			specialCards.addAll(Arrays.asList(
				// Nine
				new Card(Card.iCLUBS, 9), new Card(Card.iDIAMONDS, 9),
				new Card(Card.iHEARTS, 9), new Card(Card.iSPADES, 9)
			));
		}
		
		this.SPECIAL_CARDS.addAll(specialCards);
	}

	@Override
	public void draw(Player p)
	{
		draw(p, 1);
	}
	
	@Override
	public void draw(Player p, int n)
	{
		draw(p, n, false);
	}
	
	@Override
	public void draw(Player p, int n, boolean specialOp)
	{
		p.draw(n);
		
		if (!specialOp)
		{
			lastDraw = p;
		}
		else 
		{
			lastDraw = null;
		}

		informDraw(p, n);
		
		onDrawFromDeck();
	}
	
	private void onDrawFromDeck()
	{
		if (deck.isEmpty())
		{
			informDeckFinish();		// v2.4
			
			switch (deckFinishOption)
			{
			case DECK_FINISH_RECYCLE:
				{
				Deck recycled = new Deck();
				List<Card> top = new ArrayList<Card>(1);
				top.add(getTop(0));
				
				for (Player p : players)
				{
					recycled.draw(p.getHand());
				}
				
				recycled.draw(top);

				deck.put(recycled.cards(), true);
				}
				break;

			case DECK_FINISH_PICK_NEW:
				{
				deck.put(new Deck().cards(), true);	
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
		else 
		{
			informPasso(p);
		}
		
		switchTurn();
		//TODO: Other exception should throwed here (in the end of method)
		if (p.getHand().isEmpty()
		&& c.getRank() != 7				// v2.1 added
		&& !Card.NULL_CARD.equals(c)	// v2.1 added
		&& !p.equals(turn))
		{
			finishGame();
		}
	}
	
	public void finishGame()
	{
		isGameFinished = true;
		setPlayerScores();
		informGameFinish();
		throw new GameFinished();		
	}

	public Player whoPlayNext()
	{
		int i = players.indexOf(turn);
		
		if (i == players.size() - 1)
		{
			return players.get(0);
		}
		
		return players.get(i + 1);
	}
	
	public Player whoIsPrev()
	{
		int i = players.indexOf(turn);
		
		if (i == 0)
		{
			return players.get(players.size() - 1);
		}
		
		return players.get(i - 1);
	}
	
	/**
	 * equivalent to switchTurn(true)
	 */
	public void switchTurn()
	{
		turn = whoPlayNext();
		lastDraw = null;
		
		informSwitchTurn(whoIsPrev(), turn);
	}
	
	/**
	 * 
	 * @param toNext if its true, passes the turn to next player,
	 * otherwise to previous 
	 */
	public void switchTurn(boolean toNext)
	{
		if (toNext)
		{
			switchTurn();
		}
		else 
		{
			turn = whoIsPrev();
			lastDraw = null;
		}
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
	
	private void informSwitchTurn(Player prev, Player cur)
	{
		for (GameListener listener : gameListeners)
		{
			listener.onSwitchTurn(prev, cur);
		}
	}
	
	public void informAceSuitSelected(int selectedSuit)
	{
		for (GameListener listener : gameListeners)
		{
			if (listener instanceof AgoniaGameListener)
			{// just to be safe!
				((AgoniaGameListener)listener).onAceSuitSelected(selectedSuit);
		
			}
		}
	}
	
	/** v2.3 */
	private void informPasso(Player who)
	{
		for (GameListener listener : gameListeners)
		{
			if (listener instanceof AgoniaGameListener)
			{// just to be safe!
				((AgoniaGameListener)listener).onPasso(who);
		
			}
		}
	}
	
	/** v2.3 */
	@Override
	public Player getWinner()
	{
		if (!isGameFinished)
		{
			return null;
		}
		
		Player winner = players.get(0);
		
		for (Player p : players)
		{
			if (winner.getScore() <= p.getScore())
			{
				winner = p;
			}
		}
		
		return winner;
	}
	
	public static void addGameListener(AgoniaGameListener listener)
	{
		gameListeners.add(listener);
	}

	public void setPlayerScores()
	{
		for (Player player : players)
		{
			for (Player rivlal : players)
			{
				if (player.getTeam() != rivlal.getTeam())
				{
					player.addScore(scoreOf(rivlal.getHand()));
				}
			}
		}
	}
	
	private void informDeckFinish()
	{
		for (GameListener listener : gameListeners)
		{
			if (listener instanceof AgoniaGameListener)
			{// just to be safe!
				((AgoniaGameListener)listener).onDeckFinished(deckFinishOption);
		
			}
		}
	}

//	/**
//	 * Saved in form:
//	 * 
//	 * [lastDraw_ID] [turn_ID] [isNineSpecial] [aceOnAce] [aceFinish] [deckFinishOption]
//	 * 
//	 * if lastDraw_ID is -1, mean lastDraw == null
//	 */
//	@Override
//	public void save(DataOutputStream d) throws IOException
//	{
//		super.save(d);
//		
//		int lastDrawId = lastDraw == null ? -1 : lastDraw.getId();
//		
//		d.writeInt(lastDrawId);
//		d.writeInt(turn.getId());
//		d.writeBoolean(isNineSpecial);
//		d.writeBoolean(aceOnAce);
//		d.writeBoolean(aceFinish);
//		d.writeInt(deckFinishOption);
//	}
//
//	@Override
//	public void load(DataInputStream d) throws IOException
//	{
//		super.load(d);
//		
//		lastDraw = getPlayerById(d.readInt());
//		turn = getPlayerById(d.readInt());
//		isNineSpecial = d.readBoolean();
//		aceOnAce = d.readBoolean();
//		aceFinish = d.readBoolean();
//		deckFinishOption = d.readInt();
//	}
}
