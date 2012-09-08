package devN.etc;

import devN.games.agonia.AgoniaMenu;
import devN.games.agonia.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreen extends Activity
{
	private ImageView ivDevn; 

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		
		ivDevn = (ImageView) findViewById(R.id.ic_devn);
	
		Animation animSplash = AnimationUtils.loadAnimation(this, R.anim.splash);
		animSplash.setAnimationListener(new AnimationListener(){
			
			@Override
			public void onAnimationStart(Animation animation)
			{	/*
				 * nothing to do
				 */
			}
			
			@Override
			public void onAnimationRepeat(Animation animation)
			{	/*
				 * nothing to do
				 */
			}
			
			@Override
			public void onAnimationEnd(Animation animation)
			{
				Intent mainIntent = new Intent(SplashScreen.this, AgoniaMenu.class);
				startActivity(mainIntent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				finish();
			}
		});
		ivDevn.startAnimation(animSplash);		
	}

	@Override
	public void onBackPressed()
	{	/*
		 * nothing to do
		 */
	}
}
