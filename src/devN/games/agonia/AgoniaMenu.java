package devN.games.agonia;

import java.util.Random;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import devN.etc.DevnDialogUtils;

public class AgoniaMenu extends Activity
{
	private static final int DIALOG_ABOUT_ID = 0;
	private static final int DIALOG_COMMING_SOON_ID = 1;
	private static final int DIALOG_RATE_ID = 2;
	private static final int DIALOG_WHATS_NEW_ID = 3;
	
	private static final double CHANCE_COMMING_DIALOG = 0.0D / 9.5D;
	
	private ImageButton ibtRate;
	private ImageButton ibtFacebook;
	private ImageButton ibtStats;
	private Button btnPref;
	private Button btnGame;
	private Button btnHow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);

		if (F.settings == null)
		{
			new F(this);
		}
	
		btnPref = (Button) findViewById(R.id.btPref);
		btnGame = (Button) findViewById(R.id.btGame);
		btnHow = (Button) findViewById(R.id.btHow);
		
		ImageButton about = (ImageButton) findViewById(R.id.ibtAbout);
		ImageButton exit = (ImageButton) findViewById(R.id.ibtExit);
		
		ibtRate = (ImageButton) findViewById(R.id.ibtRate);
		ibtFacebook = (ImageButton) findViewById(R.id.ibtFacebook);
		ibtStats = (ImageButton) findViewById(R.id.ibtStats);
		
		btnPref.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(AgoniaMenu.this, AgoniaPref.class));
			}
		});

		btnGame.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(AgoniaMenu.this, GameSetActivity.class));
				
				/* v2.1 */
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			}
		});

		btnHow.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(AgoniaMenu.this, HowToPlay.class));
				overridePendingTransition(R.anim.right_to_original_pos, R.anim.out_to_left);
			}
		});
		
		about.setOnClickListener(new OnClickListener(){

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v)
			{
				showDialog(DIALOG_ABOUT_ID);
			}
		});

		exit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				atExit();
			}
		});
		
		ibtRate.setOnClickListener(oclRate);

		ibtFacebook.setVisibility(View.GONE);

		// v2.3
		ibtStats.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(AgoniaMenu.this, GameStatsActivity.class));
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			}
		});
		
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int screenSize = (int) (dm.heightPixels / dm.density);

		if (screenSize <= 460)
		{	// too small screen height
			exit.setVisibility(View.GONE);
		}
		AgoniaGamesStatsManager.create(this);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		if (hasFocus)
		{
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			boolean once = sharedPrefs.getBoolean(getString(R.string.key_dlg_whats_new_once), false);
			
			if (!once)
			{
				showDialog(DIALOG_WHATS_NEW_ID);
				
				sharedPrefs
				.edit()
				.putBoolean(getString(R.string.key_dlg_whats_new_once), true)
				.commit();
			}
			
			AgoniaGamesStatsManager.getManager().saveStats(AgoniaMenu.this, false);	//2.3
		}
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	public void onBackPressed()
	{
        atExit();
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		Dialog dialog = null;

		switch (id)
		{
		case DIALOG_ABOUT_ID:
			{
			String message = getString(R.string.dlg_about_message);

			WebView wv = new WebView (this);
			wv.loadData(message, "text/html", "utf-8");
			wv.setBackgroundColor(getResources().getColor(R.color.tsoha));
			wv.getSettings().setBuiltInZoomControls(false);
			wv.getSettings().setDefaultFontSize(14);

			AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setTitle("About")
			.setView(wv)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{ }
			})
			.setNeutralButton("what's new", new DialogInterface.OnClickListener(){
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					if (which == DialogInterface.BUTTON_NEUTRAL)
					{
						new Handler().postDelayed(new Runnable(){
							@SuppressWarnings("deprecation")
							public void run()
							{
								showDialog(DIALOG_WHATS_NEW_ID);
							}
						}, 300);
					}
				}
			})
			;
			dialog = builder.create();
			}
			break;
		
		case DIALOG_COMMING_SOON_ID:
			{
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setTitle("Comming soon!")
			.setMessage(R.string.dlg_comming_soon_message)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					finish();
					
					/* v2.1 */
					overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener(){
				
				@Override
				public void onCancel(DialogInterface dialog)
				{	
					finish();
					
					/* v2.1 */
					overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				}
		
			})
			;
			dialog = builder.create();
			}
			break;
		case DIALOG_RATE_ID:
			{
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setTitle(R.string.dlg_rate_title)
			.setMessage(R.string.dlg_rate_message)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					if (which == DialogInterface.BUTTON_POSITIVE)
					{
						try
						{
							Uri appUri = Uri.parse("market://details?id=" + getPackageName());
							Intent intent = new Intent(Intent.ACTION_VIEW, appUri);
							startActivity(intent);
							
							/* v2.1 */
							overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
						}
						catch (Exception ex)
						{
							Toast.makeText(AgoniaMenu.this, "No Google Play Store installed!!", Toast.LENGTH_SHORT).show();
						}
					}
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{ /* just close the dialog */}
			})
			;
			dialog = builder.create();
			}
			break;
			
		case DIALOG_WHATS_NEW_ID:
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle("What\'s new")
				.setMessage(R.string.dlg_whats_new_message)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{ }
				})
/*				
				.setNeutralButton(R.string.menu_btn_pref, new DialogInterface.OnClickListener(){
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{ 
						if (which == DialogInterface.BUTTON_NEUTRAL)
						{
							btnPref.performClick();
						}
					}
				})
*/				
				.setCancelable(false)
				;
				dialog = builder.create();
			}
			break;
			
		default:
			{
			dialog = null;
			}
			break;
		}

		return dialog;
	}
	
	@Override
	@Deprecated
	protected void onPrepareDialog(int id, Dialog dialog)
	{
		DevnDialogUtils.customize(dialog);
		
		super.onPrepareDialog(id, dialog);
	}

	@SuppressWarnings("deprecation")
	private void atExit()
	{
		Random random = new Random();
		
		AgoniaGamesStatsManager.getManager().saveStats(this, true);	// v2.3
		
		if (random.nextDouble() < CHANCE_COMMING_DIALOG)
		{
			showDialog(DIALOG_COMMING_SOON_ID);
		}
		else 
		{
			finish();
			
			/* v2.1 */
			overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		}
	}
	
	private View.OnClickListener oclRate = new View.OnClickListener(){
		
		@SuppressWarnings("deprecation")
		@Override
		public void onClick(View v)
		{
			showDialog(DIALOG_RATE_ID);
		}
	};
}
