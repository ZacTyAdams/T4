package com.example.taptwisttunes;

import android.support.v7.app.ActionBarActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;



public class GyroscopeTest extends ActionBarActivity implements OnTouchListener, SensorEventListener {
	
	Button Play, Stop, actionButton;
	SeekBar seeker;
	MediaPlayer ourToneToMod;
	TextView displayProgress;
	SensorManager sensorManager = null;
//	SensorEvent gyroEvent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gyroscope_test);
		displayProgress = (TextView) findViewById(R.id.seekBarProgressDelta);
		Play = (Button) findViewById(R.id.btStart);
		Stop = (Button) findViewById(R.id.btStop);
		actionButton = (Button) findViewById(R.id.btListen);
		seeker = (SeekBar) findViewById(R.id.seekBar1);
		ourToneToMod = MediaPlayer.create(this, R.raw.spashsound);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	
		Play.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ourToneToMod.setLooping(true);
				ourToneToMod.start();
			}
		});
		
		Stop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ourToneToMod.stop();
			}
		});
		
		actionButton.setOnTouchListener(this);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gyroscope_test, menu);
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

	@Override
	public boolean onTouch(View v, MotionEvent event){
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:{
				onGoHam();
				break;
			}
			case MotionEvent.ACTION_UP:{
				unGoHam();
			}
		}
		
		return true;
		
	}
	
	protected void unGoHam(){
		sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
	}
	
	protected void onGoHam(){
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		synchronized(this){
			switch(event.sensor.getType()){
				
				case Sensor.TYPE_GYROSCOPE:
					displayProgress.setText("X value = " + Float.toString(event.values[0]));
			}
		}
	}
	
}
