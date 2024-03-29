package devN.games;

import devN.games.agonia.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class UICard extends ImageButton
{
	/**
	 * v2.1 added
	 */
	private static boolean useOldDeck;
	
	protected Card card;
	protected boolean visible;

	public UICard(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initAttrs(context, attrs);
	}

	public UICard(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initAttrs(context, attrs);
	}

	/* v2.1 added */
	private void initAttrs(Context context, AttributeSet attrs)
	{
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UICard);
		visible = a.getBoolean(R.styleable.UICard_visible, false);
		int suit, rank;
		
		suit = a.getInt(R.styleable.UICard_cardSuit, -1);
		rank = a.getInt(R.styleable.UICard_cardRank, -1);
		
		card = new Card(suit, rank);
		
		setImage();
		a.recycle();
	}
	
	public UICard(Context context)
	{
		super(context);
		setScaleType(ScaleType.CENTER);
	}
	
//	public UICard(Context context, Card c, boolean visible)
//	{
//		this(context);
//		this.visible = visible;
//		this.card = c;
//
//		setImage();
//		
////		this.setBackgroundColor(Color.TRANSPARENT);
////
////		setScaleType(ScaleType.CENTER_INSIDE);
////
////		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, 
////				getResources().getDimensionPixelSize(R.dimen.UICard_height));
////		
////		this.setLayoutParams(lp);
//	}
//	
//	public UICard(UICard c)
//	{
//		this(c.getContext(), new Card(c.card), c.visible);
//	}

	public Card getCard()
	{
		return card;
	}

	public void setCard(Card card)
	{
		setCard(card, true);
	}
	
	public void setCard(Card card, boolean refreshImage)
	{
		if (this.card != null)
		{
			this.card.setRank(card.getRank());
			this.card.setSuit(card.getSuit());
		}
		else
		{
			this.card = card;
		}
		
		if (refreshImage)
		{
			setImage();
		}
	}
	
	public void setCardRefTo(Card card)
	{
		this.card = null;
		setCard(card);
	}

	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	public void setImage()
	{
		String img;
		
		if (visible)
		{
			img = "card_" + card.getSuit() + "_" + card.getRank();
		}
		else
		{
			img = "card_back";
		}

		setImage(img);
	}

	public void setImage(String resName)
	{
		int id;
		String name = resName;
		
		if (useOldDeck && resName.startsWith("card"))
		{
			name = "old_" + name;
		}
		
		id = getResources().getIdentifier(name, "drawable", getContext().getPackageName());
		
		if (id != 0)
		{
			super.setImageResource(id);
			
			requestLayout();
			invalidate();
		}
	}
	
	public void postSetImage(final String resName)
	{
		if (resName.length() == 0)
		{
			post(new Runnable(){
				
				@Override
				public void run()
				{
					setImage();
				}
			});
		}
		else 
		{
			post(new Runnable(){
				
				@Override
				public void run()
				{
					setImage(resName);
				}
			});
		}
	}
	
	public void postSetImage(final String resName, long delay)
	{
		if (resName.length() == 0)
		{
			postDelayed(new Runnable(){
				
				@Override
				public void run()
				{
					setImage();
				}
			}, delay);
		}
		else 
		{
			postDelayed(new Runnable(){
				
				@Override
				public void run()
				{
					setImage(resName);
				}
			}, delay);
		}
	}

	public static boolean isUseOldDeck()
	{
		return useOldDeck;
	}

	public static void setUseOldDeck(boolean useOldDeck)
	{
		UICard.useOldDeck = useOldDeck;
	}
}
