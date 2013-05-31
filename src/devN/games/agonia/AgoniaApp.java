package devN.games.agonia;

import devN.games.GameSet;
import android.app.Application;

public class AgoniaApp extends Application
{
	private AgoniaGamesStatsManager manager;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		manager = AgoniaGamesStatsManager.create(getApplicationContext());
		
		GameSet.infoSetType[GameSet.TYPE_POINTS] = getResources().getString(R.string.points);
		GameSet.infoSetType[GameSet.TYPE_WINS] = getResources().getString(R.string.wins);
		
		GameSet.winnerTeam = getResources().getString(R.string.winner_team);
		
		String[] aiModes = getResources().getStringArray(R.array.difficult_modes);
		
		for (int i = 0; i < aiModes.length; i++)
		{
			AgoniaAI.AgoniaAIBuilder.PSZAI_STRINGS[i] = aiModes[i];
		}
	}

	public AgoniaGamesStatsManager getManager()
	{
		return manager;
	}
}
