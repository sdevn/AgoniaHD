package devN.games;

import devN.games.agonia.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;

public class UICard extends ImageButton
{
	private final static String tag = "dbg";

	protected Card card;
	protected boolean visible;
	
	public UICard(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);	
	}

	public UICard(Context context, AttributeSet attrs)
	{
		super(context, attrs);
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
	}
	
	public UICard(Context context, Card c, boolean visible)
	{
		super(context);
		this.visible = visible;
		this.card = c;

		setImage();

		this.setBackgroundColor(Color.TRANSPARENT);
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		this.setLayoutParams(lp);
	}
	
	public UICard(UICard c)
	{
		this(c.getContext(), new Card(c.card), c.visible);
	}

	public Card getCard()
	{
		return card;
	}

	public void setCard(Card card)
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
		
		setImage();
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
			img = "card_back_blue_1";
		}

		setImage(img);
	}

	public void setImage(String resName)
	{
		int id;
	
		id = getResources().getIdentifier(resName, "drawable", getContext().getPackageName());
		
		if (id != 0)
		{
			super.setImageResource(id);
			requestLayout();
		}
		
		setTag(card);
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
}
