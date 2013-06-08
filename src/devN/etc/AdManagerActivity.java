package devN.etc;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.json.JSONObject;
import com.appflood.AppFlood;
import com.appflood.AppFlood.AFEventDelegate;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ironsource.mobilcore.ConfirmationResponse;
import com.ironsource.mobilcore.MobileCore;
import com.ironsource.mobilcore.MobileCore.LOG_TYPE;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class AdManagerActivity extends Activity
{
	public static final long SEC_MILLIS = 1000;
	public static final long MIN_MILLIS = 60 * SEC_MILLIS;
	public static final long HOUR_MILLIS = 60 * MIN_MILLIS;
	public static final long DAY_MILLIS = 24 * HOUR_MILLIS;
	
	public static final long AUTO_FINISH_TIMEOUT = 3500;
	
	/** show ads every {@link #SHOW_AD_PERIOD} days */
	public static final double SHOW_AD_PERIOD = .5;
	
	private static final String KEY_ADS = "key_AdManagerActivity_adStats";
	private static final String KEY_LAST_AD = "key_AdManagerActivity_lastAd";
	
	/** new providers should be inserted in the end of the array */
	private static final String[] AD_PROVIDERS = {"mobilecore", "leadbolt", "appflood_fullscreen"};
	
	private List<ShowAdStats> adStats = null;
	private long lastAdShown = 0;
	
	private static final String appid = "2vIcTLl5OAAhgvFv";
	private static final String secretkey = "y854qZjI9c0L517f9a59";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		MobileCore.init(this, "17SC2L1SS0W36F91XGHU8RUIV8MEK", LOG_TYPE.PRODUCTION);
		
		AppFlood.initialize(this, appid, secretkey, AppFlood.AD_ALL);
		
		initAdStats();

		long remaining, now = Calendar.getInstance().getTime().getTime();
		remaining = now - lastAdShown - (int) (DAY_MILLIS * SHOW_AD_PERIOD);
		
		if (remaining > 0)
		{
			Collections.sort(adStats, Collections.reverseOrder());
			showAd(adStats.get(0), 0);
		}
		else 
		{
			DBGLog.iads("remaining ~" + (-remaining / HOUR_MILLIS) + " hours for next intersial ad.");
			finish();
		}
	}

	@Override
	protected void onDestroy()
	{		
		AppFlood.destroy();
		
		saveAdStats();
		
		super.onDestroy();
	}
	
	private boolean closeOnNextFocus = false;
	private final Handler handler = new Handler();

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus)
		{
			if (closeOnNextFocus)
			{
				finish();
			}
			else
			{
				handler.postDelayed(finishRunnable, AUTO_FINISH_TIMEOUT);
			}
		}
		else
		{
			handler.removeCallbacks(finishRunnable);
		}
	}

	private void showAd(final ShowAdStats ad, final int index)
	{
		DBGLog.iads(ad.toString());
		
		if ("mobilecore".equals(ad.provider))
		{
			MobileCore.showOfferWall(this, new ConfirmationResponse(){
			
				@Override
				public void onConfirmation(TYPE resp)
				{
					ad.shown();
					lastAdShown = ad.lastShown.getTime();

					if (resp == TYPE.AGREED)
					{
						ad.clicked();
					}

					finish();
				}
			});
		}
		else if ("leadbolt".equals(ad.provider))
		{// Deprecated
			ad.shown();
			if (index < adStats.size() - 1)
			{
				showAd(adStats.get(index + 1) , index + 1);
			}
			else 
			{
				finish();						
			}
		}
		else if ("appflood_fullscreen".equals(ad.provider))
		{
			AppFlood.setEventDelegate(new AFEventDelegate(){
				
				@Override
				public void onClose(JSONObject arg0)
				{
					ad.shown();
					lastAdShown = ad.lastShown.getTime();
			
					finish();
				}
				
				@Override
				public void onClick(JSONObject arg0)
				{
					ad.clicked();
				}
			});

			AppFlood.showFullScreen(this);
		}
	}

	private void initAdStats()
	{
		SharedPreferences pref = getPreferences(MODE_PRIVATE);

		if (pref.contains(KEY_ADS))
		{
			Gson gson = new Gson();

			Type collectionType = new TypeToken
								<Collection<AdManagerActivity.ShowAdStats>>(){}.getType();

			adStats = gson.fromJson(pref.getString(KEY_ADS, ""), collectionType);
			
			DBGLog.iads("loaded " + adStats.size() + " ads");
		}
		else
		{
			DBGLog.iads("new ads stats will loaded");
			adStats = new ArrayList<AdManagerActivity.ShowAdStats>();
		}
		
		int n = adStats.size();
		
		for (int i = AD_PROVIDERS.length - 1; i >= n; i--)
		{// adding new/missing providers
			adStats.add(new ShowAdStats(AD_PROVIDERS[i]));
			DBGLog.iads("added " + AD_PROVIDERS[i] + " ad");
		}
		
		lastAdShown = pref.getLong(KEY_LAST_AD, 0);
	}
	
	private void saveAdStats()
	{
		Gson gson = new Gson();
			
		getPreferences(MODE_PRIVATE)
		.edit()
		.putString(KEY_ADS, gson.toJson(adStats))
		.putLong(KEY_LAST_AD, lastAdShown)
		.commit();		
	}
	
	private final Runnable finishRunnable =  new Runnable(){
		public void run()
		{
			finish();
		}
	};

	private class ShowAdStats implements Comparable<ShowAdStats>
	{
		private Date lastShown;
		private boolean clicked;
		private String provider;
		
		@SuppressWarnings("unused")
		public ShowAdStats()
		{
			this("none");
		}
		
		public ShowAdStats(String provider)
		{
			this.provider = provider;
			lastShown = Calendar.getInstance().getTime();
			clicked = false;
		}
		
		public void shown()
		{
			lastShown = Calendar.getInstance().getTime();
			clicked = false;
		}
		
		public void clicked()
		{
			clicked = true;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((provider == null) ? 0 : provider.hashCode());
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
			if (!(obj instanceof ShowAdStats))
			{
				return false;
			}
			ShowAdStats other = (ShowAdStats) obj;
			
			return provider.equals(other.provider);
		}

		@Override
		public int compareTo(ShowAdStats another)
		{	
			return Long.valueOf(getPriority())
					.compareTo(Long.valueOf(another.getPriority()));
		}
		
		private long getPriority()
		{
			long pr;
			Date now = Calendar.getInstance().getTime();
			
			pr = (clicked ? 2L : 1L) * (now.getTime() - lastShown.getTime());
			
			return pr;
		}

		@Override
		public String toString()
		{
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

			StringBuilder builder = new StringBuilder();
			builder
			.append(sdf.format(lastShown))
			.append(" | ")
			.append(provider);
			
			return builder.toString();
		}
	}
}