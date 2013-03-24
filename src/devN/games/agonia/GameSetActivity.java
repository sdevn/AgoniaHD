package devN.games.agonia;

import static devN.games.agonia.AgoniaPref.REGEX_NAME_FILTER;
import static devN.games.agonia.AgoniaPref.NAME_MIN_LENGTH;
import static devN.games.agonia.AgoniaPref.NAME_MAX_LENGTH;
import java.util.ArrayList;
import com.adsdk.sdk.Ad;
import com.adsdk.sdk.AdListener;
import com.adsdk.sdk.AdManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import devN.etc.DBGLog;
import devN.etc.DevnDialogUtils;
import devN.etc.TypefaceUtils;
import devN.games.GameSet;

public class GameSetActivity extends Activity
{
	// TODO: Save / Load GameSet
	// TODO: Statistics for GameSets wins/loses
	
	public static final int DIALOG_SET_FINISH_ID	= 0;
	public static final int DIALOG_CHANGE_NAME_ID	= 1;

	private final static int iSET_TYPE_POINTS 	= 0;
	private final static int iSET_TYPE_WINS 	= 1;
	
	public final static int iPLAYERS_NUM_1v1 	= 0;
	public final static int iPLAYERS_NUM_1v1v1 	= 1;
	public final static int iPLAYERS_NUM_2v2 	= 2;
	
	public final static String ID_GAMESET 		= "devN.games.agonia.GAMESET";
	public final static String ID_NEW_SET 		= "devN.games.agonia.NEW_SET";
	public final static String ID_PLAYERS_NUM 	= "devN.games.agonia.PLAYERS_NUM";
	public final static String ID_AI_MODES		= "devN.games.agonia.PLAYERS_AI_MODES";

	private boolean isNewSet;
	private Button btnStart;
	private String changeNameKey;
	private TextView txvCpuToChangeName;
	private String newName;
	private boolean useSFX;
	private MediaPlayer player;
	
	/**
	 * v2.2 <br />
	 * Load if {@link #isNewSet} otherwise Save 
	 */
	private ImageButton ibtLoad;
	
	// NewSet Views
	private ViewFlipper vfpSetTypeSelection;
	private Spinner spnSetType;
	private Spinner spnPlayersNum;
	private Spinner[] aspnAIModes = new Spinner[3];
	private RadioGroup[] argpSetGoal = new RadioGroup[2];
	private TextView[] atxvCpuNames = new TextView[3];
	
	// ContinueSet Views
	private TextView[] atxvPlayerSetInfos = new TextView[5];
	private TextView[] atxvTeamSetInfos = new TextView[3];
	private TextView txvSetInfo;
	private GameSet set;
	private AdManager adManager;	// v2.2 added
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		useSFX = PreferenceManager.getDefaultSharedPreferences(this)
					.getBoolean(getString(R.string.key_game_sfx), true);
		
		Intent intent = getIntent();
		
		isNewSet = intent.getBooleanExtra(ID_NEW_SET, true);
		
		if (isNewSet)
		{
			setContentView(R.layout.new_set);
			newSet();
		}
		else 
		{
			setContentView(R.layout.continue_set);
			set = intent.getParcelableExtra(ID_GAMESET);
			continueSet();
		}
		
		ViewGroup root = (ViewGroup) findViewById(R.id.gameset_root);
		
