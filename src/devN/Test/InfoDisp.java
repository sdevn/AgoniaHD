package devN.Test;

import devN.games.Card;
import devN.games.UICard;

public class InfoDisp
{
	public UICard c;
	
	public void set(UICard c)
	{
		this.c = c;
	}
	
	public UICard get()
	{
		return c;
	}
	
	public void change(Card c)
	{
		this.c.setCard(new Card(c));
		this.c.setImage();
	}
}
