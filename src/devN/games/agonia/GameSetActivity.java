package devN.games.agonia;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;
import devN.games.GameSet;

public class GameSetActivity extends Activity
{
	// TODO: Save / Load GameSet
	// TODO: Change Cpu names
	// TODO Statistics for GameSets wins/loses
	
	public static final int DIALOG_SET_FINISH_ID	= 0;

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
	
	// NewSet Views
	private ViewFlipper vfpSetTypeSelection;
	private Spinner spnSetType;
	private Spinner spnPlayersNum;
	private Spinner[] aspnAIModes = new Spinner[3];
	private RadioGroup[] argpSetGoal = new RadioGroup[2];
	
	// ContinueSet Views
	private TextView[] atxvPlayerSetInfos = new TextView[5];
	private TextView[] atxvTeamSetInfos = new TextView[3];
	private TextView txvSetInfo;
	private GameSet set;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
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
	}

	private void continueSet()
	{
		int playerId;
		
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
		btnStart = (Button) findViewById(R.id.set_btn_start);
		
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
		
		for (Spinner spn : aspnAIModes)
		{
			spn.setSelection(AgoniaAI.MODE_MODERATE);
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
			
		default:
			{
			dialog = null;
			}
			break;
		}

		return dialog;
	}
	
	private Dialog makeSetFinishDlg()
	{
		// @formatter:off			
		LinearLayout dlgContainer = (LinearLayout) getLayoutInflater().inflate(R.layout.dlg_set_finish, null);
		ImageView dlgIcon = (ImageView) dlgContainer.findViewById(R.id.dlg_set_finish_ic);
		ArrayList<Integer> winners = set.getWinningTeams();
		
		if (!winners.contains(1))
		{	// looser!
			dlgIcon.setImageResource(R.drawable.ic_thumb_down);
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle(set.winnerString())
		.setView(dlgContainer)
		.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				finish();
			}
		})
		.setCancelable(false);
		
		// @formatter:on
		return builder.create();	
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
			intent.putExtra(ID_GAMESET, set);
			
			startActivity(intent);
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
				showDialog(DIALOG_SET_FINISH_ID);
			}
			else
			{
				Intent intent = new Intent(GameSetActivity.this, AgoniaGame.class);
				
				intent.putExtra(ID_NEW_SET, false);
				intent.putExtra(ID_GAMESET, set);
				
				startActivity(intent);
				finish();
			}
		}
	};
}