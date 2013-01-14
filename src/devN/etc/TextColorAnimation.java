package devN.etc;

import android.os.SystemClock;
import android.widget.TextView;

public class TextColorAnimation
{
	private final static int COLOR_SWITCH_DELAY = 333;
	
	private int[] aSwitchingColors;
	private int endColor;
	private int iColors;
	private TextView textView;
	private boolean running;
	private boolean paused;
	private Thread thread;
	
	public TextColorAnimation(TextView tv, int[] switchingColors)
	{
		running = false;
		paused = true;
		iColors = 0;
		aSwitchingColors = switchingColors;
		textView = tv;
		thread = new SwitcherThread();
		thread.start();
	}

	public void start()
	{
		running = true;
		paused = false;
		iColors = 0;	
	}
	
	public void stop()
	{
		running = paused = false;
//
//		while (thread.isAlive())
//		{
//			SystemClock.sleep(100);
//		}
	}
	
	public void pause()
	{
		if (!paused)
		{
			paused = true;
			thread.interrupt();
		}
	}
	
	private void reset()
	{
		iColors = 0;
		
		textView.post(new Runnable(){
			public void run()
			{
				textView.setTextColor(endColor);
			}
		});
	}

	public int getEndColor()
	{
		return endColor;
	}

	public TextView getTextView()
	{
		return textView;
	}

	public void setTextView(TextView textView)
	{
		this.textView = textView;
	}

	public void setEndColor(int endColor)
	{
		this.endColor = endColor;
	}
	
	private class SwitcherThread extends Thread
	{
		private class TextColorSwitcher implements Runnable
		{
			private int iColor;
			
			public TextColorSwitcher(int i)
			{
				iColor = i;
			}
			
			@Override
			public void run()
			{
				textView.setTextColor(aSwitchingColors[iColor]);
				textView.invalidate();
			}
		}
		
		public void run()
		{
			while (!running && paused)
			{// before first start()
				SystemClock.sleep(100);
			}
			
			while (running)
			{
				try
				{
					Thread.sleep(COLOR_SWITCH_DELAY);
					Runnable r = new TextColorSwitcher(iColors);
					textView.post(r);
					++iColors;
					iColors %= aSwitchingColors.length;
				}
				catch (InterruptedException ex)
				{
					reset();
					
					while (paused)
					{
						SystemClock.sleep(100);
					}
				}
			}
		}
	}
}
