package devN.games.agonia;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class EloCalculator
{
	private final static int MAX_ELO_AWARD = 350;
	private final static int MIN_ELO_AWARD = 30;
	
	public static void setEloPoints(EloEntity winner, EloEntity losser)
	{
		int diff = Math.abs(winner.getElo() - losser.getElo());
		double prob;

		if		(diff > 700){prob = 0.99;}
		else if	(diff > 500){prob = 0.95;}
		else if	(diff > 450){prob = 0.93;}
		else if	(diff > 400){prob = 0.90;}
		else if	(diff > 350){prob = 0.88;}
		else if	(diff > 300){prob = 0.85;}
		else if	(diff > 250){prob = 0.81;}
		else if	(diff > 200){prob = 0.77;}
		else if	(diff > 150){prob = 0.70;}
		else if	(diff > 100){prob = 0.64;}
		else if	(diff > 50)	{prob = 0.57;}
		else if	(diff > 25)	{prob = 0.53;}
		else				{prob = 0.50;}
		
		if (winner.getWinRate() > losser.getWinRate())
		{
			int rateDiff = winner.getWinRate() - losser.getWinRate();
			prob = Math.min(0.99, prob + (double) rateDiff / 10.0);
		}
		
		if (winner.getElo() > losser.getElo())
		{
			prob = 1.0 - prob;
		}
		
		diff = Math.min(Math.max((int) (diff * prob), MIN_ELO_AWARD), MAX_ELO_AWARD);
		
		if (winner.getK() == 0)
		{
			winner.addElo(diff);
		}
		
		if (losser.getK() == 0)
		{
			losser.addElo(-diff);
		}
	}
	
	public static void main(String[] args)
	{
		final int cPlayers = 50;
		final int cGames = 1000000;
		final Random random = new Random();
		
		List<EloEntity> players = new ArrayList<EloEntity>();
		
		int summ = 0;
		
		for (int i = 0; i < cPlayers; i++)
		{
			players.add(new EloEntity());
		}
		
		for (int i = 0; i < cGames; i++)
		{
			int pos = random.nextInt(players.size());
			int pos2 = random.nextInt(players.size());
			
			while (pos2 == pos)
			{
				pos2 = random.nextInt(players.size());
			}
			
			EloEntity a = players.get(pos);
			EloEntity b = players.get(pos2);

			a.win();
			b.lose();
			setEloPoints(a, b);
			
			if (i % 1000 == 0)
			{
				players.add(new EloEntity());
			}
		}
		
		for (EloEntity eloEntity : players)
		{
			System.out.println(eloEntity);
			summ += eloEntity.getElo();
		}
		
		System.out.println("Elo mean " + (summ / players.size()));
	}
}
