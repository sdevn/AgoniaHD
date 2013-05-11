package devN.games.agonia;

import static devN.games.agonia.GameSetActivity.iPLAYERS_NUM_1v1;
import static devN.games.agonia.GameSetActivity.iPLAYERS_NUM_1v1v1;
import static devN.games.agonia.GameSetActivity.iPLAYERS_NUM_2v2;
import devN.games.Card;

public class AgoniaGamesStats
{
	private final static int iWIN 	= 0;
	private final static int iLOSE 	= 1;
	
	/** [iPLAYERS_NUM_*][iWIN|iLOSE] */
	private int[][] games;

	/** [iPLAYERS_NUM_*][iWIN|iLOSE] */
	private int[][] sets;
	
	private int sevensPlayed;
	private int eightsPlayed;
	private int ninesPlayed;
	private int acesPlayed;

	/** <u>All</u> cards are involved */
	private int sumCardsPlayed;
	
	private int drewFromSevens;
	
	/** {@link AgoniaGamesStats#drewFromSevens } and initial card deal are <u>not</u> involved */
	private int sumCardsDrew;
	
	private int passo;
	
	private int sumPoints;
	private int maxPoints;
	
	public AgoniaGamesStats()
	{
		games = new int[3][2];
		sets = new int[3][2];
	}

	public int getGamesWins(int type)
	{
		return games[type][iWIN];
	}
	
	public int getTotalGamesWins()
	{
		return getGamesWins(iPLAYERS_NUM_1v1) 
			+ getGamesWins(iPLAYERS_NUM_1v1v1) 
			+ getGamesWins(iPLAYERS_NUM_2v2);
	}

	public int getGamesLoses(int type)
	{
		return games[type][iLOSE];
	}

	public int getTotalGamesLoses()
	{
		return getGamesLoses(iPLAYERS_NUM_1v1) 
			+ getGamesLoses(iPLAYERS_NUM_1v1v1) 
			+ getGamesLoses(iPLAYERS_NUM_2v2);
	}
	
	public int getTotalGames()
	{
		return getTotalGamesWins() + getTotalGamesLoses();
	}

	public int getSetsWins(int type)
	{
		return sets[type][iWIN];
	}
	
	public int getTotalSetsWins()
	{
		return getSetsWins(iPLAYERS_NUM_1v1) 
			+ getSetsWins(iPLAYERS_NUM_1v1v1) 
			+ getSetsWins(iPLAYERS_NUM_2v2);
	}
	
	public int getSetsLoses(int type)
	{
		return sets[type][iLOSE];
	}
	
	public int getTotalSetsLoses()
	{
		return getSetsLoses(iPLAYERS_NUM_1v1) 
			+ getSetsLoses(iPLAYERS_NUM_1v1v1) 
			+ getSetsLoses(iPLAYERS_NUM_2v2);
	}
	
	public int getTotalSets()
	{
		return getTotalSetsWins() + getTotalSetsLoses();
	}

	public int getSevensPlayed()
	{
		return sevensPlayed;
	}

	public int getEightsPlayed()
	{
		return eightsPlayed;
	}

	public int getNinesPlayed()
	{
		return ninesPlayed;
	}

	public int getAcesPlayed()
	{
		return acesPlayed;
	}

	public int getTotalCardsPlayed()
	{
		return sumCardsPlayed;
	}

	public int getDrewFromSevens()
	{
		return drewFromSevens;
	}

	public int getTotalCardsDrew()
	{
		return sumCardsDrew;
	}

	public int getPassoCount()
	{
		return passo;
	}

	public int getSumPoints()
	{
		return sumPoints;
	}

	public int getMaxPoints()
	{
		return maxPoints;
	}
	
