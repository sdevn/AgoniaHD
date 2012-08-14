package devN.games.agonia;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import devN.etc.dragdrop.DragController;
import devN.etc.dragdrop.DragSource;
import devN.games.Card;
import devN.games.Player;
import devN.games.UICard;
import devN.games.UIDeck;
import devN.games.UIPlayer;
import devN.games.UIStackTop;

public class AgoniaGame extends Activity implements DragSource, OnTouchListener
{
	public static final int DIALOG_ACE_ID		= 0;
	public static final int DIALOG_SEVEN_ID		= 1;
	public static final int DIALOG_FINISH_ID	= 2; 	//game finished
	
	protected Agonia game;
	protected UIPlayer up;
	protected UIPlayer ucp;
	protected UICard draggedCard;
	protected UIStackTop stackTop;
	protected UIDeck deck;
	protected int suits[] = new int[3];
	protected int sevenDraw;
	protected long cpuDelay;
	protected boolean dragging = false;
	private DragController dragController;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
       	setContentView(R.layout.game);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart()
	{
		super.onStart();

		AgoniaAI cpuAI;
		String p1Name;
		String p2Name;
		int p1score;
		int p2score;
		TextView upInfo;
		TextView ucpInfo;
		TextView deckInfo;
		Animation animCpuPlay;
		Animation animCpuDraw;
		boolean isWideScreen;
		
		Configuration conf = getResources().getConfiguration();
		isWideScreen = Configuration.SCREENLAYOUT_LONG_YES 
				== (conf.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK);
		
		cpuDelay = 1500;	//TODO: initialize it from a preferences
		p1Name = F.getString(F.KEY_P1_NAME, getString(R.string.default_p1_name));
		p1score = F.getInt(F.KEY_P1_SCORE, 0);
		p2Name = F.getString(F.KEY_P2_NAME, getString(R.string.default_p2_name));
		p2score = F.getInt(F.KEY_P2_SCORE, 0);

		upInfo = (TextView) findViewById(R.id.pName);
		ucpInfo = (TextView) findViewById(R.id.cpName);
		deckInfo = (TextView) findViewById(R.id.deckSize);
		
		up = (UIPlayer) findViewById(R.id.player);
		up.setInfo(upInfo);
		up.setName(p1Name);
		up.addScore(p1score);
		up.setOnTouchListener(this);
		
		ucp = (UIPlayer) findViewById(R.id.computer);
		ucp.setInfo(ucpInfo);
		ucp.setName(p2Name);
		ucp.addScore(p2score);
		ucp.setOnTouchListener(this);

		dragController = (DragController) findViewById(R.id.dragLayer);
		
		stackTop = (UIStackTop) findViewById(R.id.stackTop);
		
		deck = (UIDeck) findViewById(R.id.deck);
		deck.setInfo(deckInfo);
		deck.setWideScreen(isWideScreen);
		deck.setOnTouchListener(this);
		
		animCpuPlay = AnimationUtils.loadAnimation(this, R.anim.cpu_play);
		animCpuDraw = AnimationUtils.loadAnimation(this, R.anim.cpu_draw);

		animCpuDraw.setDuration(cpuDelay);
		animCpuPlay.setDuration(cpuDelay);

		ucp.setOnPlayAnimation(animCpuPlay);
		ucp.setOnDrawAnimation(animCpuDraw);
		
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		UIPlayer.initDimensions(this, displayMetrics.widthPixels, displayMetrics.heightPixels);
		
		int diffMode = Integer.parseInt(F.getString(getString(R.string.key_difficult), "0"));
		
		l("mode " + diffMode);
		
		switch (diffMode)
		{
		case AgoniaAI.MODE_MONKEY:
			cpuAI = new MonkeyAI(ucp.getPlayer());
			break;
			
		case AgoniaAI.MODE_EASY:
			cpuAI = new EasyAI(ucp.getPlayer());
			break;
			
		case AgoniaAI.MODE_MODERATE:
			throw new RuntimeException("MODE_MODERATE Not implemented yet!");
//			break;
				
		case AgoniaAI.MODE_MEGAMIND:
			throw new RuntimeException("MODE_MEGAMIND Not implemented yet!");
//			break;
			
		default:
			throw new RuntimeException("Uknown difficult mode");
//			break;
		}
		ucp.setAI(cpuAI);
		
		game = new Agonia(deck.getDeck(), up.getPlayer(), ucp.getPlayer());
		
		stackTop.setGame(game);
		stackTop.setCardRefTo(game.getTop(0));
		
		deck.hideInfo();

		deck.setOnLongClickListener(new OnLongClickListener(){

			@Override
			public boolean onLongClick(View v)
			{
				if (!game.turn.equals(up.getPlayer()))
				{
					return false;
				}
				
				if (game.canDraw(up.getPlayer()) && deck.size() > 0)
				{
					return false;
				}
				else
				{
					game.switchTurn();
					cpuAutoPlay();  // -> heirizete moni tis ta exceptions tou CPU
					return true;
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed()
	{
		// TODO save game
		super.onBackPressed();
	}

	private void saveScores()
	{
		F.edit()
		.putInt(F.KEY_P1_SCORE, up.getScore())
		.putInt(F.KEY_P2_SCORE, ucp.getScore())
		.commit();
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
						makeSevenDlg().show();
					}
				}, cpuDelay + 200);
			}
			else
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
		game.turn.draw(sevenDraw);
		
		if (deck.size() == 0)
		{
			deck.showInfo();
			deck.setImage("btn_paso");
		}
		else 
		{
			deck.hideInfo();
			deck.setImage();
		}
		
		sevenDraw = 0;
		
		if (game.whoPlayNext().getHand().isEmpty())
		{
			gameFinished();
			return;
		}
		
		if (game.turn.equals(ucp.getPlayer())) 
		{
			cpuAutoPlay();
		}
	}
	
	protected void gameFinished() 
	{
		up.addScore(game.scoreOf(ucp.getHand()));
		ucp.addScore(game.scoreOf(up.getHand()));
		saveScores();
		new Handler().postDelayed(new Runnable(){
			public void run()
			{
				showDialog(DIALOG_FINISH_ID);
			}
		}, cpuDelay * 2);
	}
	
	public void playerPlay(Player p, Card c)
	{
		try
		{
			game.play(p, c);
			stackTop.setImage();
			cpuAutoPlay(); // -> heirizete moni tis ta exceptions tou CPU
		}
		catch (AcePlayed e) // heirizomaste ta exceptions tou User - Player
		{
			showDialog(DIALOG_ACE_ID);
			stackTop.setImage();
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
		if (deck.size() > 0)
		{
			deck.hideInfo();
			deck.postSetImage("");
		}
		
		new Handler().postDelayed(new Runnable(){
			public void run()
			{
				if (game.turn.equals(ucp.getPlayer()))
				{
					if (deck.size() == 0)
					{
						deck.postSetImage("btn_paso");
					}

					try
					{
						Card choosed = ucp.willPlay();
						
						if (choosed.equals(Card.NULL_CARD) && game.canDraw(ucp.getPlayer()))
						{
							game.draw(ucp.getPlayer());
						}
						else 
						{
							game.play(ucp.getPlayer(), choosed);
						}
						if (game.turn.equals(ucp.getPlayer()))
						{
							// gia na emfanizonte ola ta fila otan paizei sunehomenes fores
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
			}
		}, cpuDelay + 200);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id)
	{
		Dialog dialog = null;

		switch (id)
		{
		case DIALOG_ACE_ID:
			{
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setTitle(getString(R.string.dlg_ace_title))
			.setCancelable(false)
			.setItems(Card.pszSUITS, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int item)
				{
					stackTop.getCard().setSuit(item);
					stackTop.postSetImage("");
					game.switchTurn();
					cpuAutoPlay();
				}
			})
			;
			dialog = builder.create();
			
			}
			break;
												
		case DIALOG_SEVEN_ID: 	// TODO: unused, epeidi uparhei bug sto opoio otan 
			{					//			petiete cpu(7)->player(7)->cpu(7)
			dialog = makeSevenDlg();	//	o deuteros dialogos den emfanizete
			}
			break;
			
		case DIALOG_FINISH_ID:
			{					
			dialog = makeFinishDlg();
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
	
	private Dialog makeFinishDlg()
	{
		//TODO: replace message with a linearlayout(vertical)
		//			.add {TextView(up.getName(), up.handString(), game.scoreOf(up.getHand())}
		//			.add {View(R.drawable.numberpicker_selection_divider)}
		//			.add {TextView(ucp.getName(), ucp.handString(), game.scoreOf(ucp.getHand())}
		
		String message = String.format("%s: { %s} = %d points\n\n%s: { %s} = %d points", 
								up.getName(), up.handString(), game.scoreOf(up.getHand()),
								ucp.getName(), ucp.handString(), game.scoreOf(ucp.getHand()));
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle(getString(R.string.dlg_finish_title))
		.setMessage(message)	//TODO: replace with .addView, vlepe todo pio pano
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
		.setOnCancelListener(new DialogInterface.OnCancelListener(){
			
			@Override
			public void onCancel(DialogInterface dialog)
			{
				finish();
			}
		});
		return builder.create();
	}

	private Dialog makeSevenDlg() 
	{
		List<CharSequence> sevens = new ArrayList<CharSequence>();
		int i = 0;	
		
		for (Card c : up.getHand()) 
		{
			if (c.getRank() == 7) 
			{
				suits[i++] = c.getSuit();
				sevens.add(c.toString());
			}
		}
		
		CharSequence[] css = new CharSequence[sevens.size()];
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle(getString(R.string.dlg_seven_title))
		.setNegativeButton(getString(R.string.dlg_seven_btn_draw, sevenDraw), 
										new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int item)
			{
				if (item == DialogInterface.BUTTON_NEGATIVE) 
				{
					sevenDraw();
					return;
				}
			}
		})
		.setCancelable(true)
		.setItems(sevens.toArray(css), new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int item)
			{
				try 
				{
					game.play(up.getPlayer(), new Card(suits[item], 7));
				} 
				catch (SevenPlayed e) 
				{
					game.switchTurn();
					handleSeven(e.suit);
				}
			}
		});
//		
//		AlertDialog alertDialog = builder.create();
//		
//		alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
//
//			@Override
//			public void onDismiss(DialogInterface dialog)
//			{
//				// TODO: replace it with trick from
//				// http://stackoverflow.com/questions/4811688/how-to-set-contents-of-setsinglechoiceitems-in-onpreparedialog
//				removeDialog(DIALOG_SEVEN_ID);		
//			}
//			
//		});
		
		return builder.create();
	}
	
	public static void l(String msg) 
	{
		Log.d("dbg", msg);
	}

	@Override
	public void onDropCompleted(View target, boolean success)
	{
		if (success)
		{
			if (target.equals(up))
			{
				game.draw(up.getPlayer());
				deck.showInfo();
				deck.setImage("btn_paso");
			}
			else 
			{
				playerPlay(up.getPlayer(), draggedCard.getCard());
			}			
		}
		else 
		{
			l(draggedCard.getCard() + " is not accepted!");
		}
		dragging = false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if (game.turn.equals(ucp.getPlayer()))
		{
			return false;
		}		

		switch (event.getAction())
		{
		case MotionEvent.ACTION_MOVE:
			break;

		case MotionEvent.ACTION_DOWN:
			if (dragging)
			{
				return false;
			}

			if (v.equals(deck) && !game.canDraw(up.getPlayer()))
			{
				return false;
			}
			
			if (!(v instanceof UICard) || v instanceof UIStackTop)
			{
				return false;
			}
			draggedCard = (UICard) v;
			
			if (!draggedCard.isVisible() && !draggedCard.getCard().equals(Card.NULL_CARD))
			{
				return false;
			}
//			l("im touch-dragging " + cardToPlay.getCard());
			dragging = true;
			dragController.startDrag(v, this, v, DragController.DRAG_ACTION_MOVE);
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
}
