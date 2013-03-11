package devN.games.agonia;

import java.util.Map;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public class F
{
	public static String KEY_P1_NAME;
	public static String KEY_P2_NAME;
	public static String KEY_P3_NAME;
	public static String KEY_P4_NAME;
	public static String KEY_P1_SCORE;
	public static String KEY_P2_SCORE;
	public static String KEY_P1_WINS;
	public static String KEY_P2_WINS;
	public static String KEY_IS_NINE_SPECIAl;
	public static String KEY_DIFFICULT_MODE;
	public static String KEY_DECK_FINISH;
	public static String KEY_COLOR_HINTS;
	public static String KEY_ACE_ON_ACE;
	public static String KEY_ACE_FINISH;
	public static String KEY_OLD_DECK;
	public static String KEY_OLD_ANIM;
	
	public static SharedPreferences settings;
	private static Resources resources;
	
	public F(Activity a)
	{
		KEY_P1_NAME = a.getString(R.string.key_p1_name);
		KEY_P2_NAME = a.getString(R.string.key_p2_name);
		KEY_P3_NAME = a.getString(R.string.key_p3_name);
		KEY_P4_NAME = a.getString(R.string.key_p4_name);
		KEY_P1_SCORE = a.getString(R.string.key_p1_score);
		KEY_P2_SCORE = a.getString(R.string.key_p2_score);
		KEY_P1_WINS = a.getString(R.string.key_p1_wins);
		KEY_P2_WINS = a.getString(R.string.key_p2_wins);
		KEY_IS_NINE_SPECIAl = a.getString(R.string.key_is_nine_special);
		KEY_DIFFICULT_MODE = a.getString(R.string.key_difficult);
		KEY_DECK_FINISH = a.getString(R.string.key_deck_finish);
		KEY_COLOR_HINTS = a.getString(R.string.key_color_hints);
		KEY_ACE_ON_ACE = a.getString(R.string.key_ace_on_ace);
		KEY_ACE_FINISH = a.getString(R.string.key_ace_finish);
		KEY_OLD_DECK = a.getString(R.string.key_old_deck);
		KEY_OLD_ANIM = a.getString(R.string.key_old_anim);
		
		resources = a.getResources();
		settings = PreferenceManager.getDefaultSharedPreferences(a);
	}
	
	public static boolean contains(String key)
	{
		return settings.contains(key);
	}

	public static Editor edit()
	{
		return settings.edit();
	}

	public static Map<String, ?> getAll()
	{
		return settings.getAll();
	}

	public static boolean getBoolean(String key, boolean defValue)
	{
		return settings.getBoolean(key, defValue);
	}

	public static float getFloat(String key, float defValue)
	{
		return settings.getFloat(key, defValue);
	}

	public static int getInt(String key, int defValue)
	{
		return settings.getInt(key, defValue);
	}

	public static long getLong(String key, long defValue)
	{
		return settings.getLong(key, defValue);
	}

	public static Resources getResources()
	{
		return resources;
	}

	public static String getString(String key, String defValue)
	{
		return settings.getString(key, defValue);
	}

	public static void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener)
	{
		settings.registerOnSharedPreferenceChangeListener(listener);	
	}

	public static void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener)
	{
		settings.unregisterOnSharedPreferenceChangeListener(listener);
	}	
}
