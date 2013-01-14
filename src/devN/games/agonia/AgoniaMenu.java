package devN.games.agonia;

import java.util.Random;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class AgoniaMenu extends Activity
{
	private static final int DIALOG_ABOUT_ID = 0;
	private static final int DIALOG_COMMING_SOON_ID = 1;
	private static final int DIALOG_RATE_ID = 2;
	
	private static final double CHANCE_COMMING_DIALOG = 1.0D / 9.0D;
	
	private ImageButton ibtRate;
	private ImageButton ibtFacebook;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);

		if (F.settings == null)
		{
			new F(this);
		}
	
		Button pref = (Button) findViewById(R.id.btPref);
		Button game = (Button) findViewById(R.id.btGame);
		Button how = (Button) findViewById(R.id.btHow);
		
		ImageButton about = (ImageButton) findViewById(R.id.ibtAbout);
		ImageButton exit = (ImageButton) findViewById(R.id.ibtExit);
		
		ibtRate = (ImageButton) findViewById(R.id.ibtRate);
		ibtFacebook = (ImageButton) findViewById(R.id.ibtFacebook);
		
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
				startActivity(new Intent(AgoniaMenu.this, GameSetActivity.class));
			}
		});

		how.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(AgoniaMenu.this, HowToPlay.class));
				overridePendingTransition(R.anim.right_to_original_pos, R.anim.out_to_left);
			}
		});
		
		about.setOnClickListener(new OnClickListener(){

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v)
			{
				showDialog(DIALOG_ABOUT_ID);
			}
		});

		exit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				atExit();
			}
		});
		
		ibtRate.setOnClickListener(oclRate);
		
		ibtFacebook.setVisibility(View.GONE);
	}
	
	@Override
	public void onBackPressed()
	{
        atExit();
	}

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
			wv.setBackgroundColor(getResources().getColor(R.color.tsoha));
			wv.getSettings().setBuiltInZoomControls(false);
			wv.getSettings().setDefaultFontSize(14);

			AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setTitle("About")
			.setView(wv)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
				
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
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
				
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
		case DIALOG_RATE_ID:
			{
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setTitle(R.string.dlg_rate_title)
			.setMessage(R.string.dlg_rate_message)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					if (which == DialogInterface.BUTTON_POSITIVE)
					{
						try
						{
							Uri appUri = Uri.parse("market://details?id=" + getPackageName());
							Intent intent = new Intent(Intent.ACTION_VIEW, appUri);
							startActivity(intent);
						}
						catch (Exception ex)
						{
							Toast.makeText(AgoniaMenu.this, "No Google Play Store installed!!", Toast.LENGTH_SHORT).show();
						}
					}
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{ /* just close the dialog */}
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
	
	@SuppressWarnings("deprecation")
	private void atExit()
	{
		Random random = new Random();
		
		if (random.nextDouble() < CHANCE_COMMING_DIALOG)
		{
			showDialog(DIALOG_COMMING_SOON_ID);
		}
		else 
		{
			finish();
		}
	}
	
	private View.OnClickListener oclRate = new View.OnClickListener(){
		
		@SuppressWarnings("deprecation")
		@Override
		public void onClick(View v)
		{
			showDialog(DIALOG_RATE_ID);
		}
	};
}
