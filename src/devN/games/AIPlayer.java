package devN.games;

public interface AIPlayer
{	
	public final static int ANY_KIND = -1;
	public static final String TAG = "AI";

	public Card willPlay();
	
//	public Card askFor(int suit, int rank);
	
}
