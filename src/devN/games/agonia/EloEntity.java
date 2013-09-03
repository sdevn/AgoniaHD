package devN.games.agonia;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public class EloEntity
{
	private final static int DEFAULT_K		= Const.MAX_DAILY_GAMES - 5;	
	private final static int DEFAULT_ELO 	= 1200;
	
	private final static int MIN_ELO	= 150;
	private final static int MAX_ELO	= 2400;
	
	private int K;
	private int wins;
	private int losses;
	private int points;
	private int elo;
	private List<Long> gamesHistory;
	
	public EloEntity()
	{
		K = DEFAULT_K;
		elo = DEFAULT_ELO;
		wins = losses = points = 0;
		gamesHistory = new ArrayList<Long>();
	}
	
	public EloEntity(byte[] data)
	{
		this();
		ByteBuffer buffer = ByteBuffer.wrap(data);
		
		K = buffer.getInt();
		wins = buffer.getInt();
		losses = buffer.getInt();
		points = buffer.getInt();
		elo = buffer.getInt();
		
		int size = buffer.getInt();
		
		for (int i = 0; i < size; i++)
		{
			gamesHistory.add(buffer.getLong());
		}
	}

	public int getK()
	{
		return K;
	}

	public int getWins()
	{
		return wins;
	}

	public int getLosses()
	{
		return losses;
	}

	public int getPoints()
	{
		return points;
	}

	public int getElo()
	{
		return elo;
	}
	
	public List<Long> getGamesHistory()
	{
		return gamesHistory;
	}
	
	public int getTodayGames()
	{
		int games = 0;
		
		for (ListIterator<Long> iterator = gamesHistory.listIterator(gamesHistory.size()); 
				iterator.hasPrevious();)
		{
			if (!isToday(iterator.previous()))
			{
				break;
			}
			games++;
		}
		
		return games;
	}
	
	public int getCurrentWinStreak()
	{
		int streak = 0;
		
		for (ListIterator<Long> iterator = gamesHistory.listIterator(gamesHistory.size()); 
				iterator.hasPrevious();)
		{
			if ((iterator.previous() & 1) == 0)
			{
				break;
			}
			streak++;
		}
		
		return streak;
	}
	
	public int getMaxWinStreak()
	{
		int max = 0;
		int streak = 0;
		
		for (long val : gamesHistory)
		{
			if ((val & 1) > 0)
			{
				streak++;
			}
			else 
			{
				if (streak > max)
				{
					max = streak;
				}
				streak = 0;
			}
		}
		
		return Math.max(max, streak);
	}
	
	public int getWinRate()
	{
		int rate = 0;
		double games = wins + losses;
		
		if (games > 0)
		{
			rate = (int) ((((double) wins) / games) * 100D);
		}
		
		return rate;
	}

	private void decreaseK()
	{
		K = Math.max(0, K - 1);
	}

	public void win()
	{
		decreaseK();
		gamesHistory.add(new Date().getTime() | 1);
		++wins;
	}

	public void lose()
	{
		decreaseK();
		gamesHistory.add((new Date().getTime() >> 1) << 1);		
		++losses;
	}

	public void addPoints(int points)
	{
		this.points += points;
	}

	public void addElo(int amount)
	{
		elo += amount;
		
		if (elo > MAX_ELO)
		{
			elo = MAX_ELO;
		}
		else if (elo < MIN_ELO) 
		{
			elo = MIN_ELO;
		}
	}
	
	// elo_localized_format
	// Wins:%d Games:%d ELO:%d
    public String toLocalizedString(String format)
	{
		return String.format(format, wins, wins + losses, elo);
	}
    
    // elo_localized_long_format
    // Wins:%d Games:%d
    // Today games:%d of %d (%d+%d)
	// ELO:%d Points:%d
	// Current winning streak:%d
	// Max winning streak:%d
	//
    // Games history
    // -------------
    // %s
    public String toLocalizedLongString(String format, String dateFormat, int extraGames, String[] lostAndWon)
	{
		StringBuilder history = new StringBuilder();
		Calendar calendar = Calendar.getInstance();
		for (long val : gamesHistory)
		{
			calendar.setTimeInMillis(val);
			history.insert(0, new SimpleDateFormat(dateFormat).format(calendar.getTime()) 
								+ "\t\t" + lostAndWon[(int) (val & 1)] + "\n");
		}

    	return String.format(format, 
    						wins, wins + losses,
    						getTodayGames(), Const.MAX_DAILY_GAMES + extraGames, 
    							Const.MAX_DAILY_GAMES, extraGames,
    						elo, points,
    						getCurrentWinStreak(),
    						getMaxWinStreak(),
    						history.toString());
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder
//		.append("K:").append(K)
		.append(" wins:").append(wins)
		.append(" losses:").append(losses)
		.append(" points:").append(points)
		.append(" elo:").append(elo)
//		.append(" rate:").append(getWinRate())
		.append(" streak:").append(getCurrentWinStreak())
		.append(" max streak:").append(getMaxWinStreak());
		
//		builder.append("\ngamesHistory<")
//		.append(new SimpleDateFormat("HH:mm dd/MM/YYY").format(new Date(gamesHistory.get(0))))
//		.append(String.format(" %d", gamesHistory.get(0) & 1));
//		for (int i = 1; i < gamesHistory.size(); i++)
//		{
//			builder.append(", ")
//			.append(new SimpleDateFormat("HH:mm dd/MM/YYY").format(new Date(gamesHistory.get(i))))
//			.append(String.format(" %d", gamesHistory.get(i) & 1));
//		}
//		builder.append(">");
		
//		builder.append("\n");
		
		return builder.toString();
	}

	public byte[] getBytes()
	{
		ByteBuffer buffer = ByteBuffer.allocate((6 * Integer.SIZE 
												+ gamesHistory.size() * Long.SIZE) 
													/ Byte.SIZE)
		.putInt(K)
		.putInt(wins)
		.putInt(losses)
		.putInt(points)
		.putInt(elo)
		.putInt(gamesHistory.size())
		;
		
		for (long val : gamesHistory)
		{
			buffer.putLong(val);
		}
				
		return buffer.array();
	}
	
	private static boolean isToday(long game)
	{
		Calendar now = Calendar.getInstance();
		Calendar when = Calendar.getInstance();
		when.setTimeInMillis(game);
		
		return now.get(Calendar.YEAR) == when.get(Calendar.YEAR) 
			&& now.get(Calendar.DAY_OF_YEAR) == when.get(Calendar.DAY_OF_YEAR);
	}
	
	
	public static void main(String[] args)
	{
		EloEntity entity = new EloEntity();
		Random random = new Random();
		
		for (int i = 0; i < 300; i++)
		{
			if (random.nextBoolean())
			{
				entity.win();
				entity.addPoints(random.nextInt(65) + 5);
				entity.addElo(random.nextInt(100) + 10);
			}
			else 
			{
				entity.lose();
				entity.addPoints(0);
				entity.addElo(-(random.nextInt(100) + 10));
			}
			
			System.out.println(entity);
		}
	}
}