		TypefaceUtils.setAllTypefaces(root, this, getString(R.string.custom_font));
	}
	
	@Override
	protected void onDestroy()
	{
		releasePlayer();
		
		if (adManager != null)
		{
			adManager.release();
		}
		
		super.onDestroy();
	}

	/* v2.0b added. */
	/** Hopefully it will resolve any NullPointerException on F.get... */
	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		if (hasFocus && F.settings == null)
		{
			new F(this);
		}

		super.onWindowFocusChanged(hasFocus);
	}

	private void playSound(int resId)
	{
		if (!useSFX)
		{
			return;
		}
		
		try
		{
			player = MediaPlayer.create(this, resId);
			player.start();
			player.setOnCompletionListener(mediaPlayerReleaser);
		}
		catch (Exception ex) 
		{
			AgoniaGame.onPlaySoundFail(this, true);
		}
	}
	
	private void continueSet()
	{
		int playerId;
		
		ibtLoad = (ImageButton) findViewById(R.id.set_ibt_save);
		
		btnStart = (Button) findViewById(R.id.set_btn_continue);
		
		txvSetInfo = (TextView) findViewById(R.id.txv_set_info);
		
		atxvPlayerSetInfos[0] = (TextView) findViewById(R.id.txv_set_t1p1);
		atxvPlayerSetInfos[3] = (TextView) findViewById(R.id.txv_set_t1p2);
		atxvPlayerSetInfos[1] = (TextView) findViewById(R.id.txv_set_t2p1);
		atxvPlayerSetInfos[4] = (TextView) findViewById(R.id.txv_set_t2p2);
		atxvPlayerSetInfos[2] = (TextView) findViewById(R.id.txv_set_t3p1);
		
		atxvTeamSetInfos[0] = (TextView) findViewById(R.id.txv_set_t1_score);
		atxvTeamSetInfos[1] = (TextView) findViewById(R.id.txv_set_t2_score);
		atxvTeamSetInfos[2] = (TextView) findViewById(R.id.txv_set_t3_score);

		switch (set.getPlayersCount() - 2)
		{
		case iPLAYERS_NUM_1v1:
			findViewById(R.id.set_team3_container).setVisibility(View.GONE);
			//$FALL-THROUGH$
			
		case iPLAYERS_NUM_1v1v1:
			findViewById(R.id.txv_set_t2p2).setVisibility(View.GONE);
			findViewById(R.id.txv_set_t1p2).setVisibility(View.GONE);

			break;

		case iPLAYERS_NUM_2v2:
			findViewById(R.id.set_team3_container).setVisibility(View.GONE);
			
			break;
		}
		
		for (int team = 1; team <= set.getTeamsCount(); team++)
		{
			Object[] teamInfo = set.teamInfos(team);
			
			atxvTeamSetInfos[team - 1].setText(
					getString(R.string.set_team_info, 
							getString(R.string.wins), teamInfo[0], 
							getString(R.string.points), teamInfo[1]));
			
			for (int i = 0; i < set.getPlayersCount() / set.getTeamsCount(); i++)
			{
				int iPlayerInfo = (team - 1) + i * 3;
				playerId = set.getPlayerByTeam(team, i).getId();
				
				atxvPlayerSetInfos[iPlayerInfo].setText(
						getString(R.string.set_player_info, set.playerInfos(playerId)));
			}
		}
		
		txvSetInfo.setText(getString(R.string.set_info, set.getInfos()));
		
		if (set.isSetFinished())
		{
			btnStart.setText(R.string.set_btn_continue_winner);
		}
		
		btnStart.setOnClickListener(continueSetClickListener);
	}

	private void newSet()
	{
		vfpSetTypeSelection = (ViewFlipper) findViewById(R.id.set_type_selection);
		spnSetType = (Spinner) findViewById(R.id.spn_set_type);
		spnPlayersNum = (Spinner) findViewById(R.id.spn_players_num);
		aspnAIModes[0] = (Spinner) findViewById(R.id.spn_AI_1);
		aspnAIModes[1] = (Spinner) findViewById(R.id.spn_AI_2);
		aspnAIModes[2] = (Spinner) findViewById(R.id.spn_AI_3);
		atxvCpuNames[0] = (TextView) findViewById(R.id.txvCpu1);
		atxvCpuNames[1] = (TextView) findViewById(R.id.txvCpu2);
		atxvCpuNames[2] = (TextView) findViewById(R.id.txvFriendOp);
		btnStart = (Button) findViewById(R.id.set_btn_start);
		ibtLoad = (ImageButton) findViewById(R.id.set_ibt_load);
		
		argpSetGoal[iSET_TYPE_POINTS] = (RadioGroup) vfpSetTypeSelection.getChildAt(iSET_TYPE_POINTS);
		argpSetGoal[iSET_TYPE_WINS] = (RadioGroup) vfpSetTypeSelection.getChildAt(iSET_TYPE_WINS);
		
		vfpSetTypeSelection.showPrevious();
		
		spnSetType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void	onItemSelected(AdapterView<?> parent, View view, int pos, long id)
			{
				if (pos > 0)
				{
					vfpSetTypeSelection.showNext();
				}
				else 
				{
					vfpSetTypeSelection.showPrevious();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{ }
			
		});
		
		spnPlayersNum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void	onItemSelected(AdapterView<?> parent, View view, int pos, long id)
			{
				boolean enabled;
			
				// disabling/enabling 2nd/3rd cpus
				for (int i = 0; i < aspnAIModes.length; i++)
				{
					LinearLayout ll = (LinearLayout) aspnAIModes[i].getParent();
					
					for (int j = 0; j < ll.getChildCount(); j++)
					{
						enabled = pos >= i;
						View v = ll.getChildAt(j);
						v.setEnabled(enabled);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{ }
			
		});
		
		if (F.settings == null)
		{
			new F(GameSetActivity.this); 
		}
		
		atxvCpuNames[0].setText(F.getString(F.KEY_P2_NAME, getString(R.string.cpu1)));
		atxvCpuNames[1].setText(F.getString(F.KEY_P3_NAME, getString(R.string.cpu2)));
		atxvCpuNames[2].setText(F.getString(F.KEY_P4_NAME, getString(R.string.friend)));
		
		for (Spinner spn : aspnAIModes)
		{
			spn.setSelection(AgoniaAI.MODE_MODERATE);
		}
		
		for (TextView txv : atxvCpuNames)
		{
			txv.setOnClickListener(cpuNamesClickListener);
		}
		
		btnStart.setOnClickListener(newSetClickListener);
	}
	
	@Override
	protected Dialog onCreateDialog(int id)
	{
		Dialog dialog = null;

		switch (id)
		{
		case DIALOG_SET_FINISH_ID:
			{
			dialog = makeSetFinishDlg();
			}
			break;
		
		case DIALOG_CHANGE_NAME_ID:
			{
			dialog = makeChangeNameDlg();
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

		switch (id)
		{
		case DIALOG_CHANGE_NAME_ID:
			{
			String title = getString(R.string.dlg_change_name_title, txvCpuToChangeName.getText().toString());
 			dialog.setTitle(title);
 			}
			break;
		}
		
		super.onPrepareDialog(id, dialog);
	}

	private Dialog makeSetFinishDlg()
	{
		// @formatter:off			
		LinearLayout dlgContainer = (LinearLayout) getLayoutInflater().inflate(R.layout.dlg_set_finish, null);
		ImageView dlgIcon = (ImageView) dlgContainer.findViewById(R.id.dlg_set_finish_ic);
		ArrayList<Integer> winners = set.getWinningTeams();
		int soundId = R.raw.win_set;
		
		if (!winners.contains(1))
		{	// looser!
			dlgIcon.setImageResource(R.drawable.ic_thumb_down);
			soundId = R.raw.fail_set;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle(set.winnerString())
		.setView(dlgContainer)
		.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				adManager = new AdManager(GameSetActivity.this, 
								"http://my.mobfox.com/vrequest.php",
								"819563d36a65ebca90245302554ddb33", false);
				
				adManager.setListener(new AdListener(){
					
					@Override
					public void noAdFound()
					{
						DBGLog.dbg("no mobfox vAd found.");
						
						adClosed(null, false);
					}
					
					@Override
					public void adShown(Ad arg0, boolean arg1)
					{ }
					
					@Override
					public void adLoadSucceeded(Ad arg0)
					{
						if (adManager != null && adManager.isAdLoaded())
						{
							adManager.showAd();
						}
					}
					
					@Override
					public void adClosed(Ad arg0, boolean arg1)
					{
						finish();
						
						overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
					}
					
					@Override
					public void adClicked()
					{ }
				});
				
				adManager.requestAd();
			}
		})
		.setCancelable(false);
		
		// @formatter:on
		
		playSound(soundId);
		
		return builder.create();	
	}
	
	private Dialog makeChangeNameDlg()
	{
		final EditText edt = new EditText(this);
		
		DialogInterface.OnClickListener diocl = new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				switch (which)
				{
				case DialogInterface.BUTTON_POSITIVE:
					newName = edt.getText().toString();
					
					//Filter
					if (!newName.matches(REGEX_NAME_FILTER))
					{
						Toast.makeText(GameSetActivity.this, 
						               getString(R.string.pref_name_aborted_message, NAME_MIN_LENGTH, NAME_MAX_LENGTH), 
						               Toast.LENGTH_SHORT).show();
					}
					else
					{
						PreferenceManager.getDefaultSharedPreferences(GameSetActivity.this)
						.edit()
						.putString(changeNameKey, newName)
						.commit();
						
						txvCpuToChangeName.setText(newName);
					}	
					break;
					
				default:
					break;
				}
			}
		};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle(getString(R.string.dlg_change_name_title, txvCpuToChangeName.getText().toString()))
		.setView(edt)
		.setPositiveButton(android.R.string.ok, diocl)
		.setNegativeButton(android.R.string.cancel, diocl)
		;
		// @formatter:on
		
		final Dialog dialog = builder.create();
		
		edt.setOnFocusChangeListener(new View.OnFocusChangeListener(){
			
			@Override
		    public void onFocusChange(View v, boolean hasFocus) 
			{
				Window window = dialog.getWindow();
				
		        if (hasFocus) 
		        {
		            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		        }
		        else 
				{
					window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				}
		    }
		});

		dialog.setOnShowListener(new DialogInterface.OnShowListener(){
			
			@Override
			public void onShow(DialogInterface dialog)
			{
				edt.setText(txvCpuToChangeName.getText());
			}
		});
		
		return dialog;
	}
	
	private void releasePlayer()
	{
		if (player != null)
		{
			player.stop();
			player.release();
			
			player = null;
		}
	}
	
	private View.OnClickListener newSetClickListener = new View.OnClickListener(){
		
		@Override
		public void onClick(View v)
		{
			RadioButton rbtSelected;
			int type, goal, numPlayers, cCpus;
			int modes[] = new int[3];
			Intent intent = new Intent(GameSetActivity.this, AgoniaGame.class);
			
			type = spnSetType.getSelectedItemPosition();
			numPlayers = spnPlayersNum.getSelectedItemPosition();
			rbtSelected = (RadioButton) argpSetGoal[type].findViewById(argpSetGoal[type].getCheckedRadioButtonId());
			goal = Integer.parseInt(rbtSelected.getText().toString());
			cCpus = numPlayers + 1;
			
			set = new GameSet(type, goal);

			intent.putExtra(ID_PLAYERS_NUM, numPlayers + 2);
			for (int i = 0; i < cCpus; i++)
			{
				modes[i] = aspnAIModes[i].getSelectedItemPosition();
			}
			intent.putExtra(ID_AI_MODES, modes);
			intent.putExtra(ID_NEW_SET, true);
			intent.putExtra(ID_GAMESET, (Parcelable) set);
			
			startActivity(intent);
			
			overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); /* v2.1 */
			
			finish();
		}
	};
	
	private View.OnClickListener continueSetClickListener = new View.OnClickListener(){
		
		@SuppressWarnings("deprecation")
		@Override
		public void onClick(View v)
		{
			if (set.isSetFinished())
			{
				findViewById(R.id.adView).setVisibility(View.GONE); // v2.2 added
				showDialog(DIALOG_SET_FINISH_ID);
			}
			else
			{
				Intent intent = new Intent(GameSetActivity.this, AgoniaGame.class);
				
				intent.putExtra(ID_NEW_SET, false);
				intent.putExtra(ID_GAMESET, (Parcelable) set);
				
				startActivity(intent);
				
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); /* v2.1 */
				
				finish();
			}
		}
	};
	
	/** v2.1 */
	private View.OnClickListener cpuNamesClickListener = new View.OnClickListener(){
		
		@SuppressWarnings("deprecation")
		@Override
		public void onClick(View v)
		{
			String key = "";
			
			switch (v.getId())
			{
			case R.id.txvCpu1:
				key = getString(R.string.key_p2_name);
				break;
				
			case R.id.txvCpu2:
				key = getString(R.string.key_p3_name);
				break;
			
			case R.id.txvFriendOp:
				key = getString(R.string.key_p4_name);
				break;
				
			default:
				return;
			}
			
			changeNameKey = key;
			txvCpuToChangeName = (TextView) v;
			
			showDialog(DIALOG_CHANGE_NAME_ID);
		}
	};
	
	private OnCompletionListener mediaPlayerReleaser = new OnCompletionListener(){
		
		@Override
		public void onCompletion(MediaPlayer mp)
		{
			releasePlayer();
		}
	};
}