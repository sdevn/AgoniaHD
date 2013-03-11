package devN.etc;

import devN.games.agonia.AgoniaGame;
import devN.games.agonia.AgoniaMenu;
import devN.games.agonia.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreen extends Activity
{
	private ImageView ivDevn; 
	// TODO: add drill sound effect with animation
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		/* 	v2.1 added
		 	Nasty hack to load the game sound effects without delay
		 */
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
       	
       	boolean useSFX = prefs.getBoolean(getString(R.string.key_game_sfx), true);
		AgoniaGame.initSoundPool(this, useSFX);
		
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
