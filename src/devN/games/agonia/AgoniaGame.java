package devN.games.agonia;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import devN.Test.InfoDisp;
import devN.games.Card;
import devN.games.UICard;
import devN.games.UIDeck;
import devN.games.UIPlayer;

public class AgoniaGame extends Activity
{
	public static final int DIALOG_ACE_ID		= 0;
	public static final int DIALOG_SEVEN_ID		= 1;
	public static final int DIALOG_FINISH_ID	= 2; 	//game finished
	
	public static final int VIEWS_CUSTOM_ID = 0x7fffffff;
	public static int VIEWS_CURRENT_ID = VIEWS_CUSTOM_ID;
	
	InfoDisp i;
	UICard test;

	protected Agonia game;
	protected UIPlayer up;
	protected UIPlayer ucp;
	protected UICard deckBack;
	protected UICard stackTop;
	protected UIDeck deck;
	protected int suits[] = new int[3];
	protected int sevenDraw;
	
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

		TextView tvDeckSize;
		TextView tvPName;
		TextView tvCPName;
		LinearLayout llPlayer;
		LinearLayout llComputer;
		RelativeLayout rlTable;
		AgoniaAI cpuAI;
		String p1Name;
		String p2Name;
		int p1score;
		int p2score;
		
		p1Name = F.getString(F.KEY_P1_NAME, getString(R.string.default_p1_name));
		p1score = F.getInt(F.KEY_P1_SCORE, 0);
		p2Name = F.getString(F.KEY_P2_NAME, getString(R.string.default_p2_name));
		p2score = F.getInt(F.KEY_P2_SCORE, 0);
		
		tvDeckSize = (TextView) findViewById(R.id.deckSize);
		tvPName = (TextView) findViewById(R.id.pName);
		tvCPName = (TextView) findViewById(R.id.cpName);
		llPlayer = (LinearLayout) findViewById(R.id.player);
		llComputer = (LinearLayout) findViewById(R.id.computer);
		rlTable = (RelativeLayout) findViewById(R.id.table);

		deck = new UIDeck(tvDeckSize);

		up = new UIPlayer(p1Name, 1, llPlayer, tvPName, true, this);
		up.addScore(p1score);

		ucp = new UIPlayer(p2Name, 2, llComputer, tvCPName, false, this);
		ucp.addScore(p2score);

		int diffMode = Integer.parseInt(F.getString(getString(R.string.key_difficult), "0"));
		
		l("mode " + diffMode);
		
		switch (diffMode)
		{
		case AgoniaAI.MODE_MONKEY:
			cpuAI = new MonkeyAI(ucp);
			break;
			
		case AgoniaAI.MODE_EASY:
			cpuAI = new EasyAI(ucp);
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
		
		game = new Agonia(deck, up, ucp);

		RelativeLayout.LayoutParams lpDeck, lpTop, lpDeckInfo, lpTest;

		deckBack = new UICard(this, new Card(Card.NULL_CARD), false);
		stackTop = new UICard(this, game.getTop(0), true);
		deckBack.setId(--VIEWS_CURRENT_ID);

		lpTop = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 
		                                        ViewGroup.LayoutParams.WRAP_CONTENT);
		lpTop.addRule(RelativeLayout.CENTER_IN_PARENT);

		lpDeck = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 
		                                         ViewGroup.LayoutParams.WRAP_CONTENT);
		lpDeck.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		lpDeck.addRule(RelativeLayout.CENTER_VERTICAL);

		rlTable.addView(deckBack, lpDeck);
		rlTable.addView(stackTop, lpTop);

		lpDeckInfo = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
		                                             ViewGroup.LayoutParams.WRAP_CONTENT);
		lpDeckInfo.addRule(RelativeLayout.ABOVE, VIEWS_CURRENT_ID);
		lpDeckInfo.addRule(RelativeLayout.ALIGN_LEFT, VIEWS_CURRENT_ID);
		lpDeckInfo.leftMargin = (int) (getResources().getDisplayMetrics().density * 20.0f + 0.5f);
		deck.getTvInfo().setLayoutParams(lpDeckInfo);
		deck.hideInfo();
		
		deckBack.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				if (!game.turn.equals(up))
				{
					return;
				}
				
				if (game.canDraw(up) && deck.size() > 0)
				{
					game.draw(up);
					deck.showInfo();
					deckBack.setImage("btn_paso");
				}
				else
				{
					game.switchTurn();
					cpuAutoPlay();  // -> heirizete moni tis ta exceptions tou CPU
				}
			}

		});

		stackTop.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				/*
				 * nothing to do when clicked
				 */
			}

		});

		i = UICard.getInfoDisp();
		test = new UICard(this, Card.NULL_CARD, true);

		test.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				if (!game.canPlay(i.c.getCard()))
				{
					return;
				}

				try
				{
					game.play(up, i.c.getCard());
					cpuAutoPlay(); // -> heirizete moni tis ta exceptions tou CPU
				}
				catch (AcePlayed e)	// heirizomaste ta exceptions tou User - Player
				{
					showDialog(DIALOG_ACE_ID);
				}
				catch (SevenPlayed e) 
				{
					handleSeven(e.suit);
				}
				catch (GameFinished e) 
				{
					gameFinished();
				}
				finally
				{
					stackTop.setImage();
					test.setVisibility(View.INVISIBLE);
				}
				
			}

		});
		test.setVisibility(View.INVISIBLE);

		lpTest = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
		                           				ViewGroup.LayoutParams.WRAP_CONTENT);
		lpTest.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		lpTest.addRule(RelativeLayout.CENTER_VERTICAL);
		rlTable.addView(test, lpTest);
		i.set(test);
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
			if (game.turn.equals(ucp))
			{
				makeSevenDlg().show();
			}
			else
			{
				game.switchTurn();
				try
				{
					game.play(ucp, ucp.playSeven());
				}
				catch (GameFinished e)
				{  /*
					* nothing to do
					*/
				}
				sevenDraw();
			}
		}
		catch (SevenPlayed e)
		{
			handleSeven(e.suit);
		}
		finally
		{
			stackTop.setImage();
			test.setVisibility(View.INVISIBLE);
		}
	}

	private void sevenDraw()
	{
		game.switchTurn();
		game.turn.draw(sevenDraw);
		
		if (deck.size() == 0)
		{
			deck.showInfo();
			deckBack.setImage("btn_paso");
		}
		else 
		{
			deck.hideInfo();
			deckBack.setImage();
		}
		
		sevenDraw = 0;
		
		if (game.whoPlayNext().getHand().isEmpty())
		{
			gameFinished();
			return;
		}
		
		if (game.turn.equals(ucp)) 
		{
			cpuAutoPlay();
		}
	}
	
	protected void gameFinished() 
	{
		up.addScore(game.scoreOf(ucp.getHand()));
		ucp.addScore(game.scoreOf(up.getHand()));
		saveScores();
		makeFinishDlg().show();
	}

	public void cpuAutoPlay()
	{
		if (deck.size() > 0)
		{
			deck.hideInfo();
			deckBack.setImage();
		}
		
		while (game.turn.equals(ucp))
		{
			if (deck.size() == 0)
			{
				deckBack.setImage("btn_paso");
			}
			
			try
			{
				game.play(ucp, ucp.willPlay());
			}
			catch (AcePlayed e)
			{
				int aceSuit = ucp.getAceSuit();
				
				stackTop.getCard().setRank(1);
				stackTop.getCard().setSuit(aceSuit);
				game.getTop(0).setSuit(aceSuit);
				
				game.switchTurn();
			}
			catch (SevenPlayed e) 
			{
				handleSeven(e.suit);
				return;
			}
			catch (GameFinished e) 
			{
				gameFinished();
			}

			stackTop.setImage();
			test.setVisibility(View.INVISIBLE);
		}
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
					stackTop.getCard().setRank(1);
					stackTop.getCard().setSuit(item);
					game.getTop(0).setSuit(item);
					
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
			
		case DIALOG_FINISH_ID:	// TODO: unused, prepei na heirizomaste tin allagi
			{					//			ton periehomenon tou dialogou, kata to runtime
			dialog = makeFinishDlg();	//	vlepe, TODO sto makeSevenDlg()
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
					recreate();
			//		finish();
			//		startActivity(getIntent());
				}
			}
		})
		.setCancelable(false);
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
		.setNegativeButton(getString(R.string.dlg_seven_btn_draw, sevenDraw), new DialogInterface.OnClickListener(){

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
					game.play(up, new Card(suits[item], 7));
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
}
