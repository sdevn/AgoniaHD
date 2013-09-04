package devN.games.agonia;

import static devN.games.Card.NULL_CARD;
import static devN.games.agonia.Const.CLOUD_SLOT_ELO;
import static devN.games.agonia.GameSetActivity.ID_AI_MODES;
import static devN.games.agonia.GameSetActivity.ID_GAMESET;
import static devN.games.agonia.GameSetActivity.ID_NEW_SET;
import static devN.games.agonia.GameSetActivity.ID_PLAYERS_NUM;
import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.appstate.OnStateLoadedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeReliableMessageSentListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.tapjoy.TapjoyConnect;
import com.tapjoy.TapjoySpendPointsNotifier;
import devN.etc.DBGLog;
import devN.etc.DevnDialogUtils;
import devN.etc.TextColorAnimation;
import devN.etc.TextColorAnimationGroup;
import devN.etc.TypefaceUtils;
import devN.etc.dragdrop.DragController;
import devN.etc.dragdrop.DragSource;
import devN.games.Card;
import devN.games.CardGame;
import devN.games.GameSet;
import devN.games.Player;
import devN.games.UICard;
import devN.games.UIDeck;
import devN.games.UIPlayer;
import devN.games.UIStackTop;
import devN.games.agonia.AgoniaAI.AgoniaAIBuilder;

public class AgoniaGame extends BaseGameActivity implements DragSource, OnTouchListener, AgoniaGameListener, OnStateLoadedListener
{
	private final static boolean DEBUG = false;

	// TODO: Menu button function. {Enable/Disable music/sfx, how to play, see preferences}

	public static final int DIALOG_ACE_ID			= 0;
	public static final int DIALOG_SEVEN_ID			= 1;
	public static final int DIALOG_FINISH_ID		= 2; 	// game finished
	public static final int DIALOG_EXIT_ID			= 3;
	public static final int DIALOG_HISTORY_ID		= 4;

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
	private static boolean useSFX;
	private static boolean useMusic;
	private MediaPlayer mediaPlayer;
	private static SoundPool soundPool;
	private static int sound_deal_cards;
	private static int sound_draw_card;
	private static int sound_play_card;
	private static int sound_fail_game;
	private static int sound_win_game;
	private static int sound_clock_ticking;		// v3.0
	
	/* v3.0 */
	private static final long INTERVAL_AUTOPLAY	= 1 * 45 * 1000;
	private static final long INTERVAL_WARNING	= 1 * 35 * 1000;
	
	public static final byte MSG_RAND		= 0;
	public static final byte MSG_PLAY		= 1;
	public static final byte MSG_ACE		= 2;
	public static final byte MSG_SEVEN		= 3;
	public static final byte MSG_SEVEN_DRAW	= 4;
	public static final byte MSG_QUIT		= 5;
	public static final byte MSG_CHAT		= 6; // TODO
	public static final byte MSG_NAME		= 7;
	public static final byte MSG_ELO		= 8;
	
	private Handler mHandler;
	private Dialog frontDialog = null;
	
	@SuppressWarnings("unused")
	private ImageButton ibtnBubble;
	private ImageView ivAlarm;
	private Animation alarmAnimation;
	
	private String mRoomId;
	private String myId;
	private List<Participant> participants;
	private boolean isOnline;
	private boolean isGameStarted = false;
	
	private EloEntity myEloEntity;
	private EloEntity opEloEntity;
	
	public AgoniaGame()
	{
		super(BaseGameActivity.CLIENT_APPSTATE | BaseGameActivity.CLIENT_GAMES);
	} 
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
     	
		mHandler = new Handler();
		
       	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
       	
       	useSFX = prefs.getBoolean(getString(R.string.key_game_sfx), true);
       	initSoundPool(this, useSFX);
       	useMusic = prefs.getBoolean(getString(R.string.key_game_music), false);

       	/* default values should be same as res/xml/pref.xml */
       	// v2.4c
       	boolean oldDeck = prefs.getBoolean(getString(R.string.key_old_deck), false);
       	boolean oldAnim = prefs.getBoolean(getString(R.string.key_old_anim), false);
       	boolean colorHints = prefs.getBoolean(getString(R.string.key_color_hints), true);
       	boolean isNineSpecial = prefs.getBoolean(getString(R.string.key_is_nine_special), true);
       	boolean aceOnAce = prefs.getBoolean(getString(R.string.key_ace_on_ace), false);
       	boolean aceFinish = prefs.getBoolean(getString(R.string.key_ace_finish), false);
       	int deckFinishOption = Integer.parseInt(prefs.getString(getString(R.string.key_deck_finish), "0"));
       	// v2.5
       	boolean nineFinish = prefs.getBoolean(getString(R.string.key_nine_finish), false);
       	boolean sevenFinish = prefs.getBoolean(getString(R.string.key_seven_finish), false);
       	boolean eightFinish = prefs.getBoolean(getString(R.string.key_eight_finish), false);
       	
       	UICard.setUseOldDeck(oldDeck);
       	UIPlayer.setUseOldAnim(oldAnim);
       	UIStackTop.setColorHints(colorHints);
       	Agonia.setNineSpecial(isNineSpecial);
       	Agonia.setAceOnAce(aceOnAce);
       	Agonia.setAceFinish(aceFinish);
       	Agonia.setDeckFinishOption(deckFinishOption);
       	Agonia.setNineFinish(nineFinish);
       	Agonia.setSevenFinish(sevenFinish);
       	Agonia.setEightFinish(eightFinish);
       	
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
		isOnline = intent.getBooleanExtra(Const.ID_IS_ONLINE, false);
		
		tcaGroup = new TextColorAnimationGroup();
		inflater = getLayoutInflater();

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		
		cpuDelay = res.getInteger(R.integer.cpu_delay);

		setContentView(R.layout.game);
		
		p1Name = prefs.getString(getString(R.string.key_p1_name), getString(R.string.default_p1_name));
		up = (UIPlayer) findViewById(R.id.player);
		up.setInfo();
		up.setName(p1Name);
		up.initDimensions(displayMetrics.widthPixels, displayMetrics.heightPixels / 3,
							displayMetrics.widthPixels, displayMetrics.heightPixels);
		up.setOnTouchListener(this);
		up.setOnPlayAnimListener(invalidateListener);

