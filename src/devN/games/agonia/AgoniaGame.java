package devN.games.agonia;

import static android.content.res.Configuration.SCREENLAYOUT_LONG_MASK;
import static android.content.res.Configuration.SCREENLAYOUT_LONG_YES;
import static android.util.Log.d;
import static devN.games.Card.NULL_CARD;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.WindowManager.BadTokenException;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import devN.etc.dragdrop.DragController;
import devN.etc.dragdrop.DragSource;
import devN.games.Card;
import devN.games.CardGame;
import devN.games.Player;
import devN.games.UICard;
import devN.games.UIDeck;
import devN.games.UIPlayer;
import devN.games.UIStackTop;

public class AgoniaGame extends Activity implements DragSource, OnTouchListener
{
	public static final int DIALOG_ACE_ID		= 0;
	public static final int DIALOG_SEVEN_ID		= 1;
	public static final int DIALOG_FINISH_ID	= 2; 	// game finished
	public static final int DIALOG_SAVE_ID		= 3;	// save game
	public static final int DIALOG_LOAD_ID		= 4;	// load game

	public static final String SAVE_FILE = "agonia.sav";
	public static final String PREF_SAVE = "save.pref";
	public static final String KEY_SAVE = "save";
	public static final String TAG = "dbg";

	protected Agonia game;
	protected UIPlayer up;
	protected UIPlayer ucp;
	protected UICard draggedCard;
	protected UIStackTop stackTop;
	protected UIDeck deck;
	protected int sevenDraw;
	protected long cpuDelay;
	protected boolean dragging = false;
	private DragController dragController;
	private	LayoutInflater inflater;
	private Card selectedSeven = Card.NULL_CARD;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
       	setContentView(R.layout.game);

		AgoniaAI cpuAI;
		String p1Name;
		String p2Name;
		int p1score;
		int p2score;
		int p1wins;
		int p2wins;
		TextView upInfo;
		TextView ucpInfo;
		TextView deckInfo;
		boolean isWideScreen;
		Resources res = getResources();
		
		inflater = getLayoutInflater();
		
		if (F.settings == null)
		{
			new F(this);
		}
		
		Configuration conf = res.getConfiguration();
		isWideScreen = SCREENLAYOUT_LONG_YES == (conf.screenLayout & SCREENLAYOUT_LONG_MASK);
		
		cpuDelay = res.getInteger(R.integer.cpu_delay);

		p1Name = F.getString(F.KEY_P1_NAME, getString(R.string.default_p1_name));
		p1score = F.getInt(F.KEY_P1_SCORE, 0);
		p1wins = F.getInt(F.KEY_P1_WINS, 0);
		
		p2Name = F.getString(F.KEY_P2_NAME, getString(R.string.default_p2_name));
		p2score = F.getInt(F.KEY_P2_SCORE, 0);
		p2wins = F.getInt(F.KEY_P2_WINS, 0);
		
		upInfo = (TextView) findViewById(R.id.pName);
		ucpInfo = (TextView) findViewById(R.id.cpName);
		deckInfo = (TextView) findViewById(R.id.deckSize);
		
		up = (UIPlayer) findViewById(R.id.player);
		up.setInfo(upInfo);
		up.setName(p1Name);
		up.setWins(p1wins);
		up.addScore(p1score);
		up.setOnTouchListener(this);
		
		ucp = (UIPlayer) findViewById(R.id.computer);
		ucp.setInfo(ucpInfo);
		ucp.setName(p2Name);
		ucp.setWins(p2wins);
		ucp.addScore(p2score);
		ucp.setAnimationsDuration(cpuDelay);
		ucp.setOnTouchListener(this);

		dragController = (DragController) findViewById(R.id.dragLayer);
		
		stackTop = (UIStackTop) findViewById(R.id.stackTop);
		