	// Games: %d\n\t\t\tWon / Lost\n\t1v1:\t%d / %d\n\t1v1v1:\t%d / %d\n\t2v2:\t%d / %d\n\n
	// Sets: %d\n\t\t\tWon / Lost\n\t1v1:\t%d / %d\n\t1v1v1:\t%d / %d\n\t2v2:\t%d / %d\n\n
	// Cards Played: %d\nAces: %d\nSevens: %d\nEights: %d\nNines: %d\n\n
	// Cards drawn: %d (drawn from sevens are excluded)\nDrawn from sevens: %d\n\n
	// Passo %d times\n\n
	// Total points: %d\nMax points gained: %d
	public Object[] getInfos()
	{
		Object[] infos = 
		{
		 	getTotalGames(), 
			getGamesWins(iPLAYERS_NUM_1v1), getGamesLoses(iPLAYERS_NUM_1v1),
			getGamesWins(iPLAYERS_NUM_1v1v1), getGamesLoses(iPLAYERS_NUM_1v1v1),
			getGamesWins(iPLAYERS_NUM_2v2), getGamesLoses(iPLAYERS_NUM_2v2),
			getTotalSets(),
			getSetsWins(iPLAYERS_NUM_1v1), getSetsLoses(iPLAYERS_NUM_1v1),
			getSetsWins(iPLAYERS_NUM_1v1v1), getSetsLoses(iPLAYERS_NUM_1v1v1),
			getSetsWins(iPLAYERS_NUM_2v2), getSetsLoses(iPLAYERS_NUM_2v2),
			getTotalCardsPlayed(), 
			getAcesPlayed(), getSevensPlayed(), getEightsPlayed(), getNinesPlayed(),
			getTotalCardsDrew(), getDrewFromSevens(),
			getPassoCount(),
			getSumPoints(), getMaxPoints()
		};

		return infos;
	}

	public void wonGame(int type)
	{
		games[type][iWIN]++;
	}

	public void lostGame(int type)
	{
		games[type][iLOSE]++;
	}

	public void wonSet(int type)
	{
		sets[type][iWIN]++;
	}

	public void lostSet(int type)
	{
		sets[type][iLOSE]++;
	}

	private void sevenPlayed()
	{
		sevensPlayed++;
	}

	private void eightPlayed()
	{
		eightsPlayed++;
	}

	private void ninePlayed()
	{
		ninesPlayed++;
	}

	private void acePlayed()
	{
		acesPlayed++;
	}

	public void cardPlayed(Card c)
	{
		sumCardsPlayed++;
		
		switch (c.getRank())
		{
		case 1:
			acePlayed();
			break;
		case 7:
			sevenPlayed();
			break;
		case 8:
			eightPlayed();
			break;
		case 9:
			ninePlayed();
			break;
		}
	}

	public void drewFromSevens(int count)
	{
		drewFromSevens += count;
	}

	public void drewCard()
	{
		sumCardsDrew++;
	}

	public void passo()
	{
		passo++;
	}

	public void addPoints(int points)
	{
		sumPoints += points;
		
		setMaxPoints_Safe(points);
	}

	private void setMaxPoints_Safe(int points)
	{
		if (maxPoints < points)
		{
			maxPoints = points;
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder
		.append("games: ");
		for (int i = 0; i < games.length; i++)
		{
			builder.append("\n\t" + i + ": ");
			for (int j = 0; j < games[i].length; j++)
			{
				builder.append("\n\t\t" + j + ": " + games[i][j]);
			}
		}
		builder.append("\nsets: ");
		for (int i = 0; i < sets.length; i++)
		{
			builder.append("\n\t" + i + ": ");
			for (int j = 0; j < sets[i].length; j++)
			{
				builder.append("\n\t\t" + j + ": " + sets[i][j]);
			}
		}
		builder
		.append("\nsevensPlayed: ").append(sevensPlayed)
		.append("\neightsPlayed: ").append(eightsPlayed)
		.append("\nninesPlayed: ").append(ninesPlayed)
		.append("\nacesPlayed: ").append(acesPlayed)
		.append("\nsumCardsPlayed: ").append(sumCardsPlayed)
		.append("\ndrewFromSevens: ").append(drewFromSevens)
		.append("\nsumCardsDrew: ").append(sumCardsDrew)
		.append("\npasso: ").append(passo)
		.append("\nsumPoints: ").append(sumPoints)
		.append("\nmaxPoints: ").append(maxPoints)
		.append("\n");
		return builder.toString();
	}

}
