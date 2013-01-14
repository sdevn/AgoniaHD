package devN.games;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import devN.games.agonia.AgoniaAI.AgoniaAIBuilder;
import devN.games.agonia.F;
import devN.games.agonia.R;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Partida paihnidiwn
 *
 */
public class GameSet implements Parcelable
{	
	public final static int TYPE_POINTS = 0;
	public final static int TYPE_WINS 	= 1;
	public final static int NO_WINNER 	= -1;
	
	private Map<Integer, ArrayList<Player>> teams; // <teamID, playersList>
	private int firstPlayerId;
	private int cGame;
	private int goalScore;
	private int type;

	public GameSet(int gameSetType, int goalScore)
	{
		this.goalScore = goalScore;
		this.type = gameSetType;
		firstPlayerId = 0;
		cGame = 0;
		teams = new Hashtable<Integer, ArrayList<Player>>(); 
	}
	
	public void addPlayer(Player p)
	{
		int t = p.getTeam();
		int pos = 0;
		ArrayList<Player> players;
		
		if (teams.containsKey(t))
		{
			players = teams.get(t);
			pos = players.get(0).getId() < p.getId() ? 1 : 0;
		}
		else 
		{
			players = new ArrayList<Player>();			
			teams.put(t, players);
		}
		players.add(pos, p);
		p.setPlayingInSet(true);
	}
	
	public void clearPlayers()
	{
		teams.clear();
	}
	
	/**
	 * Isos uparhei isovathmia!!
	 */
	public ArrayList<Integer> getWinningTeams()
	{
		ArrayList<Integer> winners = new ArrayList<Integer>();
		int teamScore, maxScore = goalScore; 
		
		for (Integer team : teams.keySet())
		{
			if (type == TYPE_WINS)
			{
				teamScore = getSetWinsOf(team);
			}
			else 
			{
				teamScore = getSetScoreOf(team);
			}
			
			if (teamScore > maxScore)
			{
				winners.clear();
				winners.add(team);
				maxScore = teamScore;
			}
			else if (teamScore == maxScore) 
			{
				winners.add(team);
			}
		}

		return winners;
	}
	
	private int getSetWinsOf(int team)
	{
		int wins = 0;
		
		for (Player player : teams.get(team))
		{
			wins += player.getSetWins();
		}
		
		return wins;
	}
	
	private int getSetScoreOf(int team)
	{
		int score = 0;
		
		for (Player player : teams.get(team))
		{
			score += player.getSetScore();
		}
		
		return score / teams.get(team).size();
	}
	
	private Player getPlayerById(int id)
	{
		for (ArrayList<Player> players : teams.values())
		{
			for (Player p : players)
			{
				if (p.getId() == id)
				{
					return p;
				}
			}
		}
		
		return null;
	}
	
	public Player getPlayerByTeam(int team, int index)
	{
		return teams.get(team).get(index);
	}
	
	public boolean isSetFinished()
	{
		return !getWinningTeams().isEmpty();
	}
	
	public void addScoreTo(int playerId, int score)
	{
		getPlayerById(playerId).addSetScore(score);
		getPlayerById(playerId).addScore(score);
	}
	
	private ArrayList<Integer> getCurWinnerTeam()
	{
		ArrayList<Integer> winners = new ArrayList<Integer>();
		
		for (Integer team : teams.keySet())
		{
			for (Player p : teams.get(team))
			{
				if (!p.hasCards())
				{
					winners.add(team);
					break;
				}
			}
		}

		return winners;
	}
	
	public void gameFinished()
	{
		cGame++;
		ArrayList<Integer> winners = getCurWinnerTeam();

		for (Integer team : winners)
		{
			teams.get(team).get(0).winSet();
		}
	}
	
	public String winnerString()
	{
		StringBuilder sb = new StringBuilder();
		ArrayList<Integer> winners = getWinningTeams();

		for (Integer team : winners)
		{
			sb.append(team + " ");
		}

		return F.getResources().getString(R.string.winner_team, sb.toString());
	}
	
	public Object[] playerInfos(int playerId)
	{
		Object[] infos = new Object[3];
		Player player = getPlayerById(playerId);
		int mode = player.isRealPlayer() ? -1 : player.getAI().getMode();
		
		infos[0] = player.getTeam();
		infos[1] = player.getName();
		infos[2] = AgoniaAIBuilder.getAIString(mode);
		
		return infos;
	}

	public Object[] teamInfos(int team)
	{
		Object[] infos = new Object[2];
		
		infos[0] = getSetWinsOf(team);
		infos[1] = getSetScoreOf(team);
		
		return infos;
	}
	
	public Object[] getInfos()
	{
		Object[] infos = new Object[3];
		int id = type == TYPE_POINTS ? R.string.points : R.string.wins;
		
		infos[0] = F.getResources().getString(id);
		infos[1] = goalScore;
		infos[2] = cGame;
		
		return infos;
	}
	
	public int getPlayersCount()
	{
		int cPlayers = 0;
		
		for (ArrayList<Player> players : teams.values())
		{
			cPlayers += players.size();
		}
		
		return cPlayers;
	}
	
	public int getTeamsCount()
	{	
		return teams.size();
	}
	
	public int getGameCount()
	{
		return cGame;
	}

	public int getGoalScore()
	{
		return goalScore;
	}

	public int getType()
	{
		return type;
	}

	public int getFirstPlayerId()
	{
		return firstPlayerId;
	}

	public void setFirstPlayerId(int firstPlayerId)
	{
		this.firstPlayerId = firstPlayerId;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		for (ArrayList<Player> players : teams.values())
		{
			for (Player player : players)
			{
				sb.append(player.team + player.getName());
				sb.append("\n");
			}
		}
		
		return sb.toString();
	}

	public static final Parcelable.Creator<GameSet> CREATOR = new Parcelable.Creator<GameSet>()
	{
		@Override
		public GameSet createFromParcel(Parcel source)
		{
			return new GameSet(source);
		}

		@Override
		public GameSet[] newArray(int size)
		{
			return new GameSet[size];
		}
	};

	@Override
	public int describeContents()
	{
		return 0;
	}

	/**
	 * Saved in form:
	 * [type] [goal_score] [cGames] [firstPlayerId] [getPlayersCount()] [playerN]
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeInt(type);
		dest.writeInt(goalScore);
		dest.writeInt(cGame);
		dest.writeInt(firstPlayerId);
		
		dest.writeInt(getPlayersCount());
		
		for (ArrayList<Player> players : teams.values())
		{
			for (Player p : players)
			{
				dest.writeParcelable(p, flags);
			}
		}
	}
	
	/**
	 * Load in form:
	 * [type] [goal_score] [cGames] [firstPlayerId] [getPlayersCount()] [playerN]
	 */
	private GameSet(Parcel in) 
	{
		type = in.readInt();
		goalScore = in.readInt();
		cGame = in.readInt();
		firstPlayerId = in.readInt();
		
		teams = new Hashtable<Integer, ArrayList<Player>>(); 
		
		int cPlayers = in.readInt();
		
		for (int i = 0; i < cPlayers; i++)
		{
			Player player = in.readParcelable(Player.class.getClassLoader());
			
			addPlayer(player);
		}
    }
}