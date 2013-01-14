package devN.games.agonia;

import devN.games.AIPlayer;
import devN.games.Card;
import devN.games.Player;

public interface AgoniaAI extends AIPlayer
{
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
		private static final int MODE_TO_STRING_LEN = 3;

		public static AgoniaAI createById(int mode, Player p)
		{
			AgoniaAI cpuAI;
			
			switch (mode)
			{
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
			if (id == MODE_NO_AI)
			{
				return "";
			}
			
			String[] modes = F.getResources().getStringArray(R.array.difficult_modes);
			
			return modes[id].substring(0, MODE_TO_STRING_LEN);
		}
	}
}
