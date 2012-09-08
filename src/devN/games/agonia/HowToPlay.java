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
	private View flipperLast;
	private View flipperFirst;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.how_base);
		
		flipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
		flipperLast = flipper.getChildAt(flipper.getChildCount() - 1);
		flipperFirst = flipper.getChildAt(0);
		
		flipper.setInAnimation(getApplicationContext(), R.anim.right_to_original_pos);
		flipper.setOutAnimation(getApplicationContext(), R.anim.out_to_left);
		
		prev = (Button) findViewById(R.id.how_btn_prev);
		next = (Button) findViewById(R.id.how_btn_next);
		
		prev.setVisibility(View.INVISIBLE);
		
		prev.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v)
			{			
				flipper.setInAnimation(getApplicationContext(), R.anim.left_to_original_pos);
				flipper.setOutAnimation(getApplicationContext(), R.anim.out_to_right);
				flipper.showPrevious();
				onFlipperSwitched();
			}
		});
		
		next.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v)
			{
				if (flipper.getCurrentView().equals(flipperLast))
				{
					finish();
					overridePendingTransition(R.anim.right_to_original_pos, R.anim.out_to_left);
				}
				else
				{
					flipper.setInAnimation(getApplicationContext(), R.anim.right_to_original_pos);
					flipper.setOutAnimation(getApplicationContext(), R.anim.out_to_left);
					flipper.showNext();
					onFlipperSwitched();
				}
			}
		});
	}
	
	private void onFlipperSwitched()
	{
		try
		{
			ScrollView sv = (ScrollView) flipper.getCurrentView();
			sv.scrollTo(0, 0);
		}
		catch (Exception ex)
		{
		}
		
		View cur = flipper.getCurrentView();
		
		if (cur.equals(flipperLast))
		{
			next.setText(R.string.done);
		}
		else 
		{
			next.setText(R.string.next);
		}
		
		if (cur.equals(flipperFirst)) 
		{
			prev.setVisibility(View.INVISIBLE);
		}
		else 
		{
			prev.setVisibility(View.VISIBLE);
		}
	}
	
}
