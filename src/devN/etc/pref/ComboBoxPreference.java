package devN.etc.pref;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class ComboBoxPreference extends ListPreference
{
	public ComboBoxPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public void setValue(String value)
	{
		super.setValue(value);
		int i = findIndexOfValue(value);
		setSummary(getEntries()[i]);
	}
}
