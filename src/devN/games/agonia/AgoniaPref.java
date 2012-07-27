package devN.games.agonia;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class AgoniaPref extends PreferenceActivity 
{	
	private final static int NAME_MAX_LENGTH = 9;
	private final static int NAME_MIN_LENGTH = 3;
	private final static String REGEX_NAME_FILTER = "(^[a-zA-Z0-9\\p{InGreek}]{" + NAME_MIN_LENGTH + "," + NAME_MAX_LENGTH + "}$)";
	
	/* (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.pref);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		
		Preference.OnPreferenceChangeListener namesOnSumm = new Preference.OnPreferenceChangeListener(){
			
			@Override
			public boolean onPreferenceChange(Preference pref, Object newValue)
			{
				if (!(newValue instanceof String))
				{
					return false;
				}
				else if (((String) newValue).length() == 0)
				{
					return false;
				}
				
				String newSummury;
				
				if (pref instanceof ListPreference)
				{// Difficult mode, combobox behavior
					ListPreference lpref = (ListPreference) pref;
					int ientry = lpref.findIndexOfValue((String) newValue);
					newSummury = lpref.getEntries()[ientry].toString();
				}
				else 
				{//Player names
					newSummury = (String) newValue;
					
					//Filter
					if (!newSummury.matches(REGEX_NAME_FILTER))
					{
						Toast.makeText(pref.getContext(), 
						               getString(R.string.pref_name_aborted_message, NAME_MIN_LENGTH, NAME_MAX_LENGTH), 
						               Toast.LENGTH_SHORT).show();
						return false;
					}
				}
				
				pref.setSummary(newSummury);
			
				return true; 			
			}
		};
		
		Preference p = findPreference(getString(R.string.key_p1_name));
		p.setSummary(F.getString(getString(R.string.key_p1_name), ""));
		p.setOnPreferenceChangeListener(namesOnSumm);
	
		p = findPreference(getString(R.string.key_p2_name));
		p.setSummary(F.getString(getString(R.string.key_p2_name), ""));
		p.setOnPreferenceChangeListener(namesOnSumm);
		
		ListPreference lp = (ListPreference) findPreference(getString(R.string.key_difficult));
		lp.setSummary(lp.getEntry());
		lp.setOnPreferenceChangeListener(namesOnSumm);
	}
	
}
