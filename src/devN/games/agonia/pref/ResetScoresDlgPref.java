package devN.games.agonia.pref;

import devN.games.agonia.F;
import devN.games.agonia.R;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
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
			.putInt(F.KEY_P1_WINS, 0)
			.putInt(F.KEY_P2_WINS, 0)
			.commit();
			refreshSummury();
			}
			break;
			
		default:
			break;
		}
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder)
	{
		builder.setMessage(R.string.pref_reset_dlg_message);
		super.onPrepareDialogBuilder(builder);
	}

	public void refreshSummury()
	{
		Resources res = getContext().getResources();
		String summForamt = "%s %c:%d %c:%d";
		String defp1Name = res.getString(R.string.default_p1_name);
		String defp2Name = res.getString(R.string.default_p2_name);
		char wins = res.getString(R.string.wins).charAt(0);
		char points = res.getString(R.string.points).charAt(0);
		
		StringBuilder sb = new StringBuilder(60)
		.append(String.format(summForamt, 
				F.getString(F.KEY_P1_NAME, defp1Name), 
				wins, F.getInt(F.KEY_P1_WINS, 0),
				points, F.getInt(F.KEY_P1_SCORE, 0)))
		.append("\n")
		.append(String.format(summForamt, 
				F.getString(F.KEY_P2_NAME, defp2Name), 
				wins, F.getInt(F.KEY_P2_WINS, 0),
				points, F.getInt(F.KEY_P2_SCORE, 0)))
		;

		setSummary(sb.toString());
	}
}
