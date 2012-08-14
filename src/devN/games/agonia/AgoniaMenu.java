package devN.games.agonia;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

public class AgoniaMenu extends Activity
{
	private static final int DIALOG_ABOUT_ID = 0;
	private static final int DIALOG_COMMING_SOON_ID = 1;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);

		if (F.settings == null)
		{
			new F(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart()
	{
		super.onStart();

		Button pref = (Button) findViewById(R.id.btPref);
		Button game = (Button) findViewById(R.id.btGame);
		Button how = (Button) findViewById(R.id.btHow);
		Button about = (Button) findViewById(R.id.btAbout);
		Button exit = (Button) findViewById(R.id.btExit);

		pref.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(AgoniaMenu.this, AgoniaPref.class));
			}
		});

		game.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(AgoniaMenu.this, AgoniaGame.class));
			}
		});

		how.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub

			}
		});
		
		about.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				showDialog(DIALOG_ABOUT_ID);
			}
		});
		
		if (exit == null)
		{
			return;
		}
		
		exit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				atExit();
			}
		});
	}
	
	@Override
	public void onBackPressed()
	{
        atExit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id)
	{
		Dialog dialog = null;

		switch (id)
		{
		case DIALOG_ABOUT_ID:
			{
			String message = getString(R.string.dlg_about_message);

			WebView wv = new WebView (this);
			wv.loadData(message, "text/html", "utf-8");
			wv.setBackgroundColor(Color.TRANSPARENT);
			wv.getSettings().setBuiltInZoomControls(false);
			wv.getSettings().setDefaultFontSize(14);

			AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setTitle("About")
			.setView(wv)
			.setPositiveButton("OK", new DialogInterface.OnClickListener(){
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{  /*
					* nothing to do
					*/
				}
			})
			;
			dialog = builder.create();
			}
			break;
		
		case DIALOG_COMMING_SOON_ID:
			{
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setTitle("Comming soon!")
			.setMessage(R.string.dlg_comming_soon_message)
			.setPositiveButton("OK", new DialogInterface.OnClickListener(){
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					finish();
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener(){
				
				@Override
				public void onCancel(DialogInterface dialog)
				{	
					finish();
				}
		
			})
			;
			dialog = builder.create();
			}
			break;
			
		default:
			{
			dialog = null;
			}
			break;
		}

		return dialog;
	}
	
	private void atExit()
	{
		showDialog(DIALOG_COMMING_SOON_ID);
	}
}
