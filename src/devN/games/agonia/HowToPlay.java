package devN.games.agonia;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.ViewFlipper;

public class HowToPlay extends Activity
{
	private ViewFlipper flipper;
	private Button prev;
	private Button next;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.how_base);
		
		flipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
		prev = (Button) findViewById(R.id.how_btn_prev);
		next = (Button) findViewById(R.id.how_btn_next);
		
		prev.setVisibility(View.INVISIBLE);
		
		prev.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v)
			{
				View first = flipper.getChildAt(0);
				
				flipper.showPrevious();
				onFlipperSwitch();
				
				if (flipper.getCurrentView().equals(first))
				{
					prev.setVisibility(View.INVISIBLE);
				}
			}
		});
		
		next.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v)
			{
				View last = flipper.getChildAt(flipper.getChildCount() - 1);
				
				prev.setVisibility(View.VISIBLE);
				
				if (flipper.getCurrentView().equals(last))
				{
					finish();
				}
				else
				{
					flipper.showNext();
					onFlipperSwitch();
				}
			}
		});
	}
	
	private void onFlipperSwitch()
	{
		try
		{
			ScrollView sv = (ScrollView) flipper.getCurrentView();
			sv.scrollTo(0, 0);
		}
		catch (Exception ex)
		{
		}
	}
	
}