		aCpuNames[0] = prefs.getString(getString(R.string.key_p2_name), getString(R.string.cpu1));
		aCpuNames[1] = prefs.getString(getString(R.string.key_p3_name), getString(R.string.cpu2));
		aCpuNames[2] = prefs.getString(getString(R.string.key_p4_name), getString(R.string.friend));
		
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
				game.play(up.getPlayer(), NULL_CARD);	// 2.3.2 to hold stats of player passo
				broadCastMessage(ByteBuffer.allocate(1 + 2)
						.put(MSG_PLAY).put(NULL_CARD.toByteArray()).array(), 
					null);
				cpuAutoPlay();  // -> heirizete moni tis ta exceptions tou CPU
			}
		});
		
		ivAlarm = (ImageView) findViewById(R.id.iv_alarm);
		alarmAnimation = AnimationUtils.loadAnimation(this, R.anim.alarm_animation);
		
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

		if (isOnline)
		{
			ibtnBubble = (ImageButton) findViewById(R.id.ibtn_bubble);
			
			findViewById(R.id.gameContainer).setVisibility(View.GONE);
			findViewById(R.id.game_waiting_container).setVisibility(View.VISIBLE);
			
			AnimationListener animationListener = new AnimationListener(){
				
				@Override
				public void onAnimationStart(Animation animation)
				{ }
				
				@Override
				public void onAnimationRepeat(Animation animation)
				{
					UICard uiCard = (UICard) findViewById(R.id.uicard_left);
					uiCard.setCard(Card.getRandomCard(), true);
					
					uiCard = (UICard) findViewById(R.id.uicard_right);
					uiCard.setCard(Card.getRandomCard(), true);

					//TODO change txv_waiting text with tips
				}
				
				@Override
				public void onAnimationEnd(Animation animation)
				{ }
			};
			
			RotateAnimation animLeft1 = new RotateAnimation(0, 360, 
						Animation.RELATIVE_TO_PARENT, .5f, 
						Animation.RELATIVE_TO_SELF, .5f);
			animLeft1.setRepeatCount(Animation.INFINITE);
			animLeft1.setDuration(2400);
			animLeft1.setAnimationListener(animationListener);

			
			AnimationSet leftAnimation = new AnimationSet(true);
			leftAnimation.addAnimation(animLeft1);
//			leftAnimation.addAnimation(animLeft2);

			UICard uiCard = (UICard) findViewById(R.id.uicard_left);
			uiCard.setCard(Card.getRandomCard(), true);
			uiCard.startAnimation(leftAnimation);
			
			RotateAnimation animRight1 = new RotateAnimation(0, -360,
					Animation.ABSOLUTE, getResources().getDimensionPixelSize(R.dimen.UICard_width) - displayMetrics.widthPixels / 2f, 
					Animation.RELATIVE_TO_SELF, .5f);
			animRight1.setRepeatCount(Animation.INFINITE);
			animRight1.setDuration(2400);

			AnimationSet rightAnimation = new AnimationSet(true);
			rightAnimation.addAnimation(animRight1);
//			rightAnimation.addAnimation(animRight2);
			
			uiCard = (UICard) findViewById(R.id.uicard_right);
			uiCard.setCard(Card.getRandomCard(), true);
			uiCard.startAnimation(rightAnimation);
			
			//set default options
			Agonia.setNineSpecial(true);
	       	Agonia.setAceOnAce(false);
	       	Agonia.setAceFinish(false);
	       	Agonia.setDeckFinishOption(0);
	       	Agonia.setNineFinish(false);
	       	Agonia.setSevenFinish(true);
	       	Agonia.setEightFinish(false);
		}
		else 
		{
			newGame();
		}
		
	}

	@Override
	protected void onStart()
	{
		if (useMusic && mediaPlayer == null)
		{
			try
			{
				mediaPlayer = MediaPlayer.create(this, R.raw.game_back_sound);
				mediaPlayer.setLooping(true);
			}
			catch (Exception ex)
			{
				onPlaySoundFail(this, false);
			}
		}
		
		initSoundPool(this, useSFX);
		
		super.onStart();
	}

	@Override
	protected void onResume()
	{
		if (useMusic && mediaPlayer != null)
		{
			try
			{
				mediaPlayer.setVolume(MUSIC_GAME_VOLUME, MUSIC_GAME_VOLUME);
				mediaPlayer.start();
			}
			catch (Exception ex)
			{
				onPlaySoundFail(this, false);
			}
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
			soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
			
			try
			{
				sound_deal_cards = soundPool.load(context, R.raw.deal_cards, 1);
				sound_draw_card = soundPool.load(context, R.raw.draw_card, 1);
				sound_play_card = soundPool.load(context, R.raw.play_card, 1);
				sound_fail_game = soundPool.load(context, R.raw.fail_game, 1);
				sound_win_game = soundPool.load(context, R.raw.win_game, 1);
				sound_clock_ticking = soundPool.load(context, R.raw.clock_ticking, 1);
			}
			catch (Exception ex)
			{
				
			}
		}
	}

	/**
	 * v2.1a
	 * solving a soundpool {@link java.lang.NullPointerException}
	 */
	private int playSound(int soundId)
	{
		if (!useSFX)
		{
			return 0;
		}

		if (soundPool == null)
		{
			initSoundPool(this, true);
		}
		
		try
		{
			return soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
		}
		catch (Exception ex)
		{
			onPlaySoundFail(this, true);
			return 0;
		}
	}
	
	/**
	 * v2.1b
	 * Handles the failback of a {@link MediaPlayer} or {@link SoundPool} creation/play
	 * @param wasSFX true means SoundPool fail, otherwise MediaPlayer fail
	 */
	public static void onPlaySoundFail(Context context, boolean wasSFX)
	{
		int prefKey = wasSFX ? R.string.key_game_sfx : R.string.key_game_music;
		String problemPref = wasSFX ? "sound effects" : "background music";
		
		PreferenceManager.getDefaultSharedPreferences(context)
		.edit()
		.putBoolean(context.getString(prefKey), false)
		.commit();
				
		if (wasSFX)
		{
			useSFX = false;
		}
		else 
		{
			useMusic = false; 
		}
		
		// TODO: localize it!
		Toast
		.makeText(context, 
					"A problem with " + problemPref + " occurred.\n" + problemPref + " turned off", 
					Toast.LENGTH_SHORT)
		.show();
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

		game = new Agonia(deck.getDeck(), players, isNewSet, set.getFirstPlayerId(), false, 0);
		
		isGameStarted = true;
		
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
	
	private boolean finished = false;
	
	@SuppressWarnings("deprecation")
	protected void gameFinished() 
	{
		CardGame.unregisterGameListener(this);
		removeTimeoutRunnables();
		if (finished)
		{
			return;
		}
		
		finished = true;
		
		set.gameFinished();
		
		long delay = 200;
		final int soundId;
		Player winner = players.get(0);
		
		if (!game.whoIsPrev().equals(up.getPlayer()))
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
		
		if (isOnline)
		{
			if (myEloEntity == null)
			{
				myEloEntity = new EloEntity();
			}
			
			if (opEloEntity == null)
			{
				opEloEntity = new EloEntity();
			}
			
			try
			{
				if (winner.getTeam() == up.getTeam())
				{
					myEloEntity.addPoints(winner.getScore());
					myEloEntity.win();
					opEloEntity.lose();
					EloCalculator.setEloPoints(myEloEntity, opEloEntity);

					getGamesClient().submitScore(getString(R.string.leaderboard_game_points), 
													myEloEntity.getPoints());
					
					getGamesClient().submitScore(getString(R.string.leaderboard_total_online_wins), 
													myEloEntity.getWins());
					
					if (myEloEntity.getMaxWinStreak() == myEloEntity.getCurrentWinStreak())
					{
						getGamesClient().submitScore(getString(R.string.leaderboard_best_winning_streak), 
														myEloEntity.getCurrentWinStreak());
					}
				}
				else
				{
					opEloEntity.win();
					myEloEntity.lose();
					EloCalculator.setEloPoints(opEloEntity, myEloEntity);
				}
				
				getAppStateClient().updateState(CLOUD_SLOT_ELO, myEloEntity.getBytes());
				getGamesClient().submitScore(getString(R.string.leaderboard_elo_points), 
												myEloEntity.getElo());
				
				if (myEloEntity.getTodayGames() > Const.MAX_DAILY_GAMES)
				{
					TapjoyConnect.getTapjoyConnectInstance().spendTapPoints(1,
							new TapjoySpendPointsNotifier(){
								@Override
								public void getSpendPointsResponseFailed(String error)
								{ 
									DBGLog.dbg("Tapjoy spend points error. " + error);
								}

								@Override
								public void getSpendPointsResponse(String currencyName, int pointTotal)
								{
									DBGLog.dbg("Tapjooy spend! New points: " + pointTotal);
								}
							});
				}
			}
			catch (Exception ex)
			{
				DBGLog.dbg("ELO Calculate error: " + ex.getMessage());
				Toast.makeText(this, "Communication error!! Maybe the game is not recorded.", Toast.LENGTH_SHORT).show();
			}
		}
		
		new Handler().postDelayed(new Runnable(){
			public void run()
			{
				showDialog(DIALOG_FINISH_ID);
				playSound(soundId);
			}
		}, delay);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onBackPressed()
	{
		showDialog(DIALOG_EXIT_ID);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add("Report bug!").setOnMenuItemClickListener(new OnMenuItemClickListener(){
			
			@Override
			public boolean onMenuItemClick(MenuItem item)
			{
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
			            "mailto","devn@hotmail.gr", null));
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[Agonia HD beta] " + up.getPlayer().getName());
				emailIntent.putExtra(Intent.EXTRA_TEXT, "\n\nGame log:\n" + GAME_EVENTS + "\n--------\n\n\n");
				startActivity(Intent.createChooser(emailIntent, "Send email..."));
				
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@SuppressWarnings("deprecation")
	public void playerPlay(Player p, Card c)
	{
		deck.setImage();

		byte[] msg = ByteBuffer.allocate(1 + 2)
						.put(MSG_PLAY)
						.put(c.toByteArray())
						.array();
		
		broadCastMessage(msg, null);

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
		if (game.turn.isRealPlayer() || isOnline)
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
			if (game.turn.equals(up.getPlayer()))
			{
				new Handler().postDelayed(new Runnable(){
					public void run()
					{
						showDialog(DIALOG_SEVEN_ID);
					}
				}, 200);
			}
			else if (!isOnline)
			{	
				UIPlayer ucp = turnToUIPlayer();
				
				try
				{
					game.play(ucp.getPlayer(), ucp.playSeven());
				}
				catch (GameFinished e)
				{	// v2.5 modified, if sevenFinish is enabled 
					// the exception will thrown and handled
					
					stackTop.postSetImage("", cpuDelay - 200);
					gameFinished();
					
					return;
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
		
		if (!game.turn.equals(up.getPlayer())) 
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
		frontDialog = null;
		
		switch (id)
		{
		case DIALOG_ACE_ID:
			{
			frontDialog = dialog = makeAceDlg();
			}
			break;
												
		case DIALOG_SEVEN_ID:
			{
			frontDialog = dialog = makeSevenDlg();
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
			
		case DIALOG_HISTORY_ID:
			{
			dialog = makeHistoryDlg();
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
		
		DevnDialogUtils.embedAd(dialog, this);
		
		super.onPrepareDialog(id, dialog);
	}

	private Dialog makeAceDlg()
	{
		final Dialog retVal;
		
		ScrollView dlgContent = (ScrollView) inflater.inflate(R.layout.dlg_ace, null);
		final ImageButton suitButtons[] = new ImageButton[4];
		
		suitButtons[Card.iSPADES] = (ImageButton) dlgContent.findViewById(R.id.spades);
		suitButtons[Card.iDIAMONDS] = (ImageButton) dlgContent.findViewById(R.id.diamonds);
		suitButtons[Card.iCLUBS] = (ImageButton) dlgContent.findViewById(R.id.clubs);
		suitButtons[Card.iHEARTS] = (ImageButton) dlgContent.findViewById(R.id.hearts);
		
		// @formatter:off

		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle(getString(R.string.dlg_ace_title))
		.setCancelable(true)
		.setView(dlgContent)
		.setOnCancelListener(new DialogInterface.OnCancelListener(){
			
			@Override
			public void onCancel(DialogInterface dialog)
			{
				suitButtons[stackTop.getCard().getSuit()].performClick();
				frontDialog = null;
			}
		})
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
		if (isOnline)
		{
			byte[] msg = ByteBuffer.allocate(1 + 1)
						.put(MSG_ACE)
						.put((byte) suit)
						.array();
			
			broadCastMessage(msg, null);
		}
		
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
		final Button btnDraw = (Button) dlgContent.findViewById(R.id.dlg_seven_btn_draw);
		final Button btnPlay = (Button) dlgContent.findViewById(R.id.dlg_seven_btn_play);
		
		btnDraw.setText(getString(R.string.dlg_seven_btn_draw, sevenDraw));
		btnPlay.setText(getString(R.string.dlg_seven_btn_play));

		sevenContainer.setOnHierarchyChangeListener(new OnHierarchyChangeListener(){
			/* v2.1 fixed + modified */
			/* v2.3 by default selects the last added seven */
			
			private final static int UNSELECTED_ALPHA = 0x55;
			private final static int SELECTED_ALPHA = 0xFF;
			
			@SuppressWarnings("deprecation")
			private OnClickListener sevenClickListener = new OnClickListener(){
				
				private void select(LinearLayout container, ImageView selected)
				{
					clean(container);
					
					selected.setTag(Boolean.TRUE);
					selected.setAlpha(SELECTED_ALPHA);
				}
				
				private void clean(LinearLayout container)
				{
					int cchilds = container.getChildCount();
					
					for (int i = 0; i < cchilds; i++)
					{
						ImageView iv = (ImageView) container.getChildAt(i);
						iv.setTag(Boolean.FALSE);
						iv.setAlpha(UNSELECTED_ALPHA);
					}
				}
				
				@Override
				public void onClick(View v)
				{
					DBGLog.dbg("Seven click " + ((UICard) v).getCard() + " t:" + v.getTag());
					if (Boolean.TRUE.equals(v.getTag()))
					{
						btnPlay.performClick();
					}
					else 
					{
						select((LinearLayout) v.getParent(), (ImageView) v);
						selectedSeven = ((UICard) v).getCard();
						btnPlay.setEnabled(true);
					}
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
				
				child.performClick(); // v2.3
				child.setTag(Boolean.TRUE); // v3.0
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
				btnDraw.performClick();	
				frontDialog = null;
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
				broadCastMessage(new byte[] {MSG_SEVEN_DRAW}, null);
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
					broadCastMessage(new byte[] {MSG_SEVEN, (byte) selectedSeven.getSuit()}, null);
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
		.setMessage(getString(isGameStarted ? R.string.dlg_exit_message : R.string.dlg_exit_message_not_started))
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				if (isOnline)
				{
					if (isGameStarted)
					{
						broadCastMessage(new byte[]{MSG_QUIT}, null);
						if (game.isGameFinished())
						{
							return;
						}
					}
					else 
					{						
						finish();
						overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
						
						return;
					}
				}
				
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
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				Intent intent = new Intent(AgoniaGame.this, GameSetActivity.class);
				
				intent.putExtra(ID_NEW_SET, false);
				intent.putExtra(ID_GAMESET, (Parcelable) set);
				
				startActivity(intent);
				
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); /* v2.1 */
				
				finish();
			}
		})
		.setNeutralButton(R.string.dlg_finish_btn_history, new DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				new Handler().postDelayed(new Runnable(){
					@SuppressWarnings("deprecation")
					public void run()
					{
						showDialog(DIALOG_HISTORY_ID);
					}
				}, 250);
			}
		})
		.setCancelable(false);
		
		// @formatter:on
		return builder.create();
	}
	
	private Dialog makeHistoryDlg()
	{
		// @formatter:off 
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle(getString(R.string.dlg_history_title))
		.setMessage(GAME_EVENTS)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				new Handler().postDelayed(new Runnable(){
					@SuppressWarnings("deprecation")
					public void run()
					{
						showDialog(DIALOG_FINISH_ID);
					}
				}, 250);
			}
		})
		.setCancelable(false)
		;
		
		// @formatter:on
		
		return builder.create();
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if (!game.turn.equals(up.getPlayer()) || game.isGameFinished())
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
					broadCastMessage(ByteBuffer.allocate(1 + 2)
										.put(MSG_PLAY).put(NULL_CARD.toByteArray()).array(), 
									null);
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

		if (p.isRealPlayer() && p.equals(up.getPlayer()))
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
		GAME_EVENTS = getString(R.string.players) + " " + cPlayers + " | " + stackTopCards.get(0) + "\n\n";
		
		playSound(sound_deal_cards);	
	}

	@Override
	public void onDrawFromDeck(Player who, int n)
	{
		GAME_EVENTS = GAME_EVENTS + who.getName() + " " + getString(R.string.drew_cards, n) + "\n";
		
		if (n != 7 
		&& n != 0)	// v2.4 to avoid division by zero
		{
			long delay = cpuDelay / n;
			
			for (int i = 0; i < n; i++)
			{
				Runnable r = new Runnable(){
					public void run()
					{
						playSound(sound_draw_card);
					}
				};
				
				new Handler().postDelayed(r, delay * i);
			}
		}
		
		removeTimeoutRunnables();
			
		postTimeoutRunnables(who.equals(up.getPlayer()));
	}

	@Override
	public void onPlay(Player who, Card c)
	{
		GAME_EVENTS = GAME_EVENTS + who.getName() + " -> " + c + "\n" ;
		
		Runnable r = new Runnable(){
			public void run()
			{
				playSound(sound_play_card);
			}
		};
		
		mHandler.postDelayed(r, who.equals(up.getPlayer()) ? 10 : cpuDelay - 410);
		
		removeTimeoutRunnables();
		postTimeoutRunnables(
					(who.equals(up.getPlayer()) && c.getRank() == 1)
					|| (!who.equals(up.getPlayer()) && c.getRank() == 7));
		}
	
	@Override
	public void onAceSuitSelected(int selectedSuit)
	{
		GAME_EVENTS = GAME_EVENTS + "\t" + getString(R.string.how_special_card_ace_title) 
						+ " -> " + Card.pszSUITS[selectedSuit] + "\n" ;
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
		
		if (cur.isRealPlayer())
		{
			delay = cpuDelay;
		}
		
		mHandler.postDelayed(new Runnable(){
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
		
		/* v3.0 */
		if (cur.equals(up.getPlayer()))
		{
			postTimeoutRunnables();
		}
		else 
		{
			removeTimeoutRunnables();
		}
	}

	@Override
	public void onGameFinished(List<Player> players)
	{ }

	@Override
	public void onPasso(Player who)
	{
		if (sevenDraw == 0)
		{
			GAME_EVENTS = GAME_EVENTS + who.getName() + " passo\n";
		}
	}
	
	@Override
	public void onDeckFinished(int deckFinishOption)
	{ 
		GAME_EVENTS = GAME_EVENTS + "\n" + getString(R.string.how_deck) + " "
					+ getResources().getStringArray(R.array.deck_finish_options)[deckFinishOption] + "\n\n";
	}
	
	// v3.0
	private int soundStreamId = 0;
	private boolean eloEntitySend = false;
	
	@Override
	public void onStateLoaded(int statusCode, int stateKey, byte[] localData)
	{
		if (statusCode == AppStateClient.STATUS_OK)
		{
			myEloEntity = new EloEntity(localData);
			((TextView) findViewById(R.id.txv_my_elo))
				.setText(myEloEntity.toLocalizedString(getString(R.string.elo_localized_format)));
			
			if (isGameStarted && isOnline && !eloEntitySend)
			{
				broadCastMessage(ByteBuffer.allocate(1 + localData.length)
									.put(MSG_ELO)
									.put(localData).array(),
								null);
				findViewById(R.id.txv_my_elo).setVisibility(View.VISIBLE);

				eloEntitySend = true;
			}
		}
		DBGLog.rltm("load state " + statusCode);
	}
	
	@Override
	public void onStateConflict(int stateKey, String resolvedVersion, byte[] localData, byte[] serverData)
	{
		switch (stateKey)
		{
		case CLOUD_SLOT_ELO:
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
	
	private final Runnable timeoutWarningRunnable = new Runnable(){
		
		@Override
		public void run()
		{
			if (game.turn.equals(up.getPlayer()) || sevenDraw > 0)
			{
				ivAlarm.setVisibility(View.VISIBLE);
				ivAlarm.startAnimation(alarmAnimation);
				soundStreamId = playSound(sound_clock_ticking);
				mHandler.postDelayed(this, INTERVAL_AUTOPLAY - INTERVAL_WARNING + 100);
			}
			else 
			{
				ivAlarm.clearAnimation();
				ivAlarm.setVisibility(View.INVISIBLE);
				ivAlarm.postInvalidate();
				if (soundStreamId != 0)
				{
					soundPool.stop(soundStreamId);
				}
			}
		}
	};
	
	private final Runnable timeoutPlayRunnable = new Runnable(){
		
		@Override
		public void run()
		{
			if (!isOnline)
			{
				return;
			}
			
			if (game.turn.equals(up.getPlayer()) || sevenDraw > 0)
			{
				if (frontDialog != null)
				{
					frontDialog.cancel();
					removeTimeoutRunnables();
				}
				else if (game.canDraw(up.getPlayer()))
				{
					try
					{
						game.draw(up.getPlayer());
						broadCastMessage(ByteBuffer.allocate(1 + 2)
											.put(MSG_PLAY).put(NULL_CARD.toByteArray()).array(), 
										null);
						deck.setImage("btn_paso");
					}
					catch (GameFinished ex)
					{
						gameFinished();
					}
				}
				else 
				{
					playerPlay(up.getPlayer(), NULL_CARD);
				}
			}	
		}
	};
	
	private void postTimeoutRunnables(boolean condition)
	{
		if (condition)
		{
			postTimeoutRunnables();
		}
	}
	
	private void postTimeoutRunnables()
	{
		if (!isOnline)
		{
			return;
		}
		DBGLog.dbg("Posting callbacks");
		mHandler.postDelayed(timeoutWarningRunnable, INTERVAL_WARNING);
		mHandler.postDelayed(timeoutPlayRunnable, INTERVAL_AUTOPLAY);
	}
	
	private void removeTimeoutRunnables()
	{
		DBGLog.dbg("Removing callbacks");
		ivAlarm.clearAnimation();
		ivAlarm.setVisibility(View.INVISIBLE);
		ivAlarm.postInvalidate();
		mHandler.removeCallbacks(timeoutWarningRunnable);
		mHandler.removeCallbacks(timeoutPlayRunnable);
		if (soundStreamId != 0)
		{
			soundPool.stop(soundStreamId);
		}
	}
	
	private void broadCastMessage(byte[] msg, RealTimeReliableMessageSentListener listener)
	{
		if (!isOnline || ((game != null) && game.isGameFinished()))
		{
			return;
		}
		
		for (Participant p : participants) 
		{
			String pId = p.getParticipantId();
			
	    	if (!TextUtils.equals(myId, pId))
			{
				getGamesClient().sendReliableRealTimeMessage(listener, msg, mRoomId, pId);
			}
		}
	}
	
	private void oponnentQuit()
	{
		if (game.isGameFinished)
		{// maybe both quit at the same time
			return;
		}
		
		for (Player p : game.getPlayers())
		{
			if (p.getTeam() == up.getTeam())
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
	
	private void newOnlineGame(long seed)
	{
		findViewById(R.id.uicard_left).clearAnimation();
		findViewById(R.id.uicard_right).clearAnimation();
		findViewById(R.id.game_waiting_container).setVisibility(View.GONE);
		findViewById(R.id.gameContainer).setVisibility(View.VISIBLE);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String prefColor, defColor;
		int[] colors = new int[2];
		Typeface typeface = TypefaceUtils.get(this, getString(R.string.custom_font));
		
		Agonia.addGameListener(this);

		defColor = getString(R.string.pref_player_turn_default_color);
		
		prefColor = preferences.getString(getString(R.string.key_player_turn_color1), defColor);
		colors[0] = Color.parseColor(prefColor);
		
		prefColor = preferences.getString(getString(R.string.key_player_turn_color2), defColor);
		colors[1] = Color.parseColor(prefColor);
		
		if (preferences.getBoolean(getString(R.string.key_publish_name), true))
		{
			byte[] pname = up.getName().getBytes();
			broadCastMessage(ByteBuffer.allocate(1 + pname.length)
								.put(MSG_NAME)
								.put(pname).array() , null);
		}

		game = new Agonia(deck.getDeck(), players, isNewSet, set.getFirstPlayerId(), 
							true, seed);
		isGameStarted = true;
		
		if (!eloEntitySend && myEloEntity != null)
		{
			byte[] eloData = myEloEntity.getBytes();
			broadCastMessage(ByteBuffer.allocate(1 + eloData.length)
								.put(MSG_ELO)
								.put(eloData).array(),
							null);
			findViewById(R.id.txv_my_elo).setVisibility(View.VISIBLE);
			eloEntitySend = true;
		}
		
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
	}
	
	public void onlinePlay(Player p, Card choosed)
	{
		try
		{		
			if (choosed.equals(NULL_CARD))
			{
				if (game.canDraw(p))
				{
					game.draw(p);
				}
				else
				{
					game.play(p, choosed);
					// TODO customize it, personalize it for each cpu
					int gravity;
					int xOffset = 0;
					Toast t = Toast.makeText(AgoniaGame.this,
							"Passo",
							Toast.LENGTH_SHORT);
					
					if (cCpu == 1 || p.getTeam() == up.getTeam())
					{// 1vs1 or Friend
						gravity = Gravity.CENTER_HORIZONTAL;
					}
					else if (p.equals(aupCpu[1]) || p.getTeam() == 3) 
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
				game.play(p, choosed);
			}
			
			stackTop.postSetImage("", cpuDelay);
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
		catch (AcePlayed ex) 
		{
			stackTop.postSetImage("", cpuDelay);
		}
	}
	
	@Override
	public void onSignInFailed()
	{
		if (isOnline)
		{
			finish();
			overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		}
	}

	@Override
	public void onSignInSucceeded()
	{
		if (isOnline && !isGameStarted)
		{
			getAppStateClient().loadState(this, CLOUD_SLOT_ELO);
			
			Bundle am = RoomConfig.createAutoMatchCriteria(1, 1, 0);
	
		    // build the room config:
		    RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
		    roomConfigBuilder.setAutoMatchCriteria(am);
		    RoomConfig roomConfig = roomConfigBuilder.build();
	
		    // create room:
		    getGamesClient().createRoom(roomConfig);
	
		    // prevent screen from sleeping during handshake
		    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);		
		}
	}
	
	private RoomConfig.Builder makeBasicRoomConfigBuilder() {
	    return RoomConfig.builder(roomUpdateListener)
	            .setMessageReceivedListener(realTimeMessageReceivedListener)
	            .setRoomStatusUpdateListener(roomStatusUpdateListener);
	}
	
	private final RoomUpdateListener roomUpdateListener = new RoomUpdateListener() {

		@Override
		public void onJoinedRoom(int statusCode, Room room)
		{ }

		@Override
		public void onLeftRoom(int statusCode, String roomId)
		{ 
			DBGLog.rltm("left room " + statusCode);
		}

		@Override
		public void onRoomConnected(int statusCode, Room room)
		{
			DBGLog.rltm("room connected " + statusCode);
			
			List<Entry<Integer, String>> onlinePlayers = new ArrayList<Map.Entry<Integer, String>>();
			
			for (Participant p : participants) 
			{
				String pId = p.getParticipantId();
				String name = p.getDisplayName();
				
				if (TextUtils.equals(pId, myId))
				{
					up.setPlayerTo(p);					
				}
				else 
				{
					aupCpu[0].setPlayerTo(p);
				}
				
				onlinePlayers.add(new SimpleEntry<Integer, String>(pId.hashCode(), name));
			}
			
			// leader is the participant that generates the random params of game
			int leaderId = Collections.min(onlinePlayers, new Comparator<Entry<Integer, String>>(){

				@Override
				public int compare(Entry<Integer, String> lhs, Entry<Integer, String> rhs)
				{
					return lhs.getKey().compareTo(rhs.getKey());
				}	
			}).getKey();
			
			if (myId.hashCode() == leaderId)
			{
				long seed = new Random().nextLong();
				
				DBGLog.rltm("im lead seed: " + seed);
				
				byte[] msgRand = ByteBuffer.allocate(1 + 8)
								.put(MSG_RAND)
								.putLong(seed)
								.array();
				
				broadCastMessage(msgRand, null);
				
				newOnlineGame(seed);
			}
			else 
			{
				players.remove(up.getPlayer());
				players.add(1, up.getPlayer());
			}
		}

		@Override
		public void onRoomCreated(int statusCode, Room room)
		{
			DBGLog.rltm("room created");
			
		}	
	};
	
	private final RealTimeMessageReceivedListener realTimeMessageReceivedListener = new RealTimeMessageReceivedListener() {

		@Override
		public void onRealTimeMessageReceived(RealTimeMessage message)
		{
			ByteBuffer msg = ByteBuffer.wrap(message.getMessageData());
			
			DBGLog.rltm("recv " + message.getSenderParticipantId() + " " + msg.get(0));
			
			switch (msg.get())
			{
			case MSG_RAND:
				newOnlineGame(msg.getLong());
				break;
			case MSG_PLAY:
				onlinePlay(game.getPlayerById(message.getSenderParticipantId().hashCode()), 
							new Card(new byte[]{msg.get(), msg.get()}));
				break;
			case MSG_ACE:
				byte suit = msg.get();
				stackTop.setCard(new Card(suit, 1));
				stackTop.postSetImage("");
				
				game.informAceSuitSelected(suit);
				
				game.switchTurn();
				break;
			case MSG_SEVEN_DRAW:
				game.switchTurn();
				sevenDraw();
				break;
			case MSG_SEVEN:
				byte sevSuit = msg.get();
				onlinePlay(game.getPlayerById(message.getSenderParticipantId().hashCode()), 
							new Card(sevSuit, 7));
				break;
			case MSG_QUIT:
				oponnentQuit();
				break;
			case MSG_NAME:
				byte[] pname = new byte[msg.capacity() - 1];
				msg.get(pname);
				playerToUIPlayer(game.getPlayerById(
									message.getSenderParticipantId().hashCode()))
							.setName(new String(pname));
				break;
			case MSG_ELO:
				byte[] eloData = new byte[msg.capacity() - 1];
				msg.get(eloData);
				opEloEntity = new EloEntity(eloData);
				findViewById(R.id.txv_op_elo).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.txv_op_elo)).setText(
												opEloEntity.toLocalizedString(getString(R.string.elo_localized_format)));
				break;
			default:
				break;
			}
		}	
	};

	private final RoomStatusUpdateListener roomStatusUpdateListener = new RoomStatusUpdateListener() {
		// are we already playing?
		boolean mPlaying = false;

		// at least 2 players required for our game
		final static int MIN_PLAYERS = 2;

		// returns whether there are enough players to start the game
		boolean shouldStartGame(Room room) {
		    int connectedPlayers = 0;
		    for (Participant p : room.getParticipants()) {
		        if (p.isConnectedToRoom()) ++connectedPlayers;
		    }
		    return connectedPlayers >= MIN_PLAYERS;
		}

		@Override
		public void onPeersConnected(Room room, List<String> peers) {
		    if (mPlaying) {
		    	DBGLog.rltm("add player");
		    }
		    else if (shouldStartGame(room)) {
//		    	Toast.makeText(AgoniaMenu.this, "should create game", Toast.LENGTH_SHORT).show();
		    }
		}

		@Override
		public void onPeersDisconnected(Room room, List<String> peers) {
			DBGLog.rltm("peer disc ");
	        getGamesClient().leaveRoom(roomUpdateListener, mRoomId);
		    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		 	oponnentQuit();
		}

		@Override
		public void onPeerLeft(Room room, List<String> peers) {
			DBGLog.rltm("peer left ");
		    getGamesClient().leaveRoom(roomUpdateListener, mRoomId);
		    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		    oponnentQuit();
		}

		@Override
		public void onPeerDeclined(Room room, List<String> peers) 
		{ }

		@Override
		public void onConnectedToRoom(Room room)
		{
			DBGLog.rltm("Connected to room");
			
			mRoomId = room.getRoomId();
			participants = room.getParticipants();
	        myId = room.getParticipantId(getGamesClient().getCurrentPlayerId());
		}

		@Override
		public void onRoomConnecting(Room room)
		{
			DBGLog.rltm("room connecting");
			
		}

		@Override
		public void onDisconnectedFromRoom(Room room)
		{
			DBGLog.rltm("disc from room");
			
		}

		@Override
		public void onPeerInvitedToRoom(Room arg0, List<String> arg1)
		{
			DBGLog.rltm("peer invited");
			
		}

		@Override
		public void onPeerJoined(Room arg0, List<String> arg1)
		{
			DBGLog.rltm("peer joined");
			
		}

		@Override
		public void onRoomAutoMatching(Room room)
		{
			DBGLog.rltm("room automatching ");
			
		}
		
	};
}
