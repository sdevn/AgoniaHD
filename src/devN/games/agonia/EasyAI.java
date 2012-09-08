package devN.games.agonia;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import android.util.Log;
import devN.games.Card;
import devN.games.Player;

public class EasyAI implements AgoniaAI
{
	private Random rand = new Random();
	private Player me;
	
	public EasyAI(Player p)
	{
		me = p;
	}

	/**
	 * Paizei panta to fullo me ti megaluteri getRank()
	 */
	@Override
	public Card willPlay()
	{
		List<Card> tempHand = me.getHand();
		Collections.sort(tempHand, Collections.reverseOrder());
		
		for (Iterator<Card> iterator = tempHand.iterator(); iterator.hasNext();)
		{
			Card c = iterator.next();
			Log.d(TAG, "scanned " + c);
			
			if (me.getGame().canPlay(c))
			{
				Log.d(TAG, "choosed " + c);
				return new Card(c);
			}
		}
		return Card.NULL_CARD;
	}

	/**
	 * @return suit me ti megaluteri suhnotita ap ta fulla sto heri tou
	 */
	@Override
	public int getAceSuit()
	{
		int suitsCount[] = new int[Card.MAX_SUIT];
		int temp[] = new int[Card.MAX_SUIT];
		int i;

		if (me.getHand().size() == 0)
		{
			return rand.nextInt(Card.MAX_SUIT);
		}
		for (Card c : me.getHand())
		{
			suitsCount[c.getSuit()]++;
		}

		Log.d(TAG, Card.pszSUITS[0] + suitsCount[0] + " " + Card.pszSUITS[1]
					+ suitsCount[1] + " " + Card.pszSUITS[2] + suitsCount[2] + " "
					+ Card.pszSUITS[3] + suitsCount[3]);

		System.arraycopy(suitsCount, 0, temp, 0, Card.MAX_SUIT);

		Arrays.sort(temp);
		
		// finds index of max in unsorted array
		for (i = 0; i < suitsCount.length; i++)
		{
			if (suitsCount[i] == temp[3])
			{	// suitsCount[i] is suitsCount.getMax()
				break;
			}
		}
		Log.d(TAG, "will set to A" + Card.pszSUITS[i]);

		return i;
	}

	@Override
	public Card playSeven()
	{
		for (Card c : me.getHand())
		{
			if (c.getRank() == 7)
			{
				return new Card(c);
			}
		}

		return Card.NULL_CARD;
	}
}
