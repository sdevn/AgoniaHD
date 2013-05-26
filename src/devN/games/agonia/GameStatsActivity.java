package devN.games.agonia;

import devN.etc.TypefaceUtils;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class GameStatsActivity extends Activity
{
	private Spinner spnProfile;
	private TextView txvContent;
	private ImageButton ibtDelete;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_stats);
		
		spnProfile = (Spinner) findViewById(R.id.spn_stats_profile);
		txvContent = (TextView) findViewById(R.id.txv_stats_content);
		ibtDelete = (ImageButton) findViewById(R.id.ibt_stats_delete);
		
		spnProfile.setOnItemSelectedListener(oislProfile);
		ibtDelete.setOnClickListener(oclDelete);
		ibtDelete.setOnLongClickListener(ocllDelete);
		
		updateProfiles();
		
		ViewGroup root = (ViewGroup) findViewById(R.id.root_stats);
		
		TypefaceUtils.setAllTypefaces(root, this, getString(R.string.custom_font));
	}
	
	private void updateProfiles()
	{
		ArrayAdapter<CharSequence> adapter = 
				new ArrayAdapter<CharSequence>(this, 
						android.R.layout.simple_spinner_item, 
						((AgoniaApp)getApplication()).getManager().getProfileNames());
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		if (adapter.isEmpty())
		{
			txvContent.setText(R.string.stats_games_infos_emptry);
			ibtDelete.setVisibility(View.INVISIBLE);
		}
		else 
		{
			ibtDelete.setVisibility(View.VISIBLE);
		}
		
		spnProfile.setAdapter(adapter);
	}

	private OnItemSelectedListener oislProfile = new OnItemSelectedListener(){

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
		{
			String profile = spnProfile.getSelectedItem().toString();
			Object[] infos = ((AgoniaApp)getApplication()).getManager().getStatsOf(profile).getInfos();
			txvContent.setText(getString(R.string.stats_games_infos, infos));
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent)
		{ }
	};
	
	private View.OnClickListener oclDelete = new View.OnClickListener(){
		
		@Override
		public void onClick(View v)
		{
			// TODO: Localize it
			Toast
			.makeText(GameStatsActivity.this, 
					"Long click to delete stats", 
					Toast.LENGTH_SHORT)
			.show();
		}
	};
	
	private View.OnLongClickListener ocllDelete = new View.OnLongClickListener(){
		
		@Override
		public boolean onLongClick(View v)
		{
			String profile = spnProfile.getSelectedItem().toString();
			
			((AgoniaApp)getApplication()).getManager().deleteStatsOf(GameStatsActivity.this, profile);
			
			updateProfiles();
			
			return true;
		}
	};
}