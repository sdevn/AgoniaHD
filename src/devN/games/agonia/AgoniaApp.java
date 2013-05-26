package devN.games.agonia;

import android.app.Application;

public class AgoniaApp extends Application
{
	private AgoniaGamesStatsManager manager;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		manager = AgoniaGamesStatsManager.create(getApplicationContext());
	}

	public AgoniaGamesStatsManager getManager()
	{
		return manager;
	}
}
