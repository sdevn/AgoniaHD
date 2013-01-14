package devN.games;

import java.util.LinkedList;
import java.util.List;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
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
	
	private boolean bAnimOnRemove;
	private boolean bAnimOnAdd;
	private Animation animOnRemove;
	private Animation animOnAdd;
	
	private List<View> toRemove = new LinkedList<View>();
	private InnerPlayer player = new InnerPlayer();
	private TextView tvInfo;
	private boolean visible;
	private boolean layoutAnimFinisdhed = false; 
	private LayoutParams lpCards = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	private OnTouchListener onTouchListener;
	
	private int infoId;

	private int childWidth;		// UICard dimensions
	private int childHeight;
	
	private int parentWidth;	// this deminsions
	private int parentHeight;
	
	private int parentFullWidth;	// screen dimensions
	@SuppressWarnings("unused")
	private int parentFullHeight;
	
	private int animDeltaY;
	private int animDeltaX;
	private long animDuration;
	
	public UIPlayer(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs);
		initAttrs(context, attrs);
	}

	public UIPlayer(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}
	
	public void initAttrs(Context context, AttributeSet attrs)
	{
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.UICard, 0, 0);
		visible = a.getBoolean(R.styleable.UICard_visible, false);
		a.recycle();

		a = context.obtainStyledAttributes(attrs, R.styleable.UIInfo, 0, 0);
		
		final int N = a.getIndexCount();
		for (int i = 0; i < N; i++) 
		{
            int attr = a.getIndex(i);
            switch (attr) 
            {
            case R.styleable.UIInfo_info:
            	infoId = a.getResourceId(attr, -1);
            	if (infoId != -1 && !isInEditMode())
        		{
//        			Log.d("dbg", getName() + " info = " + infoId);
        		}
            	break;
            }
        }
		
		a.recycle();
		
		a = getContext().obtainStyledAttributes(attrs, R.styleable.UIPlayer, 0, 0);
		int team = a.getInt(R.styleable.UIPlayer_team, -1); 
		player.setTeam(team);

		int animAddId = a.getResourceId(R.styleable.UIPlayer_drawCardAnimation, R.anim.cpu_draw);
		bAnimOnAdd = a.getBoolean(R.styleable.UIPlayer_drawCardAnimation, true);
		
		int animRemoveId = a.getResourceId(R.styleable.UIPlayer_playCardAnimation, R.anim.cpu_play);
		bAnimOnRemove = a.getBoolean(R.styleable.UIPlayer_playCardAnimation, true);
		
		try
		{
			if (bAnimOnAdd && !isInEditMode())
			{
				animOnAdd = AnimationUtils.loadAnimation(context, animAddId);
			}
		}
		catch (NotFoundException e)
		{
			Log.d("anim", player + "onAdd not found! " + animAddId);
		}
		try
		{
			if (bAnimOnRemove && !isInEditMode())
			{
				animOnRemove = AnimationUtils.loadAnimation(context, animRemoveId);
				animOnRemove.setAnimationListener(this);
			}
		}
		catch (NotFoundException e)
		{
			Log.d("anim", player + "onRemv not found! " + animRemoveId);
		}
		a.recycle();
	}
	
	public void initDimensions(int width, int height, int fullWidth, int fullHeight)
	{
		parentHeight = height;
		parentWidth = width;
		
											// ean o player minei horis kartes alla den bgei
		getLayoutParams().height = height;	// (px petakse 8), to height epeidi einai WRAP_CONTENT
											// midenizete, me apotelesma na min mporei na kanei draw

		parentFullHeight = fullHeight;
		parentFullWidth = fullWidth;

		UICard c = new UICard(getContext(), new Card(-1, -1), false);
		c.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		childWidth = c.getMeasuredWidth();
		childHeight = c.getMeasuredHeight();
		
		animDeltaY = parentHeight + (parentHeight - childHeight) / 2;
	}
	
	private int calcPlayDeltaX(UICard c)
	{
		int delta;
		int center = (parentFullWidth - childWidth) / 2;
		int[] aChildCoords = new int[2];
		
		c.getLocationOnScreen(aChildCoords);
		
		delta = (int) (center - aChildCoords[0]);
		
		return delta;
	}
	
	private Animation getNewPlayAnim()
	{
		TranslateAnimation anim;
		
		anim = new TranslateAnimation(0, animDeltaX, 0, animDeltaY);
		
		anim.setDuration(animDuration);
		anim.setAnimationListener(this);
		
		return anim;
	}
	
	private int calcDrawDeltaX(UICard c)
	{
		int delta;
		int rightmostEdge;
		int[] aParLocation = new int[2];
		
		getLocationOnScreen(aParLocation);
		
		rightmostEdge = getChildCount() * childWidth + aParLocation[0];
		
		if (parentWidth < rightmostEdge)
		{
			rightmostEdge = aParLocation[0] + parentWidth;
		}
		
		delta = -(rightmostEdge - childWidth);
		
//		Log.d("anim", player.getName() + " aW " + rightmostEdge);
		
		return delta;
	}
	
	private Animation getNewDrawAnim()
	{
		TranslateAnimation anim;
		
		anim = new TranslateAnimation(animDeltaX, 0, animDeltaY, 0);
		
		anim.setDuration(animDuration);
		
		return anim;
	}
	
	public void setAnimationsDuration(long durationMillis)
	{
		animDuration = durationMillis;
		
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

	private void addToContainer(List<Card> viewsToAdd)
	{
		boolean shouldAnimate = bAnimOnAdd && layoutAnimFinisdhed;
		
		lpCards.rightMargin = getRightMarg(getChildCount() + viewsToAdd.size());
		requestLayout();
		int i = viewsToAdd.size();

		for (Card c : viewsToAdd)
		{
			UICard child = new UICard(getContext(), c, visible);
			child.setOnTouchListener(onTouchListener);
			child.setLayoutParams(lpCards);
			addView(child);
			
			if (shouldAnimate)
			{
				i--;
				animDeltaX = calcDrawDeltaX(child);
				animOnAdd = getNewDrawAnim();
				animOnAdd.setDuration(animDuration - i * 100);
				child.startAnimation(animOnAdd);	
			}
		}
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();

		if (getLayoutAnimation() == null)
		{
			layoutAnimFinisdhed = true;
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
		
		View v;

		for (int i = 0; i < childs && atMost > 0; i++)
		{
			v = getChildAt(i);
			if (v.getTag().equals(c))
			{
				animDeltaX = calcPlayDeltaX((UICard) v);
				
				if (bAnimOnRemove)
				{
					animOnRemove = getNewPlayAnim();
					((UICard) v).setVisible(true);
					((UICard) v).postSetImage("");
				}
				
				removeFromContainer(i, success);
				atMost--;
			}
		}

		return childs > getChildCount();
	}

	private boolean removeFromContainer(int index, boolean success)
	{
		if (!success)
		{
			return false;
		}
		
		boolean willStartAnimate = toRemove.isEmpty() && animOnRemove != null;
		int childs = getChildCount();
		View v = getChildAt(index);

		if (animOnRemove == null)
		{
			removeView(v);
		}
		else
		{
			toRemove.add(v);
		}
		
		if (willStartAnimate)
		{
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
	
	public void setInfo()
	{
		View parent = getRootView();
		
//		Log.d("dbg", getName() + " par " + parent);
		
		setInfo((TextView) parent.findViewById(infoId));
		
	}

	@Override
	public void setVisibility(int visibility)
	{
		tvInfo.setVisibility(visibility);
		super.setVisibility(visibility);
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	private void refreshInfo()
	{
		final String info = player.toString();
		Handler h = getHandler();
		
		if (h == null)
		{
//			Log.d(tag, getName() + " my handler sucks!! :@");
			tvInfo.setText(info);
		}
		else 
		{
			h.post(new Runnable(){
	
				@Override
				public void run()
				{
					tvInfo.setText(info);
				}
			});
		}
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
	{ }

	@Override
	public void onAnimationStart(Animation animation)
	{ }

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
						Object dragInfo) {}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
							Object dragInfo) { }

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
							Object dragInfo) { }

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
							Object dragInfo) { }

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
	
	public void setTeam(int team)
	{
		player.setTeam(team);
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
	
	public int getTeam()
	{
		return player.getTeam();
	}
	
	public void setWins(int wins)
	{
		player.setWins(wins);
	}
	
	public void win()
	{
		player.win();
	}
	
	@Override
	public int getMode()
	{
		return player.getMode();
	}
	
	public void setPlayerTo(Player p)
	{
		int aiMode = p.getAI() != null ? p.getAI().getMode() : AgoniaAI.MODE_NO_AI;
		player.id = p.getId();
		player.setName(p.getName());
		player.setSetScore(p.getSetScore());
		player.setSetWins(p.getSetWins());
		player.setAI(AgoniaAIBuilder.createById(aiMode, player));
		player.setPlayingInSet(true);
	}
		
	public class InnerPlayer extends Player
	{
		public InnerPlayer()
		{
			super();
		}

		// unused
		public InnerPlayer(String name, int team)
		{
			super(name, team);
		}

		@Override
		public List<Card> draw()
		{
			List<Card> drawed = super.draw();
			addToContainer(drawed);
			refreshInfo();

			return drawed;
		}

		@Override
		public List<Card> draw(int n)
		{
			List<Card> drawed = super.draw(n);
			addToContainer(drawed);
			refreshInfo();

			return drawed;
		}

		@Override
		public List<Card> draw(List<Card> cards)
		{
			List<Card> drawed = super.draw(cards);
			addToContainer(drawed);
			refreshInfo();

			return drawed;
		}

		@Override
		public boolean playCard(Card c)
		{
			boolean b = super.playCard(c);
			removeFromContainer(c, b, 1);
			refreshInfo();

			return b;
		}

		@Override
		public boolean playCard(int i)
		{
			boolean b = super.playCard(i);
			removeFromContainer(i, b);
			refreshInfo();

			return b;
		}

		@Override
		public void addScore(int points)
		{
			super.addScore(points);
			refreshInfo();
		}
	}
}
