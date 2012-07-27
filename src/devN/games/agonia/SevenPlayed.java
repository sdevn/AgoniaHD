package devN.games.agonia;

public class SevenPlayed extends RuntimeException
{
	private static final long serialVersionUID = -5403460006971647946L;
	protected int suit;
	
	public SevenPlayed(int suit)
	{
		super();
		this.suit = suit;
	}
}
