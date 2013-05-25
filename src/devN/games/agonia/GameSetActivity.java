package devN.games.agonia;

import static devN.games.agonia.AgoniaPref.REGEX_NAME_FILTER;
import static devN.games.agonia.AgoniaPref.NAME_MIN_LENGTH;
import static devN.games.agonia.AgoniaPref.NAME_MAX_LENGTH;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import com.appflood.AppFlood;
import com.google.gson.Gson;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import devN.etc.AdManagerActivity;
import devN.etc.DBGLog;
import devN.etc.DevnDialogUtils;
import devN.etc.TypefaceUtils;
import devN.games.GameSet;

public class GameSetActivity extends Activity
{	
	public static final int DIALOG_SET_FINISH_ID	= 0;
	public static final int DIALOG_CHANGE_NAME_ID	= 1;
	public static final int DIALOG_LOAD_GAME_ID		= 2;
	public static final int DIALOG_DELETE_GAMES_ID	= 3;
	
	private final static int iSET_TYPE_POINTS 	= 0;
	private final static int iSET_TYPE_WINS 	= 1;
	
	public final static int iPLAYERS_NUM_1v1 	= 0;
	public final static int iPLAYERS_NUM_1v1v1 	= 1;
	public final static int iPLAYERS_NUM_2v2 	= 2;
	
	public final static String ID_GAMESET 		= "devN.games.agonia.GAMESET";
	public final static String ID_NEW_SET 		= "devN.games.agonia.NEW_SET";
	public final static String ID_PLAYERS_NUM 	= "devN.games.agonia.PLAYERS_NUM";
	public final static String ID_AI_MODES		= "devN.games.agonia.PLAYERS_AI_MODES";

	public final static String SAVE_FILE_EXT	= ".gsas";
	public final static String STATE_FILE		= "gsstate";
	
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
	
	private String appid = "2vIcTLl5OAAhgvFv";
	private String secretkey = "y854qZjI9c0L517f9a59";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		AppFlood.initialize(this, appid, 		// v2.3
				secretkey, 
				AppFlood.AD_ALL);
		
		if (F.settings == null)
		{
			new F(this);
		}
		
		useSFX = PreferenceManager.getDefaultSharedPreferences(this)
					.getBoolean(getString(R.string.key_game_sfx), true);
		
		Intent intent = getIntent();
		
		isNewSet = intent.getBooleanExtra(ID_NEW_SET, true);
		
		if (isNewSet)
		{
			setContentView(R.layout.new_set);
			newSet();
			loadStateFromFile();		// v2.3
		}
		else 
		{
			setContentView(R.layout.continue_set);
			set = intent.getParcelableExtra(ID_GAMESET);
			continueSet();
		}
		
		ViewGroup root = (ViewGroup) findViewById(R.id.gameset_root);
		TypefaceUtils.setAllTypefaces(root, this, getString(R.string.custom_font));
		
