package devN.games.agonia;

import static devN.games.agonia.Const.CLOUD_SLOT_ELO;
import java.util.Random;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.appstate.OnStateLoadedListener;
import com.google.android.gms.games.OnSignOutCompleteListener;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.tapjoy.TapjoyConnect;
import com.tapjoy.TapjoyEarnedPointsNotifier;
import com.tapjoy.TapjoyFullScreenAdNotifier;
import com.tapjoy.TapjoyNotifier;
import devN.etc.DBGLog;
import devN.etc.DevnDialogUtils;
import devN.games.GameSet;

public class AgoniaMenu extends BaseGameActivity implements OnStateLoadedListener
{
	private static final int DIALOG_ABOUT_ID = 0;
	private static final int DIALOG_COMMING_SOON_ID = 1;
	private static final int DIALOG_RATE_ID = 2;
	private static final int DIALOG_WHATS_NEW_ID = 3;
	@SuppressWarnings("unused")
	private static final int DIALOG_PLAYER_ID = 4;
	private static final int DIALOG_EXTRAGAMES_ID = 5;
	
	private static final double CHANCE_COMMING_DIALOG = 0.0D / 9.5D;
	
	private ImageButton ibtRate;
	private ImageButton ibtFacebook;
	private ImageButton ibtStats;
	private ImageButton ibtnExit;
	private Button btnPref;
	private Button btnGame;
	private Button btnHow;
	
	/** v3.0 */
	private static final int RC_UNUSED = 5001;
	
	private Button btnOnlineGame;
	private ImageButton ibtnPlayer;
	private ImageButton ibtnLeaderboardsElo;
	private ImageButton ibtnLeaderboardsStreak;
	private ImageButton ibtnLeaderboardWins;
	private ImageButton ibtnLeaderboardPoints;
	private ImageButton ibtnSignout;
	
	private EloEntity myEloEntity;
	private int extraGames = 0;
	
	public AgoniaMenu()
	{
		super(BaseGameActivity.CLIENT_APPSTATE | BaseGameActivity.CLIENT_GAMES);
	} 

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
		
		TapjoyConnect.requestTapjoyConnect(getApplicationContext(), 
				Const.TAPJOY_APPID, 
				Const.TAPJOY_SECRET, 
				null);
		
		TapjoyConnect.getTapjoyConnectInstance().setEarnedPointsNotifier(new TapjoyEarnedPointsNotifier() {
			@Override
			public void earnedTapPoints(int amount)
			{
				Toast.makeText(AgoniaMenu.this, "You get " + amount + " more extra games!" ,  
						Toast.LENGTH_SHORT).show();
			}
		});
	
		btnPref = (Button) findViewById(R.id.btPref);
		btnGame = (Button) findViewById(R.id.btGame);
		btnHow = (Button) findViewById(R.id.btHow);
		
		ImageButton about = (ImageButton) findViewById(R.id.ibtAbout);
		
		ibtnExit = (ImageButton) findViewById(R.id.ibtExit);
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

		ibtnExit.setOnClickListener(new OnClickListener(){

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
//			exit.setVisibility(View.GONE);
		}
		
		//v3.0
		ibtnPlayer = (ImageButton) findViewById(R.id.ibtn_player);
		ibtnPlayer.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v)
			{
				if (myEloEntity != null)
				{
					Dialog dialog = new AlertDialog.Builder(AgoniaMenu.this)
					.setTitle(getGamesClient().getCurrentPlayer().getDisplayName())
					.setMessage(myEloEntity.toLocalizedLongString(
							getString(R.string.elo_localized_long_format), 
							getString(R.string.elo_date_format), 
							extraGames, 
							new String[] {getString(R.string.lost), getString(R.string.won)} ))
					.setPositiveButton(android.R.string.ok, null)
					.create();
					
					dialog.show();
					
					DevnDialogUtils.customize(dialog);
				}
				else 
				{
					onSignInFailed();
				}
			}
		});
		
		btnOnlineGame = (Button) findViewById(R.id.btnOnlineGame);
		btnOnlineGame.setOnClickListener(new OnClickListener(){
			
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v)
			{
				if (!getGamesClient().isConnected() || !isSignedIn())
				{
					beginUserInitiatedSignIn();
				}
				else if (myEloEntity != null)
				{
					if (myEloEntity.getTodayGames() < Const.MAX_DAILY_GAMES + extraGames)
					{
						startOnlineGame();
					}
					else
					{
						showDialog(DIALOG_EXTRAGAMES_ID);
					}
				}
				else 
				{
					onSignInFailed();
				}
			}
		});
		
		ibtnLeaderboardsElo = (ImageButton) findViewById(R.id.ibtn_leaderboards_elo);
		ibtnLeaderboardsElo.setOnClickListener(oclLeaderboards);
		
		ibtnLeaderboardsStreak = (ImageButton) findViewById(R.id.ibtn_leaderboards_winning_streak);
		ibtnLeaderboardsStreak.setOnClickListener(oclLeaderboards);
		
		ibtnLeaderboardWins = (ImageButton) findViewById(R.id.ibtn_leaderboards_wins);
		ibtnLeaderboardWins.setOnClickListener(oclLeaderboards);
		
		ibtnLeaderboardPoints = (ImageButton) findViewById(R.id.ibtn_leaderboards_points);
		ibtnLeaderboardPoints.setOnClickListener(oclLeaderboards);
		
		ibtnSignout = (ImageButton) findViewById(R.id.ibtn_signout);
		ibtnSignout.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v)
			{
				getGamesClient().signOut(new OnSignOutCompleteListener(){
					
					@Override
					public void onSignOutComplete()
					{
						Toast.makeText(AgoniaMenu.this, R.string.toast_signout, Toast.LENGTH_SHORT).show();
						findViewById(R.id.menu_left_side_icons_container).setVisibility(View.GONE);
						ibtnExit.performClick();
					}
				});
			}
		});
	}
	
	@Override
	protected void onResume()
	{ 
		TapjoyConnect.getTapjoyConnectInstance().getTapPoints(new TapjoyNotifier()
		{
			@Override
			public void getUpdatePointsFailed(String error)
			{
				extraGames = 0;
				DBGLog.dbg("get tap points error!! " + error);
			}
			
			@Override
			public void getUpdatePoints(String currencyName, int pointTotal)
			{
				extraGames = pointTotal;
				DBGLog.dbg("tap points " + extraGames);
			}
		});
		
		if (getGamesClient().isConnected())
		{
			getAppStateClient().loadState(AgoniaMenu.this, Const.CLOUD_SLOT_ELO);
		}
		
		super.onResume();
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
			
			((AgoniaApp)getApplication())
			.getManager().saveStats(AgoniaMenu.this, false);	//2.4
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
				
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
					// v2.5 modified
					@Override
					public void onClick(DialogInterface dialog, int which)
					{ }
				})
				.setPositiveButton("Join to beta testers!", new DialogInterface.OnClickListener(){
					// v2.5 added
					@Override
					public void onClick(DialogInterface dialog, int which)
					{ 
						String url = "https://plus.google.com/u/0/communities/118106152681488616591";
						Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
						startActivity(i);
					}
				})
				.setCancelable(false)
				;
				dialog = builder.create();
			}
			break;
			
		case DIALOG_EXTRAGAMES_ID:
			{
				DialogInterface.OnClickListener docl = new DialogInterface.OnClickListener(){
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						switch (which)
						{
						case DialogInterface.BUTTON_POSITIVE:
							TapjoyConnect.getTapjoyConnectInstance().getFullScreenAd(new TapjoyFullScreenAdNotifier()
							{
								@Override
								public void getFullScreenAdResponseFailed(int error)
								{
									runOnUiThread(new Runnable(){
										public void run()
										{
											Toast.makeText(AgoniaMenu.this,
													"Offer unavailable, please choose an other offer.",
													Toast.LENGTH_LONG).show();
										}
									});
								}
								
								@Override
								public void getFullScreenAdResponse()
								{
									TapjoyConnect.getTapjoyConnectInstance().showFullScreenAd();
								}
							});
							break;
						case DialogInterface.BUTTON_NEUTRAL:
							TapjoyConnect.getTapjoyConnectInstance().showOffers();
							break;
						default:
							break;
						}
					}
				};
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle(R.string.dlg_extra_games_title)
				.setMessage(R.string.dlg_extra_games_message)
				.setPositiveButton("Offer 1", docl)
				.setNeutralButton("Offer 2", docl)
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
		
		((AgoniaApp)getApplication())
		.getManager().saveStats(this, true);	// v2.4
		
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

	private View.OnClickListener oclLeaderboards = new View.OnClickListener(){
		
		@Override
		public void onClick(View v)
		{
			startActivityForResult(getGamesClient().getLeaderboardIntent((String) v.getTag()), RC_UNUSED);
		}
	};
	
	@Override
	public void onSignInFailed()
	{
		Toast.makeText(this, R.string.toast_signin, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onSignInSucceeded()
	{
		getAppStateClient().loadState(AgoniaMenu.this, Const.CLOUD_SLOT_ELO);
		findViewById(R.id.menu_left_side_icons_container).setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onStateLoaded(int statusCode, int stateKey, byte[] localData)
	{
		if (statusCode == AppStateClient.STATUS_OK)
		{
			myEloEntity = new EloEntity(localData);
		}
		else if (statusCode == AppStateClient.STATUS_STATE_KEY_NOT_FOUND) 
		{
			myEloEntity = new EloEntity();
			getAppStateClient().updateState(CLOUD_SLOT_ELO, myEloEntity.getBytes());
		}
		else 
		{
			myEloEntity = new EloEntity();
			Toast.makeText(this, "Connection error!! " + statusCode, Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onStateConflict(int stateKey, String resolvedVersion, byte[] localData, byte[] serverData)
	{
		switch (stateKey)
		{
		case Const.CLOUD_SLOT_ELO:
			EloEntity localEntity = new EloEntity(localData);
			EloEntity serverEntity = new EloEntity(serverData);
			
			if (localEntity.getGamesHistory().size() > serverEntity.getGamesHistory().size())
			{
				getAppStateClient().resolveState(this, stateKey, resolvedVersion, localData);
			}
			else 
			{
				getAppStateClient().resolveState(this, stateKey, resolvedVersion, serverData);
			}
			break;

		default:
			break;
		}
	}

	private void startOnlineGame()
	{
		Intent intent = new Intent(AgoniaMenu.this, AgoniaGame.class);
		int modes[] = new int[3];
		GameSet set = new GameSet(GameSet.TYPE_WINS, 1);

		intent.putExtra(GameSetActivity.ID_PLAYERS_NUM, 2);

		modes[0] = AgoniaAI.MODE_ONLINE;
		
		intent.putExtra(GameSetActivity.ID_AI_MODES, modes);
		intent.putExtra(GameSetActivity.ID_NEW_SET, true);
		intent.putExtra(GameSetActivity.ID_GAMESET, (Parcelable) set);
		intent.putExtra(Const.ID_IS_ONLINE, true);
		
		startActivity(intent);
		
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
}
