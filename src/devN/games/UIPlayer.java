package devN.games;

import java.util.LinkedList;
import java.util.List;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import devN.etc.dragdrop.DragSource;
import devN.etc.dragdrop.DropTarget;
import devN.games.agonia.AgoniaAI;
import devN.games.agonia.R;

public class UIPlayer extends LinearLayout implements AnimationListener, DropTarget,
											AgoniaAI
{
	@SuppressWarnings("unused")
	private static final String tag = "dbg";
	private Animation animOnRemove = null;
	private Animation animOnAdd = null;
	private List<View> toRemove = new LinkedList<View>();
	private InnerPlayer player = new InnerPlayer();
	private TextView tvInfo;
	private boolean visible;
	private boolean layoutAnimFinisdhed = false; 
	private LayoutParams lpCards = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	private OnTouchListener onTouchListener;
	
	private int infoId;
	
	private static int childWidth;
	private static int parentWidth;
	@SuppressWarnings("unused")
	private static int childHeight;
	@SuppressWarnings("unused")
	private static int parentHeight;
	
	public UIPlayer(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initAttrs(context, attrs);
	}
	
	public void initAttrs(Context context, AttributeSet attrs)
	{
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UICard, 0, 0);
		visible = a.getBoolean(R.styleable.UICard_visible, false);
		a.recycle();

		a = context.obtainStyledAttributes(attrs, R.styleable.UIInfo, 0, 0);
		infoId = a.getResourceId(R.styleable.UIInfo_info, 0);
		if (infoId != 0)
		{
			tvInfo = (TextView) findViewById(infoId);
		}
		a.recycle();
		
		a = context.obtainStyledAttributes(attrs, R.styleable.UIPlayer, 0, 0);
		int team = a.getInt(R.styleable.UIPlayer_team, -1); 
		player.setTeam(team);

		int animAddId = a.getResourceId(R.styleable.UIPlayer_drawCardAnimation, -1);
		int animRemoveId = a.getResourceId(R.styleable.UIPlayer_playCardAnimation, -1);
		try
		{
			animOnAdd = AnimationUtils.loadAnimation(context, animAddId);
		}
		catch (NotFoundException e)
		{
//			d("anim", "onAdd not found! " + animAddId);
		}
		try
		{
			animOnRemove = AnimationUtils.loadAnimation(context, animRemoveId);
			animOnRemove.setAnimationListener(this);
		}
		catch (NotFoundException e)
		{
//			d("anim", "onRemv not found! " + animRemoveId);
		}
		a.recycle();
	}
	
	public static void initDimensions(Context context, int width, int height)
	{
		parentHeight = height;
		parentWidth = width;
		
		UICard c = new UICard(context, new Card(-1, -1), false);
		c.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		childWidth = c.getMeasuredWidth();
		childHeight = c.getMeasuredHeight();
		
//		d(tag, childWidth + " / " + parentWidth);
	}
	
	public void setAnimationsDuration(long durationMillis)
	{
		if (animOnAdd != null)
		{
			animOnAdd.setDuration(durationMillis);
		}
		
		if (animOnRemove != null)
		{
			animOnRemove.setDuration(durationMillis);
		}
	}
	
	private int getRightMarg(int cChilds)
	{
		int marg = 0, width;

		width = cChilds * childWidth - parentWidth;

		if (width > 0 && cChilds > 1)
		{
			marg = -(width) / (cChilds - 1);
		}
		
		return marg - getPaddingRight();
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		
		if (getLayoutAnimation() == null)
		{
			return;
		}
		
		new Runnable(){
			
			@Override
			public void run()
			{
				while (!getLayoutAnimation().isDone())
				{
					try
					{
						Thread.sleep(300);
					}
					catch (InterruptedException ex)
					{

					}
				}
				layoutAnimFinisdhed = true;
			}
		}.run();
	}

	private void addToContainer(List<Card> viewsToAdd)
	{
		boolean shouldAnimate = animOnAdd != null && layoutAnimFinisdhed;
		
		lpCards.rightMargin = getRightMarg(getChildCount() + viewsToAdd.size());
		requestLayout();
		
		for (Card c : viewsToAdd)
		{
			UICard child = new UICard(getContext(), c, visible);
			child.setOnTouchListener(onTouchListener);
			child.setLayoutParams(lpCards);
			addView(child);
			if (shouldAnimate)
			{
				child.startAnimation(animOnAdd);	
			}
		}
	}

	/**
	 * 
	 * @param c
	 *            karta pou tha petaksei
	 * @param success
	 *            an petahtike ap ta heria tou (false simainei NULL_CARD)
	 * @param atMost
	 *            mehri poses kartes idies me ti c tha petaksei
	 * @return
	 */
	private boolean removeFromContainer(Card c, boolean success, int atMost)
	{
		if (!success)
		{
			return false;
		}

		int childs = getChildCount();
		boolean willStartAnimate = toRemove.isEmpty() && animOnRemove != null;
		View v;

		for (int i = 0; i < childs && atMost > 0; i++)
		{
			v = getChildAt(i);
			if (v.getTag().equals(c))
			{
				if (animOnRemove == null)
				{
					removeView(v);
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
			v = toRemove.get(0);
			v.startAnimation(animOnRemove);
		}
		return childs > getChildCount();
	}

	private boolean removeFromContainer(int index, boolean success)
	{
		if (!success)
		{
			return false;
		}

		int childs = getChildCount();
		View v = getChildAt(index);

		if (animOnRemove == null)
		{
			removeView(v);
		}
		else
		{
			toRemove.add(v);
			v.startAnimation(animOnRemove);
		}

		return childs > getChildCount();
	}

	@Override
	public void removeView(View view)
	{
		super.removeView(view);
		
		lpCards.rightMargin = getRightMarg(getChildCount());
		requestLayout();
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
		this.animOnAdd = anim;
	}

	public Animation getOnDrawAnimation()
	{
		return animOnAdd;
	}

	public InnerPlayer getPlayer()
	{
		return player;
	}

	public TextView getInfo()
	{
		return tvInfo;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public void setInfo(TextView tvInfo)
	{
		this.tvInfo = tvInfo;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	private void refreshInfo()
	{
		final String info = player.toString();
		
//		getHandler().post(new Runnable(){
//
//			@Override
//			public void run()
//			{
				tvInfo.setText(info);
//			}
//		});
	}

	public OnTouchListener getOnTouchListener()
	{
		return onTouchListener;
	}

	public void setOnTouchListener(OnTouchListener onTouchListener)
	{
		this.onTouchListener = onTouchListener;
	}
	
	@Override
	public void onAnimationEnd(final Animation animation)
	{
		final View v;
		v = toRemove.remove(0);

		post(new Runnable(){

			@Override
			public void run()
			{
				removeView(v);
			}
		});
		if (!toRemove.isEmpty())
		{
			final View next = toRemove.get(0);
			next.post(new Runnable(){
				
				@Override
				public void run()
				{
					next.postDelayed(new Runnable(){

						@Override
						public void run()
						{
							next.startAnimation(animation);
						}
						
					}, 200);
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

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
						Object dragInfo)
	{
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
							Object dragInfo)
	{
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
							Object dragInfo)
	{

	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
							Object dragInfo)
	{
	
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
								Object dragInfo)
	{
		UICard card = (UICard) dragInfo;
		
		boolean b = player.game.canDraw(player) 
					&& player.isRealPlayer() 
					&& card.getCard().equals(Card.NULL_CARD);
		
		return b;
	}
	
	public List<Card> draw()
	{
		return player.draw();
	}

	public List<Card> draw(int n)
	{
		return player.draw(n);
	}

	public List<Card> draw(List<Card> cards)
	{
		return player.draw(cards);
	}

	public boolean playCard(Card c)
	{
		return player.playCard(c);
	}

	public boolean playCard(int i)
	{
		return player.playCard(i);
	}

	public void addScore(int points)
	{
		player.addScore(points);
	}
	
	public void setName(String name)
	{
		player.setName(name);
	}

	public int getScore()
	{
		return player.getScore();
	}
	
	public void setAI(AgoniaAI ai)
	{
		player.setAI(ai);
	}
	
	@Override
	public Card willPlay()
	{
		return player.willPlay();
	}

	@Override
	public int getAceSuit()
	{
		return player.getAceSuit();
	}

	@Override
	public Card playSeven()
	{
		return player.playSeven();
	}
	
	public List<Card> getHand()
	{
		return player.getHand();
	}
	
	public String getName()
	{
		return player.getName();
	}
	
	public String handString()
	{
		return player.handString();
	}
	
	public int getWins()
	{
		return player.getWins();
	}
	
	public void setWins(int wins)
	{
		player.setWins(wins);
	}
	
	public void win()
	{
		player.win();
	}
		
	private class InnerPlayer extends Player
	{

		public InnerPlayer()
		{
			super();
		}

		@SuppressWarnings("unused")
		public InnerPlayer(String name, int team)
		{
			super(name, team);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see devN.games.Player#draw()
		 */
		@Override
		public List<Card> draw()
		{
			List<Card> drawed = super.draw();
			addToContainer(drawed);
			refreshInfo();

			return drawed;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see devN.games.Player#draw(int)
		 */
		@Override
		public List<Card> draw(int n)
		{
			List<Card> drawed = super.draw(n);
			addToContainer(drawed);
			refreshInfo();

			return drawed;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see devN.games.Player#draw(java.util.List)
		 */
		@Override
		public List<Card> draw(List<Card> cards)
		{
			List<Card> drawed = super.draw(cards);
			addToContainer(drawed);
			refreshInfo();

			return drawed;
		}

		/*
		 * (non-Javadoc)
		 * 
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

		/*
		 * (non-Javadoc)
		 * 
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see devN.games.Player#addScore(int)
		 */
		@Override
		public void addScore(int points)
		{
			super.addScore(points);
			refreshInfo();
		}
	}
}
