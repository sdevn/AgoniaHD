package devN.games.agonia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import devN.games.Card;
import devN.games.CardGame;
import devN.games.Player;

public class AgoniaGamesStatsManager implements AgoniaGameListener
{
	private static final String KEYS_PREFIX = "key_prof_";
	private static final String[] pszAI_KEYS =	{	KEYS_PREFIX + "Monkey_CPU",
												 	KEYS_PREFIX + "Easy_CPU",
												 	KEYS_PREFIX + "Moderate_CPU"
												};
	private static final Pattern PATTERN_OF_KEYS = Pattern.compile(KEYS_PREFIX);
	
	/** only one instance of this class should exist */
	private static AgoniaGamesStatsManager manager;

	/** to easily implement {@link Object#equals(Object)} */
	private static int ids = 0;
	
	private Map<String, AgoniaGamesStats> profiles;
	private final int id;
	
	public static AgoniaGamesStatsManager create(Context context)
	{
		if (manager != null)
		{ // preventing duplicate
			CardGame.unregisterGameListener(manager);
			manager.saveStats(context, true);
		}
		
		manager = new AgoniaGamesStatsManager(context);			

		Agonia.addGameListener(manager);
		
		return manager;
	}
	
	private AgoniaGamesStatsManager(Context context)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Map<String, ?> allPrefs = prefs.getAll();
		
		profiles = new HashMap<String, AgoniaGamesStats>();
		id = ++ids;
		
		Gson gson = new Gson();
		
		for (String key : allPrefs.keySet())
		{
			if (PATTERN_OF_KEYS.matcher(key).lookingAt())
			{
				profiles.put(key, gson.fromJson((String) allPrefs.get(key), AgoniaGamesStats.class));
			}
		}
	}
	
	public void saveStats(Context context, boolean exit)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		
		Gson gson = new Gson();
		
		for (String key : profiles.keySet())
		{
			editor.putString(key, gson.toJson(profiles.get(key)));
		}
		
		if (exit)
		{
			editor.commit();
			CardGame.unregisterGameListener(manager);
			manager = null;
		}
	}
	
	public List<CharSequence> getProfileNames()
	{
		List<CharSequence> profs = new ArrayList<CharSequence>();
		
		for (String key : profiles.keySet())
		{
			profs.add(key.substring(KEYS_PREFIX.length()));
		}
		
		return profs;
	}
	
	public void deleteStatsOf(Context context, String profileName)
	{
		String key = KEYS_PREFIX + profileName;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		
		profiles.remove(key);
		editor.remove(key);
		editor.commit();
	}
	
	public AgoniaGamesStats getStatsOf(String profileName)
	{
		String key = KEYS_PREFIX + profileName;
		
		return profiles.get(key);
	}
	
	private AgoniaGamesStats getProfile(Player p)
	{
		String profKey;
		AgoniaGamesStats profile;
		
		if (p.isRealPlayer())
		{
			profKey = KEYS_PREFIX + p.getName();
		}
		else 
		{
			profKey = pszAI_KEYS[p.getMode()];
		}
		
		if (profiles.containsKey(profKey))
		{
			profile = profiles.get(profKey);
		}
		else 
		{
			profile = new AgoniaGamesStats();
			profiles.put(profKey, profile);
		}
		
		return profile;
	}
	
	public void onSetFinished(List<Player> players, List<Integer> winnerTeams)
	{
		int gameType = players.size() - 2;
		
		for (Player player : players)
		{
			AgoniaGamesStats prof = getProfile(player);
			
			if (winnerTeams.contains(player.getTeam()))
			{
				prof.wonSet(gameType);
			}
			else 
			{
				prof.lostSet(gameType);
			}
		}
	}
	
	@Override
	public void onDrawFromDeck(Player who, int n)
	{
		if (n == Agonia.START_DEAL_COUNT)
		{
			return;
		}
		
		AgoniaGamesStats prof = getProfile(who);
		
		if (n > 1)
		{
			prof.drewFromSevens(n);
		}
		else 
		{
			prof.drewCard();
		}
	}

	@Override
	public void onPlay(Player who, Card c)
	{
		AgoniaGamesStats prof = getProfile(who);
		
		prof.cardPlayed(c);
	}

	@Override
	public void onGameFinished(List<Player> players)
	{
		Player winner = players.get(0).getGame().getWinner(); // bad bad bad implementation...
		int winnerTeam = winner.getTeam();
		int gameType = players.size() - 2;
		
		for (Player player : players)
		{
			AgoniaGamesStats prof = getProfile(player);
			
			prof.addPoints(player.getScore());
			
			if (player.getTeam() == winnerTeam)
			{
				prof.wonGame(gameType);
			}
			else 
			{
				prof.lostGame(gameType);
			}
		}
	}

	@Override
	public void onPasso(Player who)
	{
		AgoniaGamesStats prof = getProfile(who);
		
		prof.passo();
	}
	
	@Override
	public void initGameParams(int cPlayers, List<Card> stackTopCards)
	{ }

	@Override
	public void onPlay(Player who, List<Card> cards)
	{ }

	@Override
	public void onPlay(Player who, int cCards)
	{ }

	@Override
	public void onSwitchTurn(Player prev, Player cur)
	{ }

	@Override
	public void onAceSuitSelected(int selectedSuit)
	{ }

	@Override
	public void onDeckFinished(int deckFinishOption)
	{ }
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		for (String key : profiles.keySet())
		{
			AgoniaGamesStats ags = profiles.get(key);
			
			builder
			.append(ags)
			.append('\n');
		}

		return builder.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof AgoniaGamesStatsManager))
		{
			return false;
		}
		AgoniaGamesStatsManager other = (AgoniaGamesStatsManager) obj;

		return id == other.id;
	}
}
