package devN.games;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class UIPlayer extends Player implements AnimationListener
{
	private static final String tag = "anim";
	private Animation animOnRemove = null;
	private List<View> toRemove = new ArrayList<View>();
	
	private class HandManager implements OnHierarchyChangeListener
	{
		private int childWidth;
		private int parentWidth;
		private LayoutParams lpCards;
		private OnLongClickListener longClickListener;
		private OnTouchListener touchListener;
		private Animation animOnAdd;

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
			child.setOnLongClickListener(longClickListener);
			child.setOnTouchListener(touchListener);
			final View v = child;
			if (animOnAdd != null)
			{
				child.post(new Runnable(){

					public void run()
					{
						v.startAnimation(animOnAdd);
					}
				});
			}
		}

		@Override
		public void onChildViewRemoved(View parent, View child)
		{
			lpCards.rightMargin = getRightMarg(container.getChildCount() - 1);
			child.setLayoutParams(lpCards);
		}

		public OnLongClickListener getLongClickListener()
		{
			return longClickListener;
		}

		public void setLongClickListener(OnLongClickListener longClickListener)
		{
			this.longClickListener = longClickListener;
		}

		/**
		 * @return the touchListener
		 */
		public OnTouchListener getTouchListener()
		{
			return touchListener;
		}

		/**
		 * @param touchListener the touchListener to set
		 */
		public void setTouchListener(OnTouchListener touchListener)
		{
			this.touchListener = touchListener;
		}

		public Animation getAnimOnAdd()
		{
			return animOnAdd;
		}

		public void setAnimOnAdd(Animation animOnAdd)
		{
			this.animOnAdd = animOnAdd;
		}		
	}
	
	protected Activity act;
	protected ViewGroup container;
	protected TextView tvInfo;
	protected HandManager handManager;
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
		handManager = new HandManager();
		container.setOnHierarchyChangeListener(handManager);
	}

	public UIPlayer(Player p, ViewGroup vg, TextView tv, boolean visible, Activity a)
	{
		this(p.getName(), p.getTeam(), vg, tv, visible, a);
		this.setAI(p.getAI());
	}
	
	private void refreshInfo()
	{
		final String info = toString();
		
		tvInfo.post(new Runnable(){
			
			@Override
			public void run()
			{
				tvInfo.setText(info);
			}
		});
	}

	/* (non-Javadoc)
	 * @see devN.games.Player#draw()
	 */
	@Override
	public void draw()
	{
		super.draw();
		addToContainer(1);
		refreshInfo();
	}

	/* (non-Javadoc)
	 * @see devN.games.Player#draw(int)
	 */
	@Override
	public void draw(int n)
	{
		super.draw(n);
		addToContainer(n);
		refreshInfo();
	}

	/* (non-Javadoc)
	 * @see devN.games.Player#draw(java.util.List)
	 */
	@Override
	public void draw(List<Card> cards)
	{
		super.draw(cards);
		addToContainer(cards.size());
		refreshInfo();
	}

	/* (non-Javadoc)
	 * @see devN.games.Player#playCard(devN.games.Card)
	 */
	@Override
	public boolean playCard(Card c)
	{
		boolean b = super.playCard(c);
		removeFromContainer(c, b, 1);
		refreshInfo();
		
		return b;
	}

	/* (non-Javadoc)
	 * @see devN.games.Player#playCard(int)
	 */
	@Override
	public boolean playCard(int i) 
	{
		boolean b = super.playCard(i);
		removeFromContainer(i, b);
		refreshInfo();

		return b;
	}
	
	private void addToContainer(int n)
	{
		int size = getHand().size();
		final List<Card> hand = getHand();
		
		Log.d(tag, "trying to add " + n + " > " + hand.get(size - n));

		for (int i = size - n; i < size; i++)
		{
			container.addView(new UICard(act, hand.get(i), visible));
			Log.d(tag, "added " + hand.get(i));
		}
	}

	/**
	 * 
	 * @param c karta pou tha petaksei
	 * @param success an petahtike ap ta heria tou
	 * @param atMost mehri poses kartes idies me ti c tha petaksei
	 * @return
	 */
	private boolean removeFromContainer(Card c, boolean success, int atMost)
	{
		if (!success)
		{
			return false; 
		}
		
		int childs = container.getChildCount();
		boolean willStartAnimate = toRemove.isEmpty() && animOnRemove != null; 
		View v;
				
		for (int i = 0; i < childs && atMost > 0; i++)
		{
			v = container.getChildAt(i);
			if (v.getTag().equals(c))
			{
				if (animOnRemove == null)
				{
					container.removeView(v);
				}
				else
				{
					toRemove.add(v);
				}
				atMost--;
			}
		}
		
		if (willStartAnimate)
		{
			Log.d(tag, "tried to remove" + c + " atmost = " + atMost);
			v = toRemove.get(0);
			v.startAnimation(animOnRemove);
		}
		return childs > container.getChildCount();
	}
	
	private boolean removeFromContainer(int index, boolean success)
	{
		if (!success)
		{
			return false; 
		}
		
		int childs = container.getChildCount();
		View v = container.getChildAt(index);
		
		if (animOnRemove == null)
		{
			container.removeView(v);
		}
		else
		{
			toRemove.add(v);
			v.startAnimation(animOnRemove);
		}
		
		return childs > container.getChildCount();
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

	public OnLongClickListener getLongClickListener()
	{
		return handManager.getLongClickListener();
	}

	public void setLongClickListener(OnLongClickListener longClickListener)
	{
		handManager.setLongClickListener(longClickListener);
	}
	
	/**
	 * @return the touchListener
	 */
	public OnTouchListener getTouchListener()
	{
		return handManager.getTouchListener();
	}

	/**
	 * @param touchListener the touchListener to set
	 */
	public void setTouchListener(OnTouchListener touchListener)
	{
		handManager.setTouchListener(touchListener);
	}
	
	public void setOnPlayAnimation(Animation anim)
	{
		this.animOnRemove = anim;
		this.animOnRemove.setAnimationListener(this);
	}
	
	public Animation getOnPlayAnimation()
	{
		return animOnRemove;
	}
	
	public void setOnDrawAnimation(Animation anim)
	{
		handManager.setAnimOnAdd(anim);
	}
	
	public Animation getOnDrawAnimation()
	{
		return handManager.getAnimOnAdd();
	}

	@Override
	public void onAnimationEnd(Animation animation)
	{
		final View v;
		v = toRemove.remove(0);

		container.post(new Runnable(){

			@Override
			public void run()
			{
				v.clearAnimation();
				container.removeView(v);
				Log.d(tag, "removed " + v.getTag());
			}
		});
		if (!toRemove.isEmpty())
		{
			final View next = toRemove.get(0);
			next.post(new Runnable(){
				public void run()
				{					
					next.startAnimation(animOnRemove);
				}
			});
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation)
	{
		
	}

	@Override
	public void onAnimationStart(Animation animation)
	{
		
	}
}
