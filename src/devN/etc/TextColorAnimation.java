package devN.etc;

import android.widget.TextView;

public class TextColorAnimation
{
	private final static int COLOR_SWITCH_DELAY = 333;

	private int[] aSwitchingColors;
	private int endColor;
	private int iColors;
	private TextView textView;
	private boolean running;
	private Runnable mRunnable;

	public TextColorAnimation(TextView tv, int[] switchingColors)
	{
		running = false;
		iColors = 0;
		aSwitchingColors = switchingColors;
		textView = tv;
	}

	public void start()
	{
		if (!running)
		{
			running = true;
			iColors = 0;
			textView.post(mRunnable = new TextColorSwitcherRunnable());
		}
	}

	public void stop()
	{
		running = false;
		textView.removeCallbacks(mRunnable);
	}

	public void pause()
	{
		if (running)
		{
			stop();
			reset();
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

	private class TextColorSwitcherRunnable implements Runnable
	{
		@Override
		public void run()
		{
			if (running)
			{
				textView.setTextColor(aSwitchingColors[iColors]);
				textView.invalidate();
				++iColors;
				iColors %= aSwitchingColors.length;
				textView.postDelayed(this, COLOR_SWITCH_DELAY);
			}
		}
	}
}
