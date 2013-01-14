package devN.games.agonia;

import static devN.games.Card.NULL_CARD;
import static devN.games.agonia.GameSetActivity.ID_AI_MODES;
import static devN.games.agonia.GameSetActivity.ID_GAMESET;
import static devN.games.agonia.GameSetActivity.ID_NEW_SET;
import static devN.games.agonia.GameSetActivity.ID_PLAYERS_NUM;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import devN.etc.TextColorAnimation;
import devN.etc.TextColorAnimationGroup;
import devN.etc.dragdrop.DragController;
import devN.etc.dragdrop.DragSource;
import devN.games.Card;
import devN.games.GameSet;
import devN.games.Player;
import devN.games.UICard;
import devN.games.UIDeck;
import devN.games.UIPlayer;
import devN.games.UIStackTop;
import devN.games.agonia.AgoniaAI.AgoniaAIBuilder;

public class AgoniaGame extends Activity implements DragSource, OnTouchListener, AgoniaGameListener
{
	// TODO: GameHistory dialog
	// TODO: Save / Load game
	// TODO: Statistics for game wins/loses/total games
	
	public static final int DIALOG_ACE_ID			= 0;
	public static final int DIALOG_SEVEN_ID			= 1;
	public static final int DIALOG_FINISH_ID		= 2; 	// game finished
	public static final int DIALOG_EXIT_ID			= 3;

	public static final String TAG = "dbg";

	protected Agonia game;
	protected UIPlayer up;
	protected UIPlayer[] aupCpu = new UIPlayer[3];
	protected UICard draggedCard;
	protected UIStackTop stackTop;
	protected UIDeck deck;
	protected int sevenDraw;
	protected long cpuDelay;
	protected boolean dragging = false;
	private DragController dragController;
	private	LayoutInflater inflater;
	private Card selectedSeven = Card.NULL_CARD;
	private List<Player> players = new ArrayList<Player>();
	private GameSet set;
	private int cCpu;
	private LinearLayout infosContainer;
	private TextColorAnimationGroup tcaGroup;
	private boolean isNewSet;

	/**
	 * paiktes pou anamihtikan se Seven-Seven...
	 * wste na kseroume an kapoios petakse to teleutaio tou filo
	 */
	private List<Player> sevenPlayers = new ArrayList<Player>();
	
