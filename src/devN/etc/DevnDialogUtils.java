package devN.etc;

import devN.games.agonia.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.Button;

public final class DevnDialogUtils
{
	/**
	 * v2.1 added
	 * Call it on {@link android.app.Activity#onPrepareDialog(int, Dialog)}
	 */
	public static void customize(Dialog dialog)
	{
		try
		{
			for (int i = DialogInterface.BUTTON_NEUTRAL; i <= DialogInterface.BUTTON_POSITIVE; i++)
			{
				Button b = ((AlertDialog) dialog).getButton(i);
				if (b != null)
				{
					b.setBackgroundResource(R.drawable.sel_bkgr_btn);
				}
			}
		}
		catch (Exception ex)
		{
			DBGLog.dbg(ex.toString());
		}
	}
}
