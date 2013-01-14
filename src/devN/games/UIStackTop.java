package devN.games;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import devN.etc.dragdrop.DragSource;
import devN.etc.dragdrop.DropTarget;
import devN.games.agonia.F;
import devN.games.agonia.R;

public class UIStackTop extends UICard implements DropTarget
{
	@SuppressWarnings("unused")
	private final static String tag = "dragdrop";
	
	private static boolean colorHints;
	
	private CardGame game;
	 
	public UIStackTop(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		if (!isInEditMode())
		{
			colorHints = F.getBoolean(F.KEY_COLOR_HINTS, true);
		}
	}

	public UIStackTop(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		if (!isInEditMode())
		{
			colorHints = F.getBoolean(F.KEY_COLOR_HINTS, true);
		}
	}

	public UIStackTop(Context context)
	{
		super(context);
	}

	public UIStackTop(Context context, Card c, boolean visible)
	{
		super(context, c, visible);
	}

	public UIStackTop(UICard c)
	{
		super(c);
	}

	public CardGame getGame()
	{
		return game;
	}

	public void setGame(CardGame game)
	{
		this.game = game;
	}
	
	private void resetBackground()
	{
		if (!colorHints)
		{
			return;
		}
		
		setBackgroundResource(0);
		invalidate();
	}

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
						Object dragInfo)
	{
		resetBackground();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
							Object dragInfo)
	{
		if (!colorHints || dragInfo instanceof UIDeck)
		{
			return;
		}
		
		UICard draggedCard = (UICard) dragInfo;
		
		boolean b = game.canPlay(draggedCard.getCard());
		
		Drawable bgdDrawable;
		
		if (b)
		{
			bgdDrawable = getResources().getDrawable(R.drawable.accept);
		}
		else 
		{
			bgdDrawable = getResources().getDrawable(R.drawable.reject);
		}
		
		setBackgroundDrawable(bgdDrawable);
		
		invalidate();
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
		resetBackground();
	}

	/*
	 * @param dragInfo UICard pou ginete drag
	 */
	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
								Object dragInfo)
	{
		UICard draggedCard = (UICard) dragInfo;

		boolean b = game.canPlay(draggedCard.getCard());
		
		return b;
	}
}
