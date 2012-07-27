package devN.games;

import java.util.List;
import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.view.ViewGroup.OnHierarchyChangeListener;

public class UIPlayer extends Player
{
	private class HandManager implements OnHierarchyChangeListener
	{
		private int childWidth;
		private int parentWidth;
		private LayoutParams lpCards;
		
		public HandManager()
		{
			DisplayMetrics metrics = new DisplayMetrics(); 
			act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			parentWidth = metrics.widthPixels;
			
			UICard c = new UICard(act, new Card(-1, -1), false);
			c.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			childWidth = c.getMeasuredWidth();
			
			lpCards = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}

		private int getRightMarg(int cChilds)
		{
			int marg = 0, 
				width; 

			width = cChilds * childWidth - parentWidth;
			
			if (width > 0 && cChilds > 1)
			{
				marg = -(width) / (cChilds - 1);
			}

			return marg;
		}

		@Override
		public void onChildViewAdded(View parent, View child)
		{
			lpCards.rightMargin = getRightMarg(container.getChildCount());
			child.setLayoutParams(lpCards);			
		}

		@Override
		public void onChildViewRemoved(View parent, View child)
		{
			lpCards.rightMargin = getRightMarg(container.getChildCount() - 1);
			child.setLayoutParams(lpCards);	
		}
		
	}
	
	protected Activity act;
	protected ViewGroup container;
	protected TextView tvInfo;
	protected boolean visible;
	
	public UIPlayer(String name, int team, ViewGroup vg, TextView tv, boolean visible)
	{
		super(name, team);
		this.visible = visible;
		container = vg; 
		tvInfo = tv;
	}
	
	public UIPlayer(String name, int team, ViewGroup vg, TextView tv, boolean visible, Activity a)
	{
		this(name, team, vg, tv, visible);
		act = a;
		container.setOnHierarchyChangeListener(new HandManager());
	}

	public UIPlayer(Player p, ViewGroup vg, TextView tv, boolean visible, Activity a)
	{
		this(p.getName(), p.getTeam(), vg, tv, visible, a);
		this.setAI(p.getAI());
	}
	
	private void refreshInfo()
	{
		tvInfo.setText(toString());
	}
	
	private void refreshHand()
	{
		container.removeAllViews();
		
		for (Card c : getHand())
		{
			container.addView(new UICard(act, c, visible));
		}
		refreshInfo();
	}

	/* (non-Javadoc)
	 * @see devN.games.Player#draw()
	 */
	@Override
	public void draw()
	{
		super.draw();
		refreshHand();
	}

	/* (non-Javadoc)
	 * @see devN.games.Player#draw(int)
	 */
	@Override
	public void draw(int n)
	{
		super.draw(n);
		refreshHand();
	}

	/* (non-Javadoc)
	 * @see devN.games.Player#draw(java.util.List)
	 */
	@Override
	public void draw(List<Card> cards)
	{
		super.draw(cards);
		refreshHand();
	}

	/* (non-Javadoc)
	 * @see devN.games.Player#playCard(devN.games.Card)
	 */
	@Override
	public boolean playCard(Card c)
	{
		boolean b = super.playCard(c);
		refreshHand();
		
		return b;
	}

	/* (non-Javadoc)
	 * @see devN.games.Player#playCard(int)
	 */
	@Override
	public boolean playCard(int i) 
	{
		boolean b = super.playCard(i);
		refreshHand();
		
		return b;
	}

	/* (non-Javadoc)
	 * @see devN.games.Player#addScore(int)
	 */
	@Override
	public void addScore(int points)
	{
		super.addScore(points);
		refreshInfo();
	}

}
