package devN.games;

import android.content.Context;
import android.util.AttributeSet;
import devN.etc.dragdrop.DragSource;
import devN.etc.dragdrop.DropTarget;

public class UIStackTop extends UICard implements DropTarget
{
	private CardGame game;
	
	public UIStackTop(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public UIStackTop(Context context, AttributeSet attrs)
	{
		super(context, attrs);
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

	/*
	 * @param dragInfo UICard pou ginete drag
	 * 
	 * @see devN.etc.dragdrop.DropTarget#acceptDrop(devN.etc.dragdrop.DragSource, int, int, int, int, java.lang.Object)
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
