package devN.etc;

import static android.view.ViewGroup.LayoutParams.FILL_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import devN.games.agonia.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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
	
	@SuppressWarnings("deprecation")
	public static void embedAd(Dialog dialog, Activity activity)
	{
		AdView ad = (AdView) dialog.findViewById(R.id.adViewInDialog);
		Window dw = dialog.getWindow();
		FrameLayout rootView = (FrameLayout) dw.findViewById(Window.ID_ANDROID_CONTENT);
		
		ViewGroup dialogRoot = rootView;
		
		while (!(dialogRoot instanceof LinearLayout))
		{
			View child = dialogRoot.getChildAt(0);
			if (child instanceof ViewGroup)
			{
				dialogRoot = (ViewGroup) child;
			}
			else 
			{
				dialogRoot = null;
				break;
			}
		}
		
		if (ad == null)
		{	
			View adContainer = activity.getLayoutInflater().inflate(R.layout.ad_in_dialog2, null);
			ad = (AdView) adContainer.findViewById(R.id.adViewInDialog);
			
			if (dialogRoot != null)
			{
				dialogRoot.addView(adContainer);
			}
			else 
			{
				WindowManager.LayoutParams wlp = dw.getAttributes();
				
				rootView.measure(WRAP_CONTENT, WRAP_CONTENT);
	
				DisplayMetrics dm = activity.getResources().getDisplayMetrics();
				int maxHeight = dm.heightPixels;
				int newHeight = rootView.getMeasuredHeight() + (int) (55 * dm.density);						
		
				if (newHeight <= maxHeight)
				{				
					FrameLayout.LayoutParams lParams =  new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, 
																					Gravity.BOTTOM);
					wlp.height = newHeight;
					wlp.width = FILL_PARENT; // align dialog in center of screen
					dw.setAttributes(wlp);
					
					dialog.addContentView(adContainer, lParams);
				}
				else 
				{// can't embed ad, its not fit on screen... :(
					return;
				}
			}
		}
		
		rootView.requestLayout();
		
		ad.loadAd(new AdRequest()
					.addTestDevice(AdRequest.TEST_EMULATOR)
					.addTestDevice("3D600443E10074BF066B583607BB1B80")	// neo V
					.addTestDevice("2916D128B56D865EEFC199FD7084CBE6")	// ray with CM
					.addTestDevice("0D03ACAC00FC3E232AC476294FF0A41D")	// geny nexus one
					.addTestDevice("398795D7A94672AE1C66702B849E7DA9"));// geny nexus S
	}
}