		deck = (UIDeck) findViewById(R.id.deck);
		deck.setInfo(deckInfo);
		deck.setWideScreen(isWideScreen);
		deck.setOnTouchListener(this);

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		UIPlayer.initDimensions(this, displayMetrics.widthPixels, displayMetrics.heightPixels);
		
		int diffMode = Integer.parseInt(F.getString(getString(R.string.key_difficult), getString(R.string.default_difficult_mode)));
		
//		l("mode " + diffMode);
		
		switch (diffMode)
		{
		case AgoniaAI.MODE_MONKEY:
			cpuAI = new MonkeyAI(ucp.getPlayer());
			break;
			
		case AgoniaAI.MODE_EASY:
			cpuAI = new EasyAI(ucp.getPlayer());
			break;
			
		case AgoniaAI.MODE_MODERATE:
			cpuAI = new ModerateAI(ucp.getPlayer());
			break;
				
		case AgoniaAI.MODE_MEGAMIND:
			throw new RuntimeException("MODE_MEGAMIND Not implemented yet!");
//			break;
			
		default:
			throw new RuntimeException("Uknown difficult mode");
//			break;
		}
		ucp.setAI(cpuAI);
		
		deck.hideInfo();

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
				else
				{
					deck.hideInfo();
					deck.setImage();
					game.switchTurn();
					cpuAutoPlay();  // -> heirizete moni tis ta exceptions tou CPU
				}
			}
		});
		
		if (checkLoadGame())
		{
			showDialog(DIALOG_LOAD_ID);
		}
		else 
		{
			newGame();
		}
	}

	private boolean checkLoadGame()
	{
		SharedPreferences pref = getSharedPreferences(PREF_SAVE, MODE_PRIVATE);
		
		return pref.getBoolean(KEY_SAVE, false);
	}
	
	private void newGame()
	{
		game = new Agonia(deck.getDeck(), up.getPlayer(), ucp.getPlayer());

		stackTop.setGame(game);
		stackTop.setCardRefTo(game.getTop(0));
	}
	
	protected void gameFinished() 
	{
		int p1HandScore = game.scoreOf(up.getHand());
		int p2HandScore = game.scoreOf(ucp.getHand());
		
		up.addScore(p2HandScore);
		ucp.addScore(p1HandScore);
		if (p1HandScore > p2HandScore)
		{
			ucp.win();
		}
		else if (p2HandScore > p1HandScore) 
		{
			up.win();
		}
		else 
		{
			/* No winner! */
		}
		saveScores();

		// otan vgainei o player na min uparhei polu anamoni eos otou vgei to finishDialog
		Long dlgDelay = (long) (game.whoPlayNext().equals(up.getPlayer()) ? (cpuDelay / 2) : (cpuDelay * 1.3));
		new Handler().postDelayed(new Runnable(){
			public void run()
			{
				try
				{
					showDialog(DIALOG_FINISH_ID);
				}
				catch (BadTokenException ex)
				{
					/*
					 * ean patisei back button prin vgei o dialogos
					 * petiete exeption epeidi i parent activity tou dialogou
					 * den trehei
					 */
				}
			}
		}, dlgDelay);
	}

	private void saveScores()
	{
		// @formatter:off
	
		F.edit()
		.putInt(F.KEY_P1_SCORE, up.getScore())
		.putInt(F.KEY_P2_SCORE, ucp.getScore())
		.putInt(F.KEY_P1_WINS, up.getWins())
		.putInt(F.KEY_P2_WINS, ucp.getWins())
		.commit();
		
		// @formatter:on
	}

	@Override
	public void onBackPressed()
	{
		if (!game.isGameFinished() && game.turn.equals(up.getPlayer()))
		{
			showDialog(DIALOG_SAVE_ID);
		}
	}

	public void playerPlay(Player p, Card c)
	{
		deck.hideInfo();
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
			gameFinished();
			stackTop.setImage();
		}
	}

	public void cpuAutoPlay()
	{
		if (!game.turn.equals(ucp.getPlayer()))
		{
			d(TAG, "its not my turn!");
			return;
		}
	
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
							// TODO customize it
							Toast t = Toast.makeText(getApplicationContext(),
									"Passo",
									Toast.LENGTH_SHORT);
							t.setGravity(Gravity.TOP, t.getXOffset(), 30);
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
					}
				}
				catch (AcePlayed e)
				{
					int aceSuit = ucp.getAceSuit();
	
					game.getTop(0).setSuit(aceSuit);
					stackTop.postSetImage("", cpuDelay);
					game.switchTurn();
				}
				catch (SevenPlayed e)
				{
					stackTop.postSetImage("", cpuDelay);
					handleSeven(e.suit);
					return;
				}
				catch (GameFinished e)
				{
					stackTop.postSetImage("");
					gameFinished();
				}
			}
	
		}, cpuDelay + 200);
	}

	public void handleSeven(int suit)
	{
		sevenDraw += Agonia.DRAW_WITH_SEVEN;
		try
		{
			if (game.turn.equals(ucp.getPlayer()))
			{
				new Handler().postDelayed(new Runnable(){
					public void run()
					{
//						makeSevenDlg().show();
						showDialog(DIALOG_SEVEN_ID);
					}
				}, cpuDelay + 200);
			}
			else if (game.turn.equals(up.getPlayer()))
			{
				game.switchTurn();
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
			else
			{
				throw new ArrayIndexOutOfBoundsException("pehtike lathos...");
			}
		}
		catch (SevenPlayed e)
		{
			stackTop.postSetImage("", cpuDelay - 200);
			handleSeven(e.suit);
		}
	}

	private void sevenDraw()
	{
		game.switchTurn();
		
		try
		{
			game.draw(game.turn, sevenDraw, true);
		}
		catch (GameFinished ex) 
		{
			gameFinished();
			return;
		}
		
		deck.hideInfo();
		deck.setImage();
		
		sevenDraw = 0;
		
		Player next = game.whoPlayNext();	// should be previous
		
		if (next.getHand().isEmpty())
		{
			game.isGameFinished = true;
			gameFinished();
			return;
		}

		if (game.turn.equals(ucp.getPlayer())) 
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
			
		case DIALOG_SAVE_ID:
			{
			dialog = makeSaveGameDlg();
			}
			break;
			
		case DIALOG_LOAD_ID:
			{
			dialog = makeLoadGameDlg();
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
					onAceSuitSelected(suit);
					retVal.dismiss();
				}
			});
		}
		
		return retVal;
	}
	
	private void onAceSuitSelected(int suit)
	{
		stackTop.getCard().setSuit(suit);
		stackTop.postSetImage("");
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
					game.switchTurn();
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
	
	private Dialog makeFinishDlg()
	{
		// @formatter:off

		int p1TotalScore = up.getScore();
		int p2TotalScore = ucp.getScore();
		int p1Wins = up.getWins();
		int p2Wins = ucp.getWins();
		int p1HandScore = game.scoreOf(up.getHand());
		int p2HandScore = game.scoreOf(ucp.getHand());
		String p1Name = up.getName();
		String p2Name = ucp.getName();
		String p1Hand = up.handString();
		String p2Hand = ucp.handString();
		String winnerText;
		
		ScrollView dlgContainer = (ScrollView) inflater.inflate(R.layout.dlg_finish, null);
		LinearLayout dlgContent = (LinearLayout) dlgContainer.findViewById(R.id.dlg_finish_content);
		
		TextView p1name = (TextView) dlgContent.findViewById(R.id.dlg_finish_p1_name);
		p1name.setText(p1Name);
		
		TextView p2name = (TextView) dlgContent.findViewById(R.id.dlg_finish_p2_name);
		p2name.setText(p2Name);
		
		TextView p1name_border = (TextView) dlgContent.findViewById(R.id.dlg_finish_border_p1_name);
		p1name_border.setText(p1Name);
		
		TextView p2name_border = (TextView) dlgContent.findViewById(R.id.dlg_finish_border_p2_name);
		p2name_border.setText(p2Name);
		
		TextView p1hand = (TextView) dlgContent.findViewById(R.id.dlg_finish_p1_hand);
		p1hand.setText(getString(R.string.hand_and_score, p1Hand, p1HandScore, getString(R.string.points)));
		
		TextView p2hand = (TextView) dlgContent.findViewById(R.id.dlg_finish_p2_hand);
		p2hand.setText(getString(R.string.hand_and_score, p2Hand, p2HandScore, getString(R.string.points)));
		
		TextView p1wins = (TextView) dlgContent.findViewById(R.id.dlg_finish_border_p1_wins);
		p1wins.setText(Integer.toString(p1Wins));
		
		TextView p2wins = (TextView) dlgContent.findViewById(R.id.dlg_finish_border_p2_wins);
		p2wins.setText(Integer.toString(p2Wins));
		
		TextView p1points = (TextView) dlgContent.findViewById(R.id.dlg_finish_border_p1_points);
		p1points.setText(Integer.toString(p1TotalScore));
		
		TextView p2points = (TextView) dlgContent.findViewById(R.id.dlg_finish_border_p2_points);
		p2points.setText(Integer.toString(p2TotalScore));

		TextView winner = (TextView) dlgContent.findViewById(R.id.dlg_finish_winner);
		if (p1HandScore > p2HandScore)
		{
			winnerText = p2Name;
		}
		else if (p2HandScore > p1HandScore) 
		{
			winnerText = p1Name;
		}
		else 
		{
			winnerText = getString(R.string.draw);
		}
		winner.setText(getString(R.string.winner, winnerText));
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle(getString(R.string.dlg_finish_title))
		.setView(dlgContainer)
		.setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				if (which == DialogInterface.BUTTON_NEGATIVE) 
				{
					finish();
				}
			}
		})
		.setPositiveButton(getString(R.string.dlg_finish_btn_play_again), new DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				if (which == DialogInterface.BUTTON_POSITIVE) 
				{
					startActivity(getIntent());
					finish();
				}
			}
		})
		.setCancelable(false);
		
		// @formatter:on
		return builder.create();
	}

	private Dialog makeSaveGameDlg()
	{
		// @formatter:off
		DialogInterface.OnClickListener dlgOnClick = new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				switch (which)
				{
				case DialogInterface.BUTTON_POSITIVE:
					// Save game
					{
					try
					{
						FileOutputStream fos = openFileOutput(SAVE_FILE, MODE_PRIVATE);
						
						Editor pref = getSharedPreferences(PREF_SAVE, MODE_PRIVATE).edit();
						pref.putBoolean(KEY_SAVE, true)
						.commit();
						
						saveGame(fos);
						fos.close();
						finish();
					}
					catch (FileNotFoundException ex)
					{
						
					}
					catch (IOException ex)
					{

					}

					}
					break;
				
				case DialogInterface.BUTTON_NEUTRAL:
					// Finish game
					{
					game.isGameFinished = true;
					gameFinished();
					}
					break;
				
				case DialogInterface.BUTTON_NEGATIVE:
					// Exit without save
					{
					finish();
					}
					break;

				default:
					break;
				}
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle(getString(R.string.dlg_save_title))
		.setMessage(getString(R.string.dlg_save_message))
		.setPositiveButton(R.string.dlg_save_btn_save, dlgOnClick)
		.setNegativeButton(R.string.dlg_save_btn_exit, dlgOnClick)
		.setNeutralButton(R.string.dlg_save_btn_finish_game, dlgOnClick)
		.setCancelable(true)
		;
		// @formatter:on
		return builder.create();
	}
	
	private void saveGame(FileOutputStream f) throws IOException
	{
		DataOutputStream dos = new DataOutputStream(f);
		
		List<Card> list = up.getHand();
		list.add(NULL_CARD);
		list.addAll(ucp.getHand());
		list.add(NULL_CARD);
		list.add(stackTop.getCard());
		list.addAll(game.getDeck().cards());
		list.add(NULL_CARD);		
		
		// write to save file
		dos.writeBoolean(game.canDraw(up.getPlayer()));
		for (Card c : list)
		{
			dos.writeInt(c.getSuit());
			dos.writeInt(c.getRank());
		}
	}

	private Dialog makeLoadGameDlg()
	{
		// @formatter:off
		DialogInterface.OnClickListener dlgOnClick = new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				switch (which)
				{
				case DialogInterface.BUTTON_POSITIVE:
					// Load game
					{
					try
					{
						FileInputStream fis = openFileInput(SAVE_FILE);
						loadGame(fis);
						fis.close();
					}
					catch (FileNotFoundException ex)
					{
						
					}
					catch (IOException ex)
					{

					}

					}
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// Start new game
					{
					startActivity(getIntent());
					finish();
					}
					break;

				default:
					break;
				}
				Editor pref = getSharedPreferences(PREF_SAVE, MODE_PRIVATE).edit();
				pref.putBoolean(KEY_SAVE, false)
				.commit();
				deleteFile(SAVE_FILE);
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle(getString(R.string.dlg_load_title))
		.setMessage(getString(R.string.dlg_load_message))
		.setPositiveButton(R.string.dlg_load_btn_load, dlgOnClick)
		.setNegativeButton(R.string.dlg_load_btn_new_game, dlgOnClick)
		.setCancelable(false)
		;
		// @formatter:on
		return builder.create();
	}
	
	private void loadGame(FileInputStream f) throws IOException
	{
		DataInputStream dis = new DataInputStream(f);
		List<Card> p1hand = new ArrayList<Card>();
		List<Card> p2hand = new ArrayList<Card>();
		List<Card> newDeck = new ArrayList<Card>();
		
		game = new Agonia(deck.getDeck(), up.getPlayer(), ucp.getPlayer(), CardGame.DONT_DEAL);
		deck.draw(deck.size());		// clear		
		deck.put(NULL_CARD, false);		// to prevent game finish, cause of empty deck

		boolean canDraw = dis.readBoolean();
		
		Card c = new Card(dis.readInt(), dis.readInt());
		
		// Diavazoume ta hartia pou eihe o player sto heri
		// Uparhei periptosi na min kratouse tipota! (px petakse 8)
		while (!c.equals(NULL_CARD))
		{
			p1hand.add(c);
			c = new Card(dis.readInt(), dis.readInt());
		}
		deck.put(p1hand, false);
		game.draw(up.getPlayer(), p1hand.size());
		
		// Diavazoume ta hartia pou eihe o cpu sto heri
		c = new Card(dis.readInt(), dis.readInt());
		
		do
		{
			p2hand.add(c);
			c = new Card(dis.readInt(), dis.readInt());
		} while (!c.equals(NULL_CARD));
		deck.put(p2hand, false);
		game.draw(ucp.getPlayer(), p2hand.size());
		
		// Diavazoume to stackTop
		c = new Card(dis.readInt(), dis.readInt());
		
		stackTop.setGame(game);
		stackTop.setCardRefTo(game.getTop(0));
		stackTop.setCard(c);
		stackTop.postSetImage("");
		
		// Diavazoume to deck
		c = new Card(dis.readInt(), dis.readInt());
		
		do
		{
			newDeck.add(c);
			c = new Card(dis.readInt(), dis.readInt());
		} while (!c.equals(NULL_CARD));		
		deck.put(newDeck, true);
		deck.cards().remove(NULL_CARD);
		
		if (!canDraw)
		{	// simulate a draw to restore draw state
			game.draw(up.getPlayer(), 0);
			deck.showInfo();
			deck.setImage("btn_paso");
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if (game.turn.equals(ucp.getPlayer()) || game.isGameFinished())
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
					deck.showInfo();
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

	public static void l(String msg) 
	{
		Log.d("dbg", msg);
	}
}
