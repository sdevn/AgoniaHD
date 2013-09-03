package devN.games.agonia;

import devN.games.AIPlayer;
import devN.games.Card;
import devN.games.Player;

public interface AgoniaAI extends AIPlayer
{
	public static final int MODE_ONLINE		= -2;
	public static final int MODE_NO_AI		= -1;
	public static final int MODE_MONKEY 	= 0;
	public static final int MODE_EASY 		= 1;
	public static final int MODE_MODERATE 	= 2;
	public static final int MODE_MEGAMIND 	= 3;
		
	public int getAceSuit();
	
	public Card playSeven();
	
	public int getMode();
	
	public static class AgoniaAIBuilder
	{
		public static final AgoniaAI ONLINE_AI = new AgoniaAI() {

			@Override
			public Card willPlay()
			{
				return Card.NULL_CARD;
			}

			@Override
			public int getAceSuit()
			{
				return 0;
			}

			@Override
			public Card playSeven()
			{
				return Card.NULL_CARD;
			}

			@Override
			public int getMode()
			{
				return MODE_ONLINE;
			}
			
		};
		private static final int MODE_TO_STRING_LEN = 3;
		
		public static final String[] PSZAI_STRINGS = new String[4];

		/**
		 * @param mode if its {@link AgoniaAI#MODE_NO_AI} null is returned
		 */
		public static AgoniaAI createById(int mode, Player p)
		{
			AgoniaAI cpuAI;
			
			switch (mode)
			{
			case MODE_ONLINE:
				cpuAI = ONLINE_AI;
				break;
				
			case MODE_NO_AI:
				cpuAI = null;
				break;
				
			case MODE_MONKEY:
				cpuAI = new MonkeyAI(p);
				break;
				
			case MODE_EASY:
				cpuAI = new EasyAI(p);
				break;
				
			case MODE_MODERATE:
				cpuAI = new ModerateAI(p);
				break;
					
			case MODE_MEGAMIND:
				throw new RuntimeException("MODE_MEGAMIND Not implemented yet!");
//				break;
				
			default:
				throw new RuntimeException("Uknown difficult mode");
//				break;
			}
			
			return cpuAI;
		}
		
		public static String getAIString(int id)
		{
			if (id < 0)
			{
				return "";
			}
			
			return PSZAI_STRINGS[id].substring(0, MODE_TO_STRING_LEN);
		}
	}
}
