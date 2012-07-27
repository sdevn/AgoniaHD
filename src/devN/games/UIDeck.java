package devN.games;

import java.util.List;
import android.view.View;
import android.widget.TextView;

public class UIDeck extends Deck
{
	protected TextView tvInfo;

	public UIDeck(TextView tv)
	{
		super();
		this.tvInfo = tv;
	}
	
	private void refreshInfo()
	{
		tvInfo.setText(Integer.toString(super.size()));
	}

	/* (non-Javadoc)
	 * @see devN.games.Deck#draw()
	 */
	@Override
	public List<Card> draw()
	{
		List<Card> c = super.draw();
		refreshInfo();
		
		return c;
	}

	/* (non-Javadoc)
	 * @see devN.games.Deck#draw(int)
	 */
	@Override
	public List<Card> draw(int n)
	{
		List<Card> c = super.draw(n);
		refreshInfo();
		
		return c;
	}

	/* (non-Javadoc)
	 * @see devN.games.Deck#draw(java.util.List)
	 */
	@Override
	public List<Card> draw(List<Card> cards)
	{
		List<Card> c = super.draw(cards);
		refreshInfo();
		
		return c;
	}

	/* (non-Javadoc)
	 * @see devN.games.Deck#put(devN.games.Card, boolean)
	 */
	@Override
	public void put(Card c, boolean suffleAfter)
	{
		super.put(c, suffleAfter);
		refreshInfo();
	}

	/* (non-Javadoc)
	 * @see devN.games.Deck#put(java.util.List, boolean)
	 */
	@Override
	public void put(List<Card> c, boolean suffleAfter)
	{
		super.put(c, suffleAfter);
		refreshInfo();
	}

	/**
	 * @return the tvInfo
	 */
	public TextView getTvInfo()
	{
		return tvInfo;
	}

	public void hideInfo()
	{
		tvInfo.setVisibility(View.INVISIBLE);
	}

	public void showInfo()
	{
		tvInfo.setVisibility(View.VISIBLE);
	}

}
