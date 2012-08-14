package devN.games;

import java.util.List;
import devN.games.agonia.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class UIDeck extends UICard
{
	private TextView tvInfo;
	private InnerDeck deck = new InnerDeck();
	boolean isWideScreen = false;
	private int infoId;

	public UIDeck(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		TypedArray taInfo = context.obtainStyledAttributes(attrs, R.styleable.UIInfo);
		infoId = taInfo.getResourceId(R.styleable.UIInfo_info, 0);
		if (infoId != 0)
		{
//			tvInfo = (TextView) findViewById(infoId);
			taInfo.recycle();
		}
	}

	public UIDeck(Context context, TextView info)
	{
		super(context, new Card(Card.NULL_CARD), false);
	}

	public List<Card> draw()
	{
		return deck.draw();
	}

	public List<Card> draw(int n)
	{
		return deck.draw(n);
	}

	public List<Card> draw(List<Card> cards)
	{
		return deck.draw(cards);
	}

	public void put(Card c, boolean suffleAfter)
	{
		deck.put(c, suffleAfter);
	}

	public void put(List<Card> c, boolean suffleAfter)
	{
		deck.put(c, suffleAfter);
	}
	
	public int size()
	{
		return deck.size();
	}
		
	public void hideInfo()
	{
		if (!isWideScreen)
		{
			tvInfo.setVisibility(View.INVISIBLE);
		}
	}

	public void showInfo()
	{
		tvInfo.setVisibility(View.VISIBLE);
	}
	
	public InnerDeck getDeck()
	{
		return deck;
	}

	public void setDeck(InnerDeck deck)
	{
		this.deck = deck;
	}

	public TextView getInfo()
	{
		return tvInfo;
	}

	public void setInfo(TextView tvInfo)
	{
		this.tvInfo = tvInfo;
	}

	public boolean isWideScreen()
	{
		return isWideScreen;
	}

	public void setWideScreen(boolean isWideScreen)
	{
		this.isWideScreen = isWideScreen;
	}

	private void refreshInfo()
	{
		tvInfo.post(new Runnable(){

			@Override
			public void run()
			{
				tvInfo.setText(Integer.toString(size()));
			}
		});
	}
	
	private class InnerDeck extends Deck
	{
		public InnerDeck()
		{
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see devN.games.Deck#draw()
		 */
		@Override
		public List<Card> draw()
		{
			List<Card> c = super.draw();
			refreshInfo();
			return c;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see devN.games.Deck#draw(int)
		 */
		@Override
		public List<Card> draw(int n)
		{
			List<Card> c = super.draw(n);
			refreshInfo();
			return c;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see devN.games.Deck#draw(java.util.List)
		 */
		@Override
		public List<Card> draw(List<Card> cards)
		{
			List<Card> c = super.draw(cards);
			refreshInfo();
			return c;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see devN.games.Deck#put(devN.games.Card, boolean)
		 */
		@Override
		public void put(Card c, boolean suffleAfter)
		{
			super.put(c, suffleAfter);
			refreshInfo();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see devN.games.Deck#put(java.util.List, boolean)
		 */
		@Override
		public void put(List<Card> c, boolean suffleAfter)
		{
			super.put(c, suffleAfter);
			refreshInfo();
		}
	}
}
