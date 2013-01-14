package devN.etc;

import java.util.Hashtable;
import java.util.Map;
import android.util.Log;

public class TextColorAnimationGroup
{
	/**
	 * Key is Player id
	 */
	private Map<Integer, TextColorAnimation> animations;
	
	public TextColorAnimationGroup()
	{
		animations = new Hashtable<Integer, TextColorAnimation>();
	}
	
	public void add(int playerId, TextColorAnimation anim)
	{
		animations.put(playerId, anim);
	}
	
	public void addAll(int[] playerIds, TextColorAnimation[] anims)
	{
		for (int i = 0; i < anims.length; i++)
		{
			animations.put(playerIds[i], anims[i]);
		}
	}

	public void pause(int playerId)
	{
		TextColorAnimation animation = animations.get(playerId);

		animation.pause();
	}
	
	public void start(int playerId)
	{
		animations.get(playerId).start();
	}
	
	public void pauseAll()
	{
		for (TextColorAnimation animation : animations.values())
		{
			animation.pause();
		}
	}
	
	public void stopAll()
	{
		for (TextColorAnimation animation : animations.values())
		{
			animation.stop();
		}
	}
}