	/**
	 * Bundle should contains params for GameSet.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
       	setContentView(R.layout.game);

		AgoniaAI cpuAI;
		String p1Name;
		String[] aCpuNames = new String[3];;
		Resources res = getResources();

		Intent intent = getIntent();
		
		int numPlayers;
		int modes[];
		
		isNewSet = intent.getBooleanExtra(ID_NEW_SET, true);
		set = intent.getParcelableExtra(GameSetActivity.ID_GAMESET);
		numPlayers = intent.getIntExtra(ID_PLAYERS_NUM, set.getPlayersCount());
		cCpu = numPlayers - 1;
		
		tcaGroup = new TextColorAnimationGroup();
		inflater = getLayoutInflater();

		if (F.settings == null)
		{
			new F(this);
		}

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		
		cpuDelay = res.getInteger(R.integer.cpu_delay);

		p1Name = F.getString(F.KEY_P1_NAME, getString(R.string.default_p1_name));
		up = (UIPlayer) findViewById(R.id.player);
		up.setInfo();
		up.setName(p1Name);
		up.initDimensions(displayMetrics.widthPixels, displayMetrics.heightPixels / 3,
							displayMetrics.widthPixels, displayMetrics.heightPixels);
		up.setOnTouchListener(this);

		aCpuNames[0] = getString(R.string.cpu1);
		aCpuNames[1] = getString(R.string.cpu2);
		aCpuNames[2] = getString(R.string.friend);
		
		aupCpu[0] = (UIPlayer) findViewById(R.id.cpu1);
		aupCpu[1] = (UIPlayer) findViewById(R.id.cpu2);
		aupCpu[2] = (UIPlayer) findViewById(R.id.cpu3);
		
		if (numPlayers == GameSetActivity.iPLAYERS_NUM_2v2 + 2)
		{
			aupCpu[1].setTeam(2);
		}
		
		for (int i = cCpu; i < aupCpu.length; i++)
		{
			aupCpu[i].setInfo();
			aupCpu[i].setVisibility(View.GONE);
		}
		
		int cpusWidth = displayMetrics.widthPixels / cCpu;
		modes = intent.getIntArrayExtra(ID_AI_MODES);
		
		for (int i = 0; i < cCpu; i++)
		{
			if (isNewSet)
			{
				cpuAI = AgoniaAIBuilder.createById(modes[i], aupCpu[i].getPlayer());
				aupCpu[i].setAI(cpuAI);
			}
			else
			{
				int teamIndex = 0;
				
				if (numPlayers == GameSetActivity.iPLAYERS_NUM_2v2 + 2)
				{
					teamIndex = i > 0 ? 1 : 0;
				}
				Player target = set.getPlayerByTeam(aupCpu[i].getTeam(), teamIndex);
				
				aupCpu[i].setPlayerTo(target);
			}

			aupCpu[i].setInfo();
			aupCpu[i].setName(aCpuNames[i]);
			aupCpu[i].initDimensions(cpusWidth, displayMetrics.heightPixels / 3, 
										displayMetrics.widthPixels, displayMetrics.heightPixels);
			aupCpu[i].setOnTouchListener(this);
			aupCpu[i].setAnimationsDuration(cpuDelay);
		}

		dragController = (DragController) findViewById(R.id.dragLayer);
		
		infosContainer = (LinearLayout) findViewById(R.id.infosContainer);
		
		stackTop = (UIStackTop) findViewById(R.id.stackTop);
		
		deck = (UIDeck) findViewById(R.id.deck);
		deck.setInfo();
		deck.setOnTouchListener(this);
		deck.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				if (!game.turn.equals(up.getPlayer())	// den einai seira tou player
				|| game.canDraw(up.getPlayer())		// prepei na traviksei gia na paei passo
				|| game.isGameFinished())
				{
					return;
				}

				deck.setImage();
				game.switchTurn();
				cpuAutoPlay();  // -> heirizete moni tis ta exceptions tou CPU
			}
		});
		
		if (!isNewSet)
		{
			up.setPlayerTo(set.getPlayerByTeam(1, 0));
			set.clearPlayers();
		}
		
		players.add(up.getPlayer());

		if (numPlayers == GameSetActivity.iPLAYERS_NUM_2v2 + 2)
		{
			players.add(aupCpu[1].getPlayer());
			players.add(aupCpu[2].getPlayer());
			players.add(aupCpu[0].getPlayer());
		}
		else if (numPlayers == GameSetActivity.iPLAYERS_NUM_1v1v1 + 2) 
		{
			players.add(aupCpu[1].getPlayer());
			players.add(aupCpu[0].getPlayer());
		}
		else 
		{
			players.add(aupCpu[0].getPlayer());
		}

		newGame();
	}

	@Override
	protected void onDestroy()
	{
		tcaGroup.stopAll();		
		super.onDestroy();
	}

	private void newGame()
	{
		int[] colors = {Color.YELLOW, Color.RED};

		Agonia.addGameListener(this);

		game = new Agonia(deck.getDeck(), players, isNewSet, set.getFirstPlayerId());
		
		stackTop.setGame(game);
		stackTop.setCardRefTo(game.getTop(0));
		
		infosContainer.removeAllViews();
		
		for (Player p : players)
		{
			UIPlayer uip = playerToUIPlayer(p);
			TextView infos = uip.getInfo();
			
			infosContainer.addView(infos);

			TextColorAnimation tca = new TextColorAnimation(infos, colors);
			tca.setEndColor(Color.WHITE);
			
			tcaGroup.add(p.getId(), tca);
			
			set.addPlayer(p);
		}
		
		set.setFirstPlayerId(game.turn.getId());
		
		onSwitchTurn(game.turn, game.turn);
		
		if (!game.turn.isRealPlayer())
		{
			new Handler().postDelayed(new Runnable(){
				public void run()
				{
					cpuAutoPlay();
				}
			}, (long) (cpuDelay * 1.5));
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void gameFinished() 
	{
//		int p1HandScore = game.scoreOf(up.getHand());
//		int p2HandScore = game.scoreOf(ucp.getHand());
//		
//		up.addScore(p2HandScore);
//		ucp.addScore(p1HandScore);
//		if (p1HandScore > p2HandScore)
//		{
//			ucp.win();
//		}
//		else if (p2HandScore > p1HandScore) 
//		{
//			up.win();
//		}
//		else 
//		{
//			/* No winner! */
//		}
//		saveScores();
//
//		// otan vgainei o player na min uparhei polu anamoni eos otou vgei to finishDialog
//		Long dlgDelay = (long) (game.whoPlayNext().equals(up.getPlayer()) ? (cpuDelay / 2) : (cpuDelay * 1.3));
//		new Handler().postDelayed(new Runnable(){
//			public void run()
//			{
//				try
//				{
//					showDialog(DIALOG_FINISH_ID);
//				}
//				catch (BadTokenException ex)
//				{
//					/*
//					 * ean patisei back button prin vgei o dialogos
//					 * petiete exeption epeidi i parent activity tou dialogou
//					 * den trehei
//					 */
//				}
//			}
//		}, dlgDelay);

//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setMessage(GAME_EVENTS);
//		builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener(){
//
//			@Override
//			public void onClick(DialogInterface dialog, int which)
//			{
//				
//			}
//		}).show();		
		
