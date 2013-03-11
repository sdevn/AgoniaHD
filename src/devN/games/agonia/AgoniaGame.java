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
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import devN.etc.DBGLog;
import devN.etc.DevnDialogUtils;
import devN.etc.TextColorAnimation;
import devN.etc.TextColorAnimationGroup;
import devN.etc.TypefaceUtils;
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
	private final static boolean DEBUG = false;
	
	// TODO: GameHistory dialog
	// TODO: Save / Load game
	// TODO: Statistics for game wins/loses/total games
	// TODO: Menu button function. {Enable/Disable music/sfx, how to play, see preferences}
	
	public static final int DIALOG_ACE_ID			= 0;
	public static final int DIALOG_SEVEN_ID			= 1;
	public static final int DIALOG_FINISH_ID		= 2; 	// game finished
	public static final int DIALOG_EXIT_ID			= 3;

	/** v2.1 added
	 * epeidi ta deltaX, deltaY tou playAnimation den einai akrives
	 * prepei na kanoume invalidate wste na min fenetai oti i karta
	 * den paei akrivws panw ap to UIStackTop (hanei merika pixels)
	 */
	private final AnimationListener invalidateListener = new AnimationListener(){
		
		@Override
		public void onAnimationStart(Animation animation)
		{ }
		
		@Override
		public void onAnimationRepeat(Animation animation)
		{ }
		
		@Override
		public void onAnimationEnd(Animation animation)
		{
			((ViewGroup) dragController).requestLayout();
			((ViewGroup) dragController).postInvalidate();
			stackTop.requestLayout();
			stackTop.postInvalidate();
		}
	};

	public static final float MUSIC_GAME_VOLUME		= .2f;
	
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

	private LinearLayout cpusContainer; /* v2.0a */
	private RelativeLayout.LayoutParams rlpCpuContainer; /* v2.0a */
	
	/* v2.1 */
	private boolean useSFX;
	private boolean useMusic;
	private MediaPlayer mediaPlayer;
	private static SoundPool soundPool;
	private static int sound_deal_cards;
	private static int sound_draw_card;
	private static int sound_play_card;
	private static int sound_fail_game;
	private static int sound_win_game;

	/**
	 * Bundle should contains params for GameSet.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
       	       	
       	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
       	
       	useSFX = prefs.getBoolean(getString(R.string.key_game_sfx), true);
       	initSoundPool(this, useSFX);
       	useMusic = prefs.getBoolean(getString(R.string.key_game_music), false);
       	
       	boolean oldDeck = prefs.getBoolean(getString(R.string.key_old_deck), false);
       	boolean oldAnim = prefs.getBoolean(getString(R.string.key_old_anim), false);
       	       	
       	UICard.setUseOldDeck(oldDeck);
       	UIPlayer.setUseOldAnim(oldAnim);
       	
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

		setContentView(R.layout.game);
		
		p1Name = F.getString(F.KEY_P1_NAME, getString(R.string.default_p1_name));
		up = (UIPlayer) findViewById(R.id.player);
		up.setInfo();
		up.setName(p1Name);
		up.initDimensions(displayMetrics.widthPixels, displayMetrics.heightPixels / 3,
							displayMetrics.widthPixels, displayMetrics.heightPixels);
		up.setOnTouchListener(this);
		up.setOnPlayAnimListener(invalidateListener);

		aCpuNames[0] = F.getString(F.KEY_P2_NAME, getString(R.string.cpu1));
		aCpuNames[1] = F.getString(F.KEY_P3_NAME, getString(R.string.cpu2));
		aCpuNames[2] = F.getString(F.KEY_P4_NAME, getString(R.string.friend));
		
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
			aupCpu[i].setOnPlayAnimListener(invalidateListener);
		}

		dragController = (DragController) findViewById(R.id.dragLayer);
		
		infosContainer = (LinearLayout) findViewById(R.id.infosContainer);
		
		cpusContainer = (LinearLayout) findViewById(R.id.cpusContainer);
		rlpCpuContainer = new RelativeLayout.LayoutParams(cpusContainer.getLayoutParams());
		cpusContainer.setLayoutParams(rlpCpuContainer);
		
		if (DEBUG)
		{// v2.1
			cpusContainer.setBackgroundColor(Color.WHITE);
		}
		
		stackTop = (UIStackTop) findViewById(R.id.stackTop);
		
		deck = (UIDeck) findViewById(R.id.deck);
		deck.setInfo();
		deck.setImage();
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
	protected void onStart()
	{
		if (useMusic && mediaPlayer == null)
		{
			mediaPlayer = MediaPlayer.create(this, R.raw.game_back_sound);
			mediaPlayer.setLooping(true);
		}
		
		initSoundPool(this, useSFX);
		
		super.onStart();
	}

	@Override
	protected void onResume()
	{
		if (useMusic && mediaPlayer != null)
		{
			mediaPlayer.setVolume(MUSIC_GAME_VOLUME, MUSIC_GAME_VOLUME);
			mediaPlayer.start();
		}
		
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		if (useMusic && mediaPlayer != null)
		{
			mediaPlayer.reset();
		}
		
		super.onPause();
	}
	
	@Override
	protected void onDestroy()
	{
		if (mediaPlayer != null)
		{
			mediaPlayer.release();
			mediaPlayer = null;
		}
		
		if (soundPool != null)
		{
			soundPool.release();
			soundPool = null;
		}
		
		tcaGroup.stopAll();		
		super.onDestroy();
	}
	
	public static void initSoundPool(Context context, boolean prefUseSFX)
	{
		if (prefUseSFX && soundPool == null)
		{
			soundPool = new SoundPool(9, AudioManager.STREAM_MUSIC, 0);
			
			sound_deal_cards = soundPool.load(context, R.raw.deal_cards, 1);
			sound_draw_card = soundPool.load(context, R.raw.draw_card, 1);
			sound_play_card = soundPool.load(context, R.raw.play_card, 1);
			sound_fail_game = soundPool.load(context, R.raw.fail_game, 1);
			sound_win_game = soundPool.load(context, R.raw.win_game, 1);
		}
	}

	private void newGame()
	{
		String prefColor, defColor;
		int[] colors = new int[2];
		Typeface typeface = TypefaceUtils.get(this, getString(R.string.custom_font));
		
		Agonia.addGameListener(this);

		defColor = getString(R.string.pref_player_turn_default_color);
		
		prefColor = PreferenceManager.getDefaultSharedPreferences(this)
						.getString(getString(R.string.key_player_turn_color1), defColor);
		colors[0] = Color.parseColor(prefColor);
		
		prefColor = PreferenceManager.getDefaultSharedPreferences(this)
						.getString(getString(R.string.key_player_turn_color2), defColor);
		colors[1] = Color.parseColor(prefColor);

		game = new Agonia(deck.getDeck(), players, isNewSet, set.getFirstPlayerId());
		
		stackTop.setGame(game);
		stackTop.setCardRefTo(game.getTop(0));
		deck.getInfo().setTypeface(typeface);
		
		infosContainer.removeAllViews();
		
		for (Player p : players)
		{
			UIPlayer uip = playerToUIPlayer(p);
			TextView infos = uip.getInfo();
			infos.setTypeface(typeface);
			
			infosContainer.addView(infos);

			TextColorAnimation tca = new TextColorAnimation(infos, colors);
			tca.setEndColor(Color.WHITE);
			
			tcaGroup.add(p.getId(), tca);
			
			set.addPlayer(p);
		}
		
		TypefaceUtils.setAllTypefaces(infosContainer, this, getString(R.string.custom_font));
		
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
		final int soundId;
		Player winner = players.get(0);
		
		if (!game.whoIsPrev().isRealPlayer())
		{
			delay += cpuDelay;
		}
		
		for (Player p : players)
		{
			if (winner.getScore() <= p.getScore())
			{
				winner = p;
			}
		}
		
		if (winner.getTeam() == up.getTeam())
		{
			soundId = sound_win_game;
		}
		else 
		{
			soundId = sound_fail_game;
		}
		
		new Handler().postDelayed(new Runnable(){
			public void run()
			{
				showDialog(DIALOG_FINISH_ID);
				soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
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
							Toast t = Toast.makeText(AgoniaGame.this,
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
						
						/* v2.1 modified - delete */
				    	
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
					game.switchTurn();
					
					/* v2.0a modified */
				    new Handler().postDelayed(new Runnable() {
					    public void run()
					    {
					    	if (!game.turn.isRealPlayer())
							{
					    		cpuAutoPlay();
							}
					    	
					    	stackTop.postSetImage("");
					    	stackTop.postInvalidate();
					    }
				    }, cpuDelay);
				    /* v2.0a end */
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
	@SuppressWarnings("deprecation")
	public void handleSeven(int suit)
	{
		sevenDraw += Agonia.DRAW_WITH_SEVEN;

		/* v2.0b added */
		rlpCpuContainer.height = LayoutParams.FILL_PARENT;
		cpusContainer.setLayoutParams(rlpCpuContainer);
		cpusContainer.requestLayout();
		cpusContainer.invalidate();
		
		game.switchTurn();
		
		try
		{
			if (game.turn.isRealPlayer())
			{
				new Handler().postDelayed(new Runnable(){
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
				{	/*
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
		
		try
		{
			game.draw(game.turn, sevenDraw, true);	
		}
		catch (GameFinished ex) // maybe deck finished
		{
			gameFinished();
			return;
		}
		
		deck.setImage();
		
		/* v2.0b modified. sevenPlayers to game.getPlayers() */
		for (Player p : game.getPlayers())
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
		
		if (!game.turn.isRealPlayer()) 
		{
			cpuAutoPlay();
		}
		else 
		{	/* v2.0b added */
			rlpCpuContainer.height = LayoutParams.WRAP_CONTENT;
			cpusContainer.setLayoutParams(rlpCpuContainer);
			cpusContainer.requestLayout();
			cpusContainer.invalidate();
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
			{	/* v2.1 modified */
				getLayoutInflater().inflate(R.layout.inflateable_uicard, sevenContainer);
				UICard seven = (UICard) sevenContainer.getChildAt(sevenContainer.getChildCount() - 1);
				seven.setVisible(true);
				seven.setCard(c, true);
//				sevenContainer.addView(seven);
			}
			break;

		default:
			break;
		}
		
		DevnDialogUtils.customize(dialog);
		
		super.onPrepareDialog(id, dialog);
	}

	private Dialog makeAceDlg()
	{
		final Dialog retVal;
		
		ScrollView dlgContent = (ScrollView) inflater.inflate(R.layout.dlg_ace, null);
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
			/* v2.1 fixed + modified */
			private final static int UNSELECTED_ALPHA = 0x55;
			private final static int SELECTED_ALPHA = 0xFF;
			
			@SuppressWarnings("deprecation")
			private OnClickListener sevenClickListener = new OnClickListener(){
				
				private void select(LinearLayout container, ImageView selected)
				{
					clean(container);
					
					selected.setAlpha(SELECTED_ALPHA);
				}
				
				private void clean(LinearLayout container)
				{
					int cchilds = container.getChildCount();
					
					for (int i = 0; i < cchilds; i++)
					{
						ImageView iv = (ImageView) container.getChildAt(i);
						
						iv.setAlpha(UNSELECTED_ALPHA);
					}
				}
				
				@Override
				public void onClick(View v)
				{
					select((LinearLayout) v.getParent(), (ImageView) v);
					selectedSeven = ((UICard) v).getCard();
					btnPlay.setEnabled(true);
				}
			};
			
			@Override
			public void onChildViewRemoved(View parent, View child)
			{ }
			
			@SuppressWarnings("deprecation")
			@Override
			public void onChildViewAdded(View parent, View child)
			{
				((ImageView) child).setAlpha(UNSELECTED_ALPHA);	
				child.setOnClickListener(sevenClickListener);
			}
		});
		
		for (Card c : sevens)
		{	/* v2.1 modified */
			getLayoutInflater().inflate(R.layout.inflateable_uicard, sevenContainer);
			UICard seven = (UICard) sevenContainer.getChildAt(sevenContainer.getChildCount() - 1);
			seven.setVisible(true);
			seven.setCard(c, true);
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
				
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); /* v2.1 */
				
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
		
		if (useSFX)
		{
			int r = soundPool.play(sound_deal_cards, 1.0F, 1.0F, 1, 0, 1.0F);
			DBGLog.dbg("" + sound_deal_cards + " ||>> " + r);
		}		
	}

	@Override
	public void onDrawFromDeck(Player who, int n)
	{
		GAME_EVENTS = GAME_EVENTS + who.getName() + " draw " + n + "\n";
		
		if (useSFX && n != 7)
		{
			long delay = cpuDelay / n;
			
			for (int i = 0; i < n; i++)
			{
				Runnable r = new Runnable(){
					public void run()
					{
						soundPool.play(sound_draw_card, 1.0F, 1.0F, 1, 0, 1.0F);
					}
				};
				
				new Handler().postDelayed(r, delay * i);
			}
		}
	}

	@Override
	public void onPlay(Player who, Card c)
	{
		GAME_EVENTS = GAME_EVENTS + who.getName() + " -> " + c + "\n" ;
		
		Runnable r = new Runnable(){
			public void run()
			{
				soundPool.play(sound_play_card, 1.0F, 1.0F, 1, 0, 1.0F);
			}
		};
		
		new Handler().postDelayed(r, who.isRealPlayer() ? 0 : cpuDelay - 410);
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

	@SuppressWarnings("deprecation")
	@Override
	public void onSwitchTurn(Player prev, final Player cur)
	{	
		tcaGroup.pauseAll(); /*be safe, cause nine causes two TextViews to animate */
		
		tcaGroup.start(cur.getId());
		
		/* v2.0b */
		if (sevenDraw > 0)
		{
			return;
		}
		
		/* v2.0a */
		long delay = 10;
		Handler handler = new Handler();
		
		if (cur.isRealPlayer())
		{
			delay = cpuDelay;
		}
		
		handler.postDelayed(new Runnable(){
			public void run()
			{	
				if (cur.isRealPlayer())
				{
					rlpCpuContainer.height = LayoutParams.WRAP_CONTENT;
				}
				else 
				{
					rlpCpuContainer.height = LayoutParams.FILL_PARENT;
				}
				cpusContainer.setLayoutParams(rlpCpuContainer);
				cpusContainer.invalidate();
			}
		}, delay);
	}
}