package devN.games;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;
import devN.games.agonia.AgoniaAI;

public class Player implements AgoniaAI, Parcelable, Serializable
{
	/**
	 * v2.2
	 */
	private static final long serialVersionUID = -3883828405433368181L;

	private static int cIDs = 0;

	/**
	 * v2.2 modified to transient <br />
	 * it will serialized for {@link GameSet}  
	 */
	protected transient Deck deck;
	
	/**
	 * v2.2 modified to transient <br />
	 * it will serialized for {@link GameSet}  
	 */
	protected transient List<Card> hand;
	
	/**
	 * v2.2 modified to transient <br />
	 * it will serialized for {@link GameSet}  
	 */
	protected transient List<Card> owned;
	
	/**
	 * v2.2 modified to transient <br />
	 * it will serialized for {@link GameSet}  
	 */
	protected transient CardGame game;
	
	protected String name;
	protected int setScore;
	protected int setWins;
	protected boolean playingInSet;
	protected int score;
	protected int team;
	protected int id;
	protected int wins;
	
	/**
	 * v2.2 modified to transient <br />
	 * it will manually serialized to {@link ObjectOutputStream} 
	 */
	protected transient AgoniaAI ai;
	
	public Player()
	{
		this.id = ++cIDs;
		this.name = "Uknown_" + this.id;
		this.setScore = 0;
		this.setWins = 0;
		this.playingInSet = false;
		this.score = 0;
		this.team = -1;
		this.wins = 0;
		this.hand = new ArrayList<Card>();
		this.owned = new ArrayList<Card>(); 
	}
	
	public Player(String name, int team)
	{
		this();
		this.name = name;
		this.team = team;
	}
	
	public void addScore(int points)
	{
		if (playingInSet)
		{
			addSetScore(points);
		}
		this.score += points;
	}

	public List<Card> draw()
	{
		List<Card> drawed = deck.draw();
		hand.addAll(drawed);
		
		return drawed;
	}
	
	public List<Card> draw(int n)
	{
		List<Card> drawed = deck.draw(n); 
		hand.addAll(drawed);
		
		return drawed;
	}
	
	public List<Card> draw(List<Card> cards)
	{
		List<Card> drawed = deck.draw(cards);
		hand.addAll(drawed);
		
		return drawed;
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
	
	public boolean isRealPlayer()
	{
		return ai == null;
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
	
	public int getWins()
	{
		return wins;
	}

	/**
	 * @return the hand
	 */
	public List<Card> getHand()
	{
		return hand;
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

	public int getSetScore()
	{
		return setScore;
	}

	public int getSetWins()
	{
		return setWins;
	}

	public boolean isPlayingInSet()
	{
		return playingInSet;
	}

	public void setSetScore(int newScore)
	{
		this.setScore = newScore;
	}
	
	public void addSetScore(int score)
	{
		this.setScore += score;
	}

	public void setSetWins(int setWins)
	{
		this.setWins = setWins;
	}
	
	public void winSet()
	{
		this.setWins++;
	}

	public void setPlayingInSet(boolean playingInSet)
	{
		this.playingInSet = playingInSet;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setHand(List<Card> hand)
	{
		this.hand = hand;
	}

	public void setScore(int score)
	{
		this.score = score;
	}

	public void setTeam(int team)
	{
		this.team = team;
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
	
	public void setWins(int wins)
	{
		this.wins = wins;
	}
	
	public void win()
	{
		if (playingInSet)
		{
			winSet();
		}
		else 
		{
			wins++;
		}
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

	@Override
	public String toString()
	{
		return name + " [" + hand.size() + "]";
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
	
	/**
	 * @return {@link AgoniaAI#getMode() getMode()} or {@link AgoniaAI#MODE_NO_AI} if its not an AI
	 */
	@Override
	public int getMode()
	{
		if (isRealPlayer())
		{
			return AgoniaAI.MODE_NO_AI;
		}
		
		return ai.getMode();
	}

	@Override
	public Card willPlay()
	{
		return ai.willPlay();
	}

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
	
	//
	// Serializable implementation
	//
	
	protected void writeObject(ObjectOutputStream s) throws IOException 
	{// v2.2 GameSet serialization
		
		s.defaultWriteObject();
		
		s.writeInt(getMode());
	}

	protected void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException 
	{// v2.2 GameSet deserialization
		
		s.defaultReadObject();
		
		ai = AgoniaAIBuilder.createById(s.readInt(), this);
	}
	
	//
	// Parcelable implementation
	//
	
	public static final Parcelable.Creator<Player> CREATOR = new Parcelable.Creator<Player>(){
		@Override
		public Player createFromParcel(Parcel source)
		{
			return new Player(source);
		}

		@Override
		public Player[] newArray(int size)
		{
			return new Player[size];
		}

	};
	
	@Override
	public int describeContents()
	{
		return 0;
	}

	/**
	 * Saved in form:
	 * [name] [id] [team] [AI_mode] [setWins] [setScore]
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(name);
		dest.writeInt(id);
		dest.writeInt(team);
		dest.writeInt(getMode());
		dest.writeInt(setWins);
		dest.writeInt(setScore);
	}

	/**
	 * Load in form:
	 * [name] [id] [team] [AI_mode] [setWins] [setScore]
	 */
	private Player(Parcel in) 
	{
		name = in.readString();		
		id = in.readInt();
		team = in.readInt();
		ai = AgoniaAIBuilder.createById(in.readInt(), this);
		setWins = in.readInt();
		setScore = in.readInt();
    }
}