		set.gameFinished();
		
		long delay = 200;
		
		if (!game.whoIsPrev().isRealPlayer())
		{
			delay += cpuDelay;
		}
		
		new Handler().postDelayed(new Runnable(){
			public void run()
			{
				showDialog(DIALOG_FINISH_ID);
			}
		}, delay);
	}

	@SuppressWarnings("unused")
	private void saveScoresToSet()
	{
		// @formatter:off
	
//		F.edit()
//		.putInt(F.KEY_P1_SCORE, up.getScore())
//		.putInt(F.KEY_P2_SCORE, ucp.getScore())
//		.putInt(F.KEY_P1_WINS, up.getWins())
//		.putInt(F.KEY_P2_WINS, ucp.getWins())
//		.commit();
		
		// @formatter:on
		
//		for (int team = 1; team <= set.getTeamsCount(); team++)
//		{
//			Player setPlayer = set.getPlayerByTeam(team, 0);
//			Player gamePlayer = game.getPlayerById(setPlayer.getId());
//			setPlayer.addScore(gamePlayer.getScore());
//		}
		
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onBackPressed()
	{
		showDialog(DIALOG_EXIT_ID);
	}

	@SuppressWarnings("deprecation")
	public void playerPlay(Player p, Card c)
	{
		deck.setImage();
		
		try
		{
			game.play(p, c);
			stackTop.setImage();
			cpuAutoPlay(); // -> heirizete moni tis ta exceptions tou CPU
		}
		catch (AcePlayed e) // heirizomaste ta exceptions tou User - Player
		{
			stackTop.setImage();
			showDialog(DIALOG_ACE_ID);
		}
		catch (SevenPlayed e)
		{
			stackTop.setImage();
			handleSeven(e.suit);
		}
		catch (GameFinished e)
		{
			stackTop.setImage();
			gameFinished();
		}
	}

	public void cpuAutoPlay()
	{
		if (game.turn.isRealPlayer())
		{
			return;
		}
		
		final UIPlayer ucp = turnToUIPlayer();
		
//		d(TAG, ucp.getPlayer().getName() + " plays now");
		
		new Handler().postDelayed(new Runnable(){
			public void run()
			{
				try
				{
					Card choosed = ucp.willPlay();
	
					if (choosed.equals(NULL_CARD))
					{
						if (game.canDraw(ucp.getPlayer()))
						{
							game.draw(ucp.getPlayer());
						}
						else
						{
							game.play(ucp.getPlayer(), choosed);
							// TODO customize it, personalize it for each cpu
							int gravity;
							int xOffset = 0;
							Toast t = Toast.makeText(getApplicationContext(),
									"Passo",
									Toast.LENGTH_SHORT);
							
							if (cCpu == 1 || ucp.getTeam() == up.getTeam())
							{// 1vs1 or Friend
								gravity = Gravity.CENTER_HORIZONTAL;
							}
							else if (ucp.equals(aupCpu[1]) || ucp.getTeam() == 3) 
							{// Cpu2
								gravity = Gravity.RIGHT;
								xOffset = -20;
							}
							else 
							{// 2vs2 and Cpu1
								gravity = Gravity.LEFT;
								xOffset = 20;
							}
							
							t.setGravity(Gravity.TOP | gravity, xOffset, 30);
							
							t.show();
						}
					}
					else
					{
						game.play(ucp.getPlayer(), choosed);
					}
					if (game.turn.equals(ucp.getPlayer()))
					{
						// gia na emfanizonte ola ta fila otan paizei
						// sunehomenes fores
						// px 8 - 6, tha fenotan mono to 6
						stackTop.postSetImage("", cpuDelay - 200);
						cpuAutoPlay();
					}
					else
					{
						stackTop.postSetImage("", cpuDelay);
						if (!game.turn.isRealPlayer())
						{
							cpuAutoPlay();
						}      
					}
					
				}
				catch (AcePlayed e)
				{
					int aceSuit = ucp.getAceSuit();
	
					game.informAceSuitSelected(aceSuit);
					
					stackTop.setCard(new Card(aceSuit, 1), false);
					stackTop.postSetImage("", cpuDelay);
					game.switchTurn();
					
					if (!game.turn.isRealPlayer())
					{
					    new Handler().postDelayed(new Runnable() {
						    public void run()
						    {
						        cpuAutoPlay();
						    }
					    }, cpuDelay);
                    }     
				}
				catch (final SevenPlayed e)
				{
					stackTop.postSetImage("", cpuDelay);
					
					new Handler().postDelayed(new Runnable() {
						public void run()
						{
							handleSeven(e.suit);
						}
					}, cpuDelay);
				}
				catch (GameFinished e)
				{
					stackTop.postSetImage("", cpuDelay);
					gameFinished();
				}
			}
	
		}, cpuDelay + 200);
	}

	/**
	 * game.turn == playerOfSeven 
	 */
	public void handleSeven(int suit)
	{
		sevenDraw += Agonia.DRAW_WITH_SEVEN;
		sevenPlayers.add(game.turn);
		
		game.switchTurn();
		
		try
		{
			if (game.turn.isRealPlayer())
			{
				new Handler().postDelayed(new Runnable(){
					@SuppressWarnings("deprecation")
					public void run()
					{
						showDialog(DIALOG_SEVEN_ID);
					}
				}, 200);
			}
			else
			{	
				UIPlayer ucp = turnToUIPlayer();
				
				try
				{
					game.play(ucp.getPlayer(), ucp.playSeven());
				}
				catch (GameFinished e)
				{  /*
					* will handled on sevenDraw()
					*/
				}
				sevenDraw();
			}
		}
		catch (final SevenPlayed e)
		{
			stackTop.postSetImage("", cpuDelay - 200);
			new Handler().postDelayed(new Runnable() {
				public void run()
				{
					handleSeven(e.suit);
				}
			}, cpuDelay);
		}
	}

	private void sevenDraw()
	{
		game.turn = game.whoIsPrev();
		
		tcaGroup.pauseAll();	// just to be safe...

		onSwitchTurn(game.turn, game.turn);
			
		final View table = findViewById(R.id.table);
		final View cpusContainer = findViewById(R.id.cpusContainer);
		Runnable r = new Runnable(){
			public void run()
			{
				cpusContainer.postInvalidate();
				cpusContainer.requestLayout();
				table.postInvalidate();
				table.requestLayout();
			}
		};
		
		try
		{
			game.draw(game.turn, sevenDraw, true);
			
			for (int i = 15; i > 0; i--)
			{
				cpusContainer.postDelayed(r, cpuDelay / i);
				table.postDelayed(r, cpuDelay / i);
			}

			cpusContainer.postDelayed(r, cpuDelay + 50);
			table.postDelayed(r, cpuDelay + 50);
		}
		catch (GameFinished ex) // maybe deck finished
		{
			gameFinished();
			return;
		}
		
		deck.setImage();
		
		for (Player p : sevenPlayers)
		{
			if (p.getHand().isEmpty())
			{
				try
				{
					game.finishGame();
				}
				catch (GameFinished ex)
				{
					gameFinished();
					return;
				}
			}
		}
		
		sevenDraw = 0;
		sevenPlayers.clear();
		
		if (!game.turn.isRealPlayer()) 
		{
			cpuAutoPlay();
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id)
	{
		Dialog dialog = null;

		switch (id)
		{
		case DIALOG_ACE_ID:
			{
			dialog = makeAceDlg();
			}
			break;
												
		case DIALOG_SEVEN_ID:
			{
			dialog = makeSevenDlg();
			}
			break;
			
		case DIALOG_FINISH_ID:
			{					
			dialog = makeFinishDlg();
			}
			break;
			
		case DIALOG_EXIT_ID:
			{
			dialog = makeExitDlg();
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

	@SuppressWarnings("deprecation")
	@Override
	protected void onPrepareDialog(int id, Dialog dialog)
	{
		switch (id)
		{
		case DIALOG_SEVEN_ID:
			LinearLayout sevenContainer = (LinearLayout) dialog.findViewById(R.id.dlg_seven_container);
			Button btnDraw = (Button) dialog.findViewById(R.id.dlg_seven_btn_draw);
			List<Card> sevens = getSevens();
			
			btnDraw.setText(getString(R.string.dlg_seven_btn_draw, sevenDraw));
			
			sevenContainer.removeAllViews();
			for (Card c : sevens)
			{
				UICard seven = new UICard(this, c, true);
				sevenContainer.addView(seven);
			}
			break;

		default:
			break;
		}
		
		super.onPrepareDialog(id, dialog);
	}

	private Dialog makeAceDlg()
	{
		final Dialog retVal;
		
		LinearLayout dlgContent = (LinearLayout) inflater.inflate(R.layout.dlg_ace, null);
		ImageButton suitButtons[] = new ImageButton[4];
		
		suitButtons[Card.iSPADES] = (ImageButton) dlgContent.findViewById(R.id.spades);
		suitButtons[Card.iDIAMONDS] = (ImageButton) dlgContent.findViewById(R.id.diamonds);
		suitButtons[Card.iCLUBS] = (ImageButton) dlgContent.findViewById(R.id.clubs);
		suitButtons[Card.iHEARTS] = (ImageButton) dlgContent.findViewById(R.id.hearts);
		
		// @formatter:off

		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle(getString(R.string.dlg_ace_title))
		.setCancelable(false)
		.setView(dlgContent)
		;
		
		// @formatter:on
		
		retVal = builder.create();
		
		for (int i = 0; i < suitButtons.length; i++)
		{
			final int suit = i;
			
			suitButtons[i].setOnClickListener(new OnClickListener(){
				
				@Override
				public void onClick(View v)
				{
					playerAceSuitSelected(suit);
					retVal.dismiss();
				}
			});
		}
		
		return retVal;
	}
	
	private void playerAceSuitSelected(int suit)
	{
		stackTop.setCard(new Card(suit, 1));
		stackTop.postSetImage("");
		
		game.informAceSuitSelected(suit);
		
		game.switchTurn();
		cpuAutoPlay();
	}
	
	private Dialog makeSevenDlg()
	{
		final Dialog retVal;
		
		List<Card> sevens = getSevens();
		
		LinearLayout dlgContent = (LinearLayout) inflater.inflate(R.layout.dlg_seven, null);
		LinearLayout sevenContainer = (LinearLayout) dlgContent.findViewById(R.id.dlg_seven_container);
		Button btnDraw = (Button) dlgContent.findViewById(R.id.dlg_seven_btn_draw);
		final Button btnPlay = (Button) dlgContent.findViewById(R.id.dlg_seven_btn_play);
		
		btnDraw.setText(getString(R.string.dlg_seven_btn_draw, sevenDraw));
		btnPlay.setText(getString(R.string.dlg_seven_btn_play));

		sevenContainer.setOnHierarchyChangeListener(new OnHierarchyChangeListener(){
				private OnClickListener sevenClickListener = new OnClickListener(){
				int cchilds;
				
				private void select(LinearLayout container, View selected)
				{
					cchilds = container.getChildCount();
					clean(container);
					
					selected.setBackgroundResource(R.drawable.accept);
				}
				
				private void clean(LinearLayout container)
				{
					for (int i = 0; i < cchilds; i++)
					{
						View v = container.getChildAt(i);
						
						v.setBackgroundResource(0);
					}
				}
				
				@Override
				public void onClick(View v)
				{
					select((LinearLayout) v.getParent(), v);
					selectedSeven = ((UICard) v).getCard();
					btnPlay.setEnabled(true);
				}
			};
			
			@Override
			public void onChildViewRemoved(View parent, View child)
			{
								
			}
			
			@Override
			public void onChildViewAdded(View parent, View child)
			{
				child.setOnClickListener(sevenClickListener);
			}
		});
		
		for (Card c : sevens)
		{
			UICard seven = new UICard(this, c, true);
			sevenContainer.addView(seven);
		}
		
		// @formatter:off
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle(getString(R.string.dlg_seven_title))
		.setView(dlgContent)
		.setCancelable(true)
		.setOnCancelListener(new OnCancelListener(){
			
			@Override
			public void onCancel(DialogInterface dialog)
			{
				game.switchTurn();
				sevenDraw();				
			}
		})
		;
		// @formatter:on
		
		retVal = builder.create();
		retVal.setOnDismissListener(new OnDismissListener(){
			
			@Override
			public void onDismiss(DialogInterface dialog)
			{
				btnPlay.setEnabled(false);
				
			}
		});
		
		btnDraw.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v)
			{
				game.switchTurn();
				sevenDraw();
				retVal.dismiss();
			}
		});
		
		btnPlay.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v)
			{
				try 
				{
					game.play(up.getPlayer(), selectedSeven);
				}
				catch (SevenPlayed e) 
				{
//					game.switchTurn();
					stackTop.postSetImage("", 100);
					handleSeven(e.suit);
				}
				retVal.dismiss();
			}
		});
		
		return retVal;
	}
	
	private List<Card> getSevens()
	{
		List<Card> sevens = new ArrayList<Card>();
		
		for (Card c : up.getHand())
		{
			if (c.getRank() == 7)
			{
				sevens.add(new Card(c));
			}
		}
		
		return sevens;
	}
	
	private Dialog makeExitDlg()
	{
		// @formatter:off 
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle(getString(R.string.dlg_exit_title))
		.setMessage(getString(R.string.dlg_exit_message))
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				for (Player p : game.getPlayers())
				{
					if (p.getTeam() != up.getTeam())
					{
						p.getHand().clear();
					}
				}
				
				try
				{
					game.finishGame();
				}
				catch (GameFinished ex)
				{ 
					gameFinished();
				}
			}
		})
		.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				/*
				 * nothing to do
				 */
			}
		})
		;
		
		// @formatter:on
		
		return builder.create();
	}

	private Dialog makeFinishDlg()
	{
		// @formatter:off
		TextView[] atxvName = new TextView[4];
		TextView[] atxvHand = new TextView[4];
		
		LinearLayout dlgContent = (LinearLayout) inflater.inflate(R.layout.dlg_finish, null);

		int i = 0;
	
		for (i = 0; i < 4; i++)
		{
			atxvName[i] = (TextView) dlgContent.getChildAt(i * 2);
			atxvHand[i] = (TextView) dlgContent.getChildAt(i * 2 + 1);

			if (i > cCpu)
			{
				atxvName[i].setVisibility(View.GONE);
				atxvHand[i].setVisibility(View.GONE);
			}
			else 
			{
				Player p = players.get(i);
				if (p.hasCards())
				{
					atxvName[i].setText(p.getName() + "  { " + game.scoreOf(p.getHand()) + " }");
					atxvHand[i].setText(p.handString());
				}
				else 
				{
					atxvName[i].setVisibility(View.GONE);
					atxvHand[i].setVisibility(View.GONE);
				}
			}
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle(R.string.dlg_finish_title)
		.setView(dlgContent)
		.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				Intent intent = new Intent(AgoniaGame.this, GameSetActivity.class);
				
				intent.putExtra(ID_NEW_SET, false);
				intent.putExtra(ID_GAMESET, set);
				
				startActivity(intent);
				finish();
			}
		})
		.setCancelable(false);
		
		// @formatter:on
		return builder.create();
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if (!game.turn.isRealPlayer() || game.isGameFinished())
		{
			return false;
		}		

		switch (event.getAction())
		{
		case MotionEvent.ACTION_MOVE:
			break;

		case MotionEvent.ACTION_DOWN:
			if (dragging)
			{// Den epitrepete multidragging
				return false;
			}

			if (v.equals(deck) && (!game.canDraw(up.getPlayer()) || !deck.hasCards()))
			{// travaei mono an mas epitrepei to game kai ean to deck ehei fila
				return false;
			}
			
			if (!(v instanceof UICard) || v instanceof UIStackTop)
			{// kanoume drag mono ta UICards ki ohi to StackTop
				return false;
			}
			draggedCard = (UICard) v;
			
			if (!draggedCard.isVisible() && !draggedCard.getCard().equals(NULL_CARD))
			{// den kanomue drag ta fila tou cpu 
				return false;
			}

			dragging = true;
			
			int dragAction = v instanceof UIDeck ? DragController.DRAG_ACTION_COPY : DragController.DRAG_ACTION_MOVE;
			
			dragController.startDrag(v, this, v, dragAction);
			break;

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			dragging = false;
			break;

		default:
			break;
		}

		return dragging;
	}

	@Override
	public void onDropCompleted(View target, boolean success)
	{
		if (success)
		{
			if (target.equals(up))
			{
				try
				{
					game.draw(up.getPlayer());
					deck.setImage("btn_paso");
				}
				catch (GameFinished ex)
				{
					gameFinished();
				}
			}
			else 
			{
				playerPlay(up.getPlayer(), draggedCard.getCard());
			}			
		}
		else 
		{
			// draggedCard is placed back to its owner's hand
		}
		dragging = false;
	}

	private UIPlayer playerToUIPlayer(Player p)
	{
		UIPlayer uip = null;

		if (p.isRealPlayer())
		{
			uip = up;
		}
		else 
		{
			for (UIPlayer uiPlayer : aupCpu)
			{
				if (p.equals(uiPlayer.getPlayer()))
				{
					uip = uiPlayer;
					break;
				}
			}
		}
		
		return uip;
	}
	
	private UIPlayer turnToUIPlayer()
	{
		return playerToUIPlayer(game.turn);
	}
	
	private String GAME_EVENTS = "";
	
	@Override
	public void initGameParams(int cPlayers, List<Card> stackTopCards)
	{
		GAME_EVENTS = "Players " + cPlayers + " | Top " + stackTopCards.get(0) + "\n";
	}

	@Override
	public void onDrawFromDeck(Player who, int n)
	{
		GAME_EVENTS = GAME_EVENTS + who.getName() + " draw " + n + "\n";
	}

	@Override
	public void onPlay(Player who, Card c)
	{
		GAME_EVENTS = GAME_EVENTS + who.getName() + " -> " + c + "\n" ;
	}
	
	@Override
	public void onAceSuitSelected(int selectedSuit)
	{
		GAME_EVENTS = GAME_EVENTS + "\t Ace -> " + Card.pszSUITS[selectedSuit] + "\n" ;
	}

	@Override
	public void onPlay(Player who, List<Card> cards)
	{ }

	@Override
	public void onPlay(Player who, int cCards)
	{ }

	@Override
	public void onSwitchTurn(Player prev, Player cur)
	{	
		tcaGroup.pause(prev.getId());
		
		tcaGroup.start(cur.getId());
	}
}