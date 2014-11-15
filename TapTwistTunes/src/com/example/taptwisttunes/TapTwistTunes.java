package com.example.taptwisttunes;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class TapTwistTunes extends ActionBarActivity {
	
	Button musicPlayer;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		
		//final Intent intent = new Intent(this, AudioPlayer.class);
		
		//musicPlayer = (Button) findViewById(R.id.bMusicPlayer);
		
		
		//musicPlayer.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			//public void onClick(View arg0) {
				//startActivity(intent);
				
			//}
		//});
		
	}
	
	public void navigateM(View view){
		Intent intent = new Intent(this, AudioPlayer.class);
		startActivity(intent);
	}
	
	public void navigateB(View view){
		Intent intent = new Intent(this, BPMCalc.class);
		startActivity(intent);
	}
	
	public void navigateA(View view){
		Intent intent = new Intent(this, AccelerometerTest.class);
		startActivity(intent);
	}

	public void navigateG(View view){
		Intent intent = new Intent(this, GyroscopeTest.class);
		startActivity(intent);
	}
	
	public void navigateR(View view){
		Intent intent = new Intent(this,Recorder.class);
		startActivity(intent);
	}
	
	public void navigateT(View view){
		Intent intent = new Intent(this, TarsosMediaPlayer.class);
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
