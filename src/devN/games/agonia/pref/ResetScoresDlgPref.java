package devN.games.agonia.pref;

import devN.games.agonia.F;
import devN.games.agonia.R;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class ResetScoresDlgPref extends DialogPreference
{
	public ResetScoresDlgPref(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		refreshSummury();
	}

	public ResetScoresDlgPref(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		refreshSummury();
	}

	/* (non-Javadoc)
	 * @see android.preference.DialogPreference#onClick(android.content.DialogInterface, int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		super.onClick(dialog, which);
		
		switch (which)
		{
		case DialogInterface.BUTTON_NEGATIVE:
			
			break;
			
		case DialogInterface.BUTTON_POSITIVE:
			{
			F.edit()
			.putInt(F.KEY_P1_SCORE, 0)
			.putInt(F.KEY_P2_SCORE, 0)
			.commit();
			refreshSummury();
			}
			break;
			
		default:
			break;
		}
	}

	/* (non-Javadoc)
	 * @see android.preference.DialogPreference#onPrepareDialogBuilder(android.app.AlertDialog.Builder)
	 */
	@Override
	protected void onPrepareDialogBuilder(Builder builder)
	{
		builder.setMessage(R.string.pref_reset_dlg_message);
		super.onPrepareDialogBuilder(builder);
	}

	public void refreshSummury()
	{
		StringBuilder sb = new StringBuilder(10)
		.append(F.getInt(F.KEY_P1_SCORE, 0) + " - ")
		.append(F.getInt(F.KEY_P2_SCORE, 0));

		setSummary(sb.toString());
	}
}
