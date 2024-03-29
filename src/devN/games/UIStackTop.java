package devN.games;

import android.content.Context;
import android.util.AttributeSet;
import devN.etc.dragdrop.DragSource;
import devN.etc.dragdrop.DropTarget;
import devN.games.agonia.R;

public class UIStackTop extends UICard implements DropTarget
{
	private static boolean colorHints;
	
	private CardGame game;
	 
	public UIStackTop(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public UIStackTop(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs)
	{
		if (!isInEditMode())
		{
//			colorHints = F.getBoolean(F.KEY_COLOR_HINTS, true);
		}
	}
//	public UIStackTop(Context context, Card c, boolean visible)
//	{
//		super(context, c, visible);
//	}
//
//	public UIStackTop(UICard c)
//	{
//		super(c);
//	}

	/** v2.4b to replace F.java */
	public static void setColorHints(boolean colorHints)
	{
		UIStackTop.colorHints = colorHints;
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
		setPadding(0, 0, 0, 0);

		requestLayout();
		invalidate();
	}

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
						Object dragInfo)
	{
		resetBackground();
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
							Object dragInfo)
	{
		if (!colorHints || dragInfo instanceof UIDeck)
		{
			return;
		}
		
		UICard draggedCard = (UICard) dragInfo;

		boolean can = game.canPlay(draggedCard.getCard());
		
		int bkgrId = can ? R.drawable.bkgr_uicard_accept : R.drawable.bkgr_uicard_reject;
		
		setBackgroundResource(bkgrId);
		
		requestLayout();
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
