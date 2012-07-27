package devN.games;

import devN.Test.InfoDisp;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;

public class UICard extends ImageButton implements OnClickListener
{
	protected Card card;
	protected boolean visible;
	
	protected static InfoDisp i = new InfoDisp();

	public UICard(Context context, Card c, boolean visible)
	{
		super(context);
		this.visible = visible;
		this.card = c;

		setImage();

		this.setBackgroundColor(Color.TRANSPARENT);
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		this.setLayoutParams(lp);
		this.setOnClickListener(this);
	}
	
	public UICard(UICard c)
	{
		this(c.getContext(), new Card(c.card), c.visible);
	}
	
	public static InfoDisp getInfoDisp()
	{
		return i;
	}

	public Card getCard()
	{
		return card;
	}

	public void setCard(Card card)
	{
		this.card = card;
		setImage();
	}

	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	public void setImage()
	{
		String img;
		int id;
		
		if (visible)
		{
			img = "card_" + card.getSuit() + "_" + card.getRank();
		}
		else
		{
			img = "card_back_blue_1";
		}

		id = getResources().getIdentifier(img, "drawable", getContext().getPackageName());
		
		if (id != 0)
		{
			super.setImageResource(id);
		}
		
	}

	public void setImage(String resName)
	{
		int id;
	
		id = getResources().getIdentifier(resName, "drawable", getContext().getPackageName());
		
		if (id != 0)
		{
			super.setImageResource(id);
			requestLayout();
		}
		
	}
	@Override
	public void onClick(View v)
	{
		if (visible)
		{
			i.change(card);
			i.c.setVisibility(VISIBLE);
		}
		
	}

}
