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
	boolean isWideScreen = true;
	private int infoId;

	public UIDeck(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.UIInfo);

		final int N = a.getIndexCount();
		for (int i = 0; i < N; i++) 
		{
            int attr = a.getIndex(i);
            switch (attr) 
            {
            case R.styleable.UIInfo_info:
            	infoId = a.getResourceId(attr, -1);
            	break;
            }
        }
		
//		Log.d("dbg", "deck info = " + infoId);
		
		a.recycle();
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
	
	public void setInfo()
	{
		View parent = getRootView();
		
//		Log.d("dbg", "deck par " + parent);
		
		setInfo((TextView) parent.findViewById(infoId));
		
	}

	public boolean isWideScreen()
	{
		return isWideScreen;
	}

	public void setWideScreen(boolean isWideScreen)
	{
		this.isWideScreen = isWideScreen;
	}
	
	public boolean hasCards()
	{
		return deck.hasCards();
	}

	private void refreshInfo()
	{
		tvInfo.post(new Runnable(){

			@Override
			public void run()
			{
				tvInfo.setText("" + size() + "");
			}
		});
	}
	
	public List<Card> cards()
	{
		return deck.cards();
	}
	
	private class InnerDeck extends Deck
	{
		public InnerDeck()
		{
			super();
		}

		@Override
		public List<Card> draw()
		{
			List<Card> c = super.draw();
			refreshInfo();
			return c;
		}

		@Override
		public List<Card> draw(int n)
		{
			List<Card> c = super.draw(n);
			refreshInfo();
			return c;
		}

		@Override
		public List<Card> draw(List<Card> cards)
		{
			List<Card> c = super.draw(cards);
			refreshInfo();
			return c;
		}

		@Override
		public void put(Card c, boolean suffleAfter)
		{
			super.put(c, suffleAfter);
			refreshInfo();
		}

		@Override
		public void put(List<Card> c, boolean suffleAfter)
		{
			super.put(c, suffleAfter);
			refreshInfo();
		}
	}
}