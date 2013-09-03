package devN.games;

import java.util.Random;


public class Card implements Comparable<Card>
{
	// pszSUITS indices
	public static final int iCLUBS 		= 0; // 000
	public static final int iDIAMONDS 	= 1; // 001
	public static final int iSPADES 	= 2; // 010
	public static final int iHEARTS 	= 3; // 011
	public static final String[] pszSUITS =
		{"\u2663", "\u2666", "\u2660", "\u2665"};

	// gia aples kartes
	public static final int MAX_SUIT 	= 4;
	public static final int MAX_RANK 	= 10 + 3;

	// jokers ranks - suits
	public static final int JOKER_RED_SUIT	 = 5; // 101
	public static final int JOKER_BLACK_SUIT = 6; // 110
	public static final int JOKER_RANK 		 = MAX_RANK + 1;

	// bitwise masks
	protected static final int MASK_COLOR 	= 1;
	protected static final int MASK_RED 	= 1;
	protected static final int MASK_BLACK 	= 0;

	public static final Card NULL_CARD 	= new Card(-1, -1);
	
	protected int suit;
	protected int rank;
	
	public Card(int suit, int rank)
	{
		this.rank = rank;
		this.suit = suit;
	}

	public Card(byte[] array)
	{
		this.rank = array[1];
		this.suit = array[0];
	}
	
	public Card(Card c)
	{
		this(c.getSuit(), c.getRank());
	}

	/**
	 * @return the rank
	 */
	public int getRank()
	{
		return rank;
	}

	/**
	 * @return the suit
	 */
	public int getSuit()
	{
		return suit;
	}

	/**
	 * @return tin aksia tou hartiou. Ta grammena ehoun aksia isi me 10. Eno ta
	 *         upoloipa ehoun aksia isi me to rank tous.
	 */
	public int getValue()
	{
		return (rank > 10) ? 10 : rank;
	}

	/**
	 * @param rank the rank to set
	 */
	public void setRank(int rank)
	{
		this.rank = rank;
	}

	/**
	 * @param suit the suit to set
	 */
	public void setSuit(int suit)
	{
		this.suit = suit;
	}

	/**
	 * @return true an i karta ehei kokkino hroma.
	 */
	public boolean isRed()
	{
		return (this.suit & MASK_COLOR) == MASK_RED;
	}

	/**
	 * @return true an i karta ehei mauro hroma.
	 */
	public boolean isBlack()
	{
		return (this.suit & MASK_COLOR) == MASK_BLACK;
	}

	public boolean isCourt()
	{
		return this.rank > 10;
	}
	public boolean sameColor(Card that)
	{
		return (this.suit & MASK_COLOR) == (that.getSuit() & MASK_COLOR);
	}

	public static boolean sameColor(Card a, Card b)
	{
		return (a.getSuit() & MASK_COLOR) == (b.getSuit() & MASK_COLOR);
	}

	public boolean sameRank(Card that)
	{
		return this.rank == that.getRank();
	}
	
	public static boolean sameRank(Card a, Card b)
	{
		return a.getRank() == b.getRank();
	}
	
	public boolean sameSuit(Card that)
	{
		return this.suit == that.getSuit();
	}
	
	public static boolean sameSuit(Card a, Card b)
	{
		return a.getSuit() == b.getSuit();
	}
	
	public boolean sameValue(Card that)
	{
		return this.getValue() == that.getValue();
	}
	
	public static boolean sameValue(Card a, Card b)
	{
		return a.getValue() == b.getValue();
	}
	
	public byte[] toByteArray()
	{
		byte[] array = new byte[2];
		
		array[0] = (byte) suit;
		array[1] = (byte) rank;
		
		return array;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Card that)
	{
		if (this.equals(that))
		{
			return 0;
		}
		
		if (this.rank == that.getRank())
		{
			return this.suit > that.getSuit() ? 1 : -1;
		}
		
		return this.rank > that.getRank() ? 1 : -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}

		if (!(o instanceof Card))
		{
			return false;
		}

		Card that = (Card) o;

		return (this.sameRank(that)) && (this.sameSuit(that));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return 31 * this.rank + this.suit;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		String rank;
		
		if (equals(NULL_CARD))
		{
			return "[NC]";
		}
		
		switch (this.rank)
		{
			case 1:
			{
				rank = "A";
			}
			break;
			
			case 11:
			{
				rank = "J";
			}
			break;
			
			case 12:
			{
				rank = "Q";
			}
			break;
			
			case 13:
			{
				rank = "K";
			}
			break;
			
			case JOKER_RANK:
			{
				rank = "JK";
			}
			break;
			
			default:
			{
				rank = Integer.toString(this.rank);
			}
			break;
		}
		
		return "[" + rank + pszSUITS[suit] + "]";
	}
	
	public static Card getRandomCard()
	{
		Random random = new Random();
		
		return new Card(random.nextInt(MAX_SUIT), random.nextInt(MAX_RANK));
	}
}
