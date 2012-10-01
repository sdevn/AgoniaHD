package devN.games.agonia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import android.util.Log;
import devN.games.Card;
import devN.games.Player;

public class ModerateAI implements AgoniaAI
{
	private final static int SEVEN_PRIORITY = Card.MAX_RANK + 1;
	private final static int EIGHT_NINE_PRIORITY = SEVEN_PRIORITY + 1;
	
	private boolean isNineSpecial = F.getBoolean(F.KEY_IS_NINE_SPECIAl, true);
	private Random rand = new Random();
	private Comparator<Card> willPlayDescComparator;
	private Player me;

	public ModerateAI(Player p)
	{
		me = p;
		willPlayDescComparator = new Comparator<Card>(){

			@Override
			public int compare(Card arg0, Card arg1)
			{
				int _0priority, _1priority;
				
				_0priority = getPriorityOf(arg0);
				_1priority = getPriorityOf(arg1);
				
				if (_0priority > _1priority)
				{
					return -1;
				}
				else if (_0priority <_1priority) 
				{
					return 1;
				}
				// _0priority == _1priority, we need to check their suits
				
				int suitScores[] = getSuitsScores(Collections.<Integer> emptyList());
				
				if (suitScores[arg0.getSuit()] > suitScores[arg1.getSuit()])
				{
					return -1;
				}
				
				return suitScores[arg0.getSuit()] == suitScores[arg1.getSuit()] ? 0 : 1;
			}
			
		};
	}

	/**
	 * Paizei to fullo sumfona me tin parakato seira:<br />
	 * 8 - 9* (*an einai special)<br />
	 * 7<br />
	 * getValue()<br />
	 * An uparhei pano apo ena fullo me idia protereotita, dialegei auto
	 * tou opoiou to suit dinei to megalutero score ap ta fulla sto heri
	 * oi asoi den katametronte! ;)
	 */
	@Override
	public Card willPlay()
	{
		List<Card> playable = new ArrayList<Card>();

		Log.d(TAG, "available " + me.handString() + 
				"top -> " + me.getGame().getTop(0));
		
		for (Card card : me.getHand())
		{
			if (me.getGame().canPlay(card))
			{
				playable.add(card);
			}
		}
		
		if (playable.isEmpty())
		{
			Log.d(TAG, "No one playable!");
			return Card.NULL_CARD;
		}
		
		Collections.sort(playable, willPlayDescComparator);
		
		Log.d(TAG, "sorted: " + playable);
		
		return playable.get(0);
	}

	/**
	 * Dialegei to suit me to megalutero score ap ta fulla sto heri tou,
	 * an uparhei duplicate sto max ton suitsScore dialegei to suit me to
	 * megalutero priority	
	 * oi asoi den katametronte! ;)
	 */
	@Override
	public int getAceSuit()
	{
		int aceSuit;
		int scores[];
		int temp[] = new int[Card.MAX_SUIT];
		List<Integer> excludedRanks = new ArrayList<Integer>();
		List<Integer> topSuitsIndices = new ArrayList<Integer>();	// se periptosi pou duo suits
															// ehoun idio score kai einai max
		if (me.getHand().isEmpty())
		{
			return rand.nextInt(Card.MAX_SUIT);
		}
		
		excludedRanks.add(1);
		scores = getSuitsScores(excludedRanks);
		System.arraycopy(scores, 0, temp, 0, Card.MAX_SUIT);
		Arrays.sort(temp);
		
		for (int i = 0; i < Card.MAX_SUIT; i++)
		{
			if (scores[i] == temp[Card.MAX_SUIT - 1])
			{ // index of scores.max
				topSuitsIndices.add(i);
			}
		}
		
		if (topSuitsIndices.size() == 1)
		{
			aceSuit = topSuitsIndices.get(0);
		}
		else 
		{	// Duplicate maxValue
			int cDuplicates = topSuitsIndices.size();
			int suitsPrio[] = getSuitsPriorities(Collections.<Integer> emptyList());
			temp = new int[cDuplicates];
			
			for (int i = 0; i < cDuplicates; i++)
			{
				temp[i] = suitsPrio[i];
			}
			
			Arrays.sort(temp);
			
			// find index of filtered suitsPrio.max
			for (aceSuit = 0; aceSuit < suitsPrio.length; aceSuit++)
			{
				if (suitsPrio[aceSuit] == temp[cDuplicates - 1])
				{
					break;
				}
			}
		}
		
		Log.d(TAG, "will set to A" + Card.pszSUITS[aceSuit]);

		return aceSuit;
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

	private int[] getSuitsPriorities(List<Integer> excludedRanks)
	{
		int suitsPrio[] = new int[Card.MAX_SUIT];
		
		for (Card c : me.getHand())
		{
			if (!excludedRanks.contains(c.getRank()))
			{
				suitsPrio[c.getSuit()] += getPriorityOf(c);
			}
		}

		Log.d(TAG, "Priorities: " + Card.pszSUITS[0] + suitsPrio[0] + " " + Card.pszSUITS[1]
					+ suitsPrio[1] + " " + Card.pszSUITS[2] + suitsPrio[2] + " "
					+ Card.pszSUITS[3] + suitsPrio[3]);

		return suitsPrio;		
	}
	
	private int[] getSuitsScores(List<Integer> excludedRanks)
	{
		int suitsScores[] = new int[Card.MAX_SUIT];
		@SuppressWarnings("unchecked")
		List<Card> suit[] = new List[Card.MAX_SUIT];

		for (int i = 0; i < suit.length; i++)
		{
			suit[i] = new ArrayList<Card>();
		}

		for (Card c : me.getHand())
		{
			if (!excludedRanks.contains(c.getRank()))
			{
				suit[c.getSuit()].add(c);
			}
		}

		for (int i = 0; i < suit.length; i++)
		{
			suitsScores[i] = ((Agonia) me.getGame()).scoreOf(suit[i]);
		}

		Log.d(TAG, "Scores: " + Card.pszSUITS[0] + suitsScores[0] + " " + Card.pszSUITS[1]
					+ suitsScores[1] + " " + Card.pszSUITS[2] + suitsScores[2] + " "
					+ Card.pszSUITS[3] + suitsScores[3]);

		return suitsScores;		
	}
	
	private int getPriorityOf(Card c)
	{
		int prio;
		
		switch (c.getRank())
		{
		case 7:
			prio = SEVEN_PRIORITY;
			break;

		case 8:
			prio = EIGHT_NINE_PRIORITY;
			break;
		case 9:
			if (isNineSpecial)
			{
				prio = EIGHT_NINE_PRIORITY;
				break;
			}
		default:
			prio = c.getValue();
			break;
		}
		
		return prio;
	}
}