		AgoniaGamesStatsManager.getManager().saveStats(GameSetActivity.this, false);	// v2.3
	}
	
	@Override
	protected void onDestroy()
	{
		AppFlood.destroy();
		super.onDestroy();
	}

	@Override
	public void onBackPressed()
	{// v2.2
		if (isNewSet)
		{			
			finish();
			overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		}
		else if (set.isSetFinished()) 
		{
			Animation scale = new ScaleAnimation(0.85f, 1.3f, 0.9f, 1.2f, 
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);
			scale.setDuration(500);
			scale.setFillEnabled(true);
			scale.setFillBefore(true);
			scale.setFillAfter(false);
			scale.setInterpolator(new BounceInterpolator());
			
			btnStart.startAnimation(scale);
		}
		else 
		{
			ibtLoad.performClick();
		}
	}

	private boolean adOnce = false;
	private final static double INTERSIAL_AD_CHANCE = 1D / 4D; // v2.3.1b
	/* v2.0b added. */
	/** Hopefully it will resolve any NullPointerException on F.get... */
	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		if (hasFocus && F.settings == null)
		{
			new F(this);
		}
		
		if (hasFocus 
		&& !isNewSet 
		&& !set.isSetFinished() 
		&& !adOnce
		&& INTERSIAL_AD_CHANCE > Math.random()	// v2.3.1b
		&& AppFlood.isConnected())	// v2.3.1b
		{
			AppFlood.showPanel(this, AppFlood.PANEL_BOTTOM);
			adOnce = true;
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
			ibtLoad.setVisibility(View.INVISIBLE); // v2.2
		}
		
		btnStart.setOnClickListener(continueSetClickListener);
		ibtLoad.setOnClickListener(saveGameSetClickListener);
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
		ibtLoad.setOnClickListener(loadGameSetClickListener);
		ibtLoad.setOnLongClickListener(loadGameSetLongClickListener);
	}
	
	/**
	 * v2.2
	 * Its also finishes the Activity <br />
	 * <br />
	 * saved in form: <br />
	 * [set] [pref.deckFinishOption] [pref.aceOnAce] [pref.aceFinish]
	 */
	private void saveGameSet(FileOutputStream out, String fileName) throws IOException
	{
		final int extLen = SAVE_FILE_EXT.length();
		final String savedName = fileName.substring(0, fileName.length() - extLen);

		ObjectOutputStream s = new ObjectOutputStream(out);
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		int deckFinishOption = Integer.parseInt(pref.getString(getString(R.string.key_deck_finish), "0"));
		boolean aceOnAce = pref.getBoolean(getString(R.string.key_ace_on_ace), false);
		boolean aceFinish = pref.getBoolean(getString(R.string.key_ace_finish), false);
		
		s.writeObject(set);
		s.writeInt(deckFinishOption);
		s.writeBoolean(aceOnAce);
		s.writeBoolean(aceFinish);

		s.flush();
		s.close();
		
		Toast.makeText(this, 
				getString(R.string.set_toast_on_save_game, savedName), 
				Toast.LENGTH_SHORT)
		.show();

		finish();
		
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
	
	/**
	 * v2.2
	 * loads in form: <br />
	 * [set] [pref.deckFinishOption] [pref.aceOnAce] [pref.aceFinish]
	 * @throws ClassNotFoundException 
	 */
	private void loadGameSet(FileInputStream in, String fileName) throws IOException, ClassNotFoundException
	{
		ObjectInputStream s = new ObjectInputStream(in);
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		set = (GameSet) s.readObject();
		int deckFinishOption = s.readInt();
		boolean aceOnAce = s.readBoolean();
		boolean aceFinish = s.readBoolean();
		
		pref.edit()
		.putString(getString(R.string.key_deck_finish), deckFinishOption + "")
		.putBoolean(getString(R.string.key_ace_on_ace), aceOnAce)
		.putBoolean(getString(R.string.key_ace_finish), aceFinish)
		.commit();
		
		s.close();
		
		deleteFile(fileName);
		
		Intent intent = new Intent(GameSetActivity.this, GameSetActivity.class);
		
		intent.putExtra(ID_NEW_SET, false);
		intent.putExtra(ID_GAMESET, (Parcelable) set);
		
		startActivity(intent);
		
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		
		finish();
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
			
		case DIALOG_LOAD_GAME_ID:
			{
			dialog = makeLoadGameDlg();	
			}
			break;
			
		case DIALOG_DELETE_GAMES_ID:
			{
			dialog = makeDeleteGamesDlg();	
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
			String title = getString(R.string.dlg_change_name_title, txvCpuToChangeName.getText().toString());
 			dialog.setTitle(title);
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
		
		AgoniaGamesStatsManager			// v2.3
		.getManager().onSetFinished(set.getPlayers(), winners);
		
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
				finish();
				overridePendingTransition(android.R.anim.fade_in,
												android.R.anim.fade_out);
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
		
		// @formatter:off
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
	
	private Dialog makeLoadGameDlg()
	{
		final List<String> saveFiles = getSaveFileNames();
		
		if (saveFiles.isEmpty())
		{
			Toast.makeText(GameSetActivity.this, 
					getString(R.string.set_toast_no_saved_games), 
					Toast.LENGTH_SHORT)
			.show();
			
			return null;
		}
		
		// @formatter:off
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle(R.string.dlg_set_load_title)
		.setAdapter(new ArrayAdapter<String>(GameSetActivity.this, R.layout.txv_set_load_game, saveFiles), 
			new DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				FileInputStream f = null;
				String name = saveFiles.get(which) + SAVE_FILE_EXT;

				try
				{
					f = openFileInput(name);
					loadGameSet(f, name);
				}
				catch (FileNotFoundException ex)
				{
					Toast.makeText(GameSetActivity.this, 
							"Can't open " + name + "\n" + ex, 
							Toast.LENGTH_SHORT)
					.show();
				}
				catch (Exception ex)
				{
					Toast.makeText(GameSetActivity.this, 
							"Can't load from " + name + "\n" + ex, 
							Toast.LENGTH_SHORT)
					.show();
					
					if (f != null)
					{
						try
						{
							f.close();
						}
						catch (IOException ex1)
						{ }
					}
				}
			}
		})
		;
		// @formatter:on
		return builder.create();
	}
	
	private Dialog makeDeleteGamesDlg()
	{
		final List<String> saveFiles = getSaveFileNames();
		
		if (saveFiles.isEmpty())
		{
			Toast.makeText(GameSetActivity.this, 
					getString(R.string.set_toast_no_saved_games), 
					Toast.LENGTH_SHORT)
			.show();
			
			return null;
		}

		DeleteSaveFilesDlgListener dsfdl = new DeleteSaveFilesDlgListener(saveFiles);
		
		// @formatter:off
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setMultiChoiceItems(saveFiles.toArray(new String[saveFiles.size()]), 
				null, dsfdl)
		.setTitle(R.string.dlg_set_delete_games_title)
		.setPositiveButton(R.string.dlg_btn_set_delete_games_delete, dsfdl)
		.setNegativeButton(android.R.string.cancel, dsfdl)
		;
		// @formatter:on
		
		return builder.create();
	}
	
	private List<String> getSaveFileNames()
	{
		final int extLen = SAVE_FILE_EXT.length();
		
		List<String> saveFiles = new ArrayList<String>();
		
		for (String fileName : fileList())
		{
			if (fileName.endsWith(SAVE_FILE_EXT))
			{
				saveFiles.add(fileName.substring(0, fileName.length() - extLen));
			}
		}
		
		return saveFiles;
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
			
			// v2.3
			State state = new State();
			state.saveState();
			
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
				Intent adIntent = new Intent(GameSetActivity.this, AdManagerActivity.class);
				startActivity(adIntent); // v2.4 added
				
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
	
	//
	// v2.2 added
	//
	
	private View.OnClickListener saveGameSetClickListener = new View.OnClickListener(){
		
		@Override
		public void onClick(View v)
		{
			FileOutputStream f = null;
			
			Calendar cal = Calendar.getInstance();
			String name = String.format("%s_%d.%d.%d_%d.%d.%d%s", 
					set.getPlayerByTeam(1, 0).getName(),
					cal.get(Calendar.DAY_OF_MONTH),
					cal.get(Calendar.MONTH), cal.get(Calendar.YEAR),
					cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
					cal.get(Calendar.SECOND),
					SAVE_FILE_EXT);
			try
			{
				f = openFileOutput(name, MODE_PRIVATE);
				
				saveGameSet(f, name);
			}
			catch (FileNotFoundException ex)
			{
				Toast.makeText(GameSetActivity.this, 
						"Can't save game!\n" + ex, Toast.LENGTH_SHORT)
				.show();
			}
			catch (IOException ex)
			{
				Toast.makeText(GameSetActivity.this, 
						"Error occured on save game!\n" + ex, Toast.LENGTH_SHORT)
				.show();
				
				if (f != null)
				{
					try
					{
						f.close();
						deleteFile(name);
					}
					catch (IOException ex1)
					{ }
				}
			}
		}
	};
	
	private View.OnClickListener loadGameSetClickListener = new View.OnClickListener(){
		
		@SuppressWarnings("deprecation")
		@Override
		public void onClick(View v)
		{
			try
			{
				removeDialog(DIALOG_LOAD_GAME_ID);
			}
			catch (Exception ex)
			{ }
			
			showDialog(DIALOG_LOAD_GAME_ID);
		}
	};
	
	private View.OnLongClickListener loadGameSetLongClickListener = new View.OnLongClickListener() {

		@SuppressWarnings("deprecation")
		@Override
		public boolean onLongClick(View v)
		{
			try
			{
				removeDialog(DIALOG_DELETE_GAMES_ID);
			}
			catch (Exception ex)
			{ }
			
			showDialog(DIALOG_DELETE_GAMES_ID);
			return true;
		}
		
	};
	
	private class DeleteSaveFilesDlgListener 
			implements DialogInterface.OnMultiChoiceClickListener, DialogInterface.OnClickListener
	{
		private LinkedHashMap<String, Boolean> items;
		
		public DeleteSaveFilesDlgListener(List<String> saveFiles)
		{
			items = new LinkedHashMap<String, Boolean>();
			
			for (String string : saveFiles)
			{
				items.put(string, false);
			}
			
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			switch (which)
			{
			case DialogInterface.BUTTON_NEGATIVE:
				{ /* Cancel. Do noting */ }
				break;
			
			case DialogInterface.BUTTON_POSITIVE:
				int i = 0;
				for (String name : items.keySet())
				{
					if (items.get(name))
					{
						i++;
						deleteFile(name + SAVE_FILE_EXT);
					}
				}
				
				Toast.makeText(GameSetActivity.this, 
						getString(R.string.set_toast_on_delete_games, i), 
						Toast.LENGTH_SHORT)
				.show();
				break;

			default:
				break;
			}	
		}
	
		@Override
		public void onClick(DialogInterface dialog, int which, boolean isChecked)
		{
			items.put((String) items.keySet().toArray()[which], isChecked);			
		}
	}
	
	//
	//	v2.3
	//
	
	public void loadStateFromFile()
	{
		Gson gson = new Gson();
		FileInputStream f = null;
		
		try
		{
			f = openFileInput(STATE_FILE);
			Scanner sc = new Scanner(f);
			
			String json = sc.nextLine();
			State state = gson.fromJson(json, State.class);
			
			spnSetType.setSelection(state.getType());
			
			if (state.getType() > 0)
			{
				vfpSetTypeSelection.showNext();
			}
			
			RadioButton rbt = (RadioButton) argpSetGoal[state.getType()].findViewById(state.getGoal());
			rbt.performClick();
			
			int[] modes = state.getModes();
			spnPlayersNum.setSelection(modes.length - 1);
			for (int i = 0; i < modes.length; i++)
			{
				aspnAIModes[i].setSelection(modes[i]);
			}
			
			sc.close();
		}
		catch (FileNotFoundException ex)
		{
			DBGLog.dbg("Can't load gameSet state. " + ex.getMessage());
		}
		
	}
	
	public class State
	{
		private int type;
		private int goal;
		private int[] modes;
		
		public State()
		{
			int cModes = spnPlayersNum.getSelectedItemPosition() + 1;
			
			type = spnSetType.getSelectedItemPosition();
			
			goal = argpSetGoal[type].getCheckedRadioButtonId();

			modes = new int[cModes];
			for (int i = 0; i < cModes; i++)
			{
				modes[i] = aspnAIModes[i].getSelectedItemPosition();
			}
		}
			
		public void saveState()
		{
			FileOutputStream f = null;
			PrintWriter pw;
			try
			{
				f = openFileOutput(STATE_FILE, MODE_PRIVATE);
				pw = new PrintWriter(f);
				
				Gson gson = new Gson();
				
				pw.write(gson.toJson(this));
				
				pw.flush();
				pw.close();
			}
			catch (Exception ex)
			{
				DBGLog.dbg("Can't save gameSet state. " + ex.getMessage());
			}
		}

		public int getType()
		{
			return type;
		}

		public int getGoal()
		{
			return goal;
		}

		public int[] getModes()
		{
			return modes;
		}

		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("State [type=").append(type).append(", goal=").append(goal)
					.append(", modes=").append(Arrays.toString(modes)).append("]");
			return builder.toString();
		}
	}
}