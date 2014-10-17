package com.example.example;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;


//import be.tarsos.dsp.*;
//import be.tarsos.dsp.pitch.*;

public class ToneMod extends Activity implements SensorEventListener, OnTouchListener{

	Button Play, Stop, actionButton;
	SeekBar seeker;
	MediaPlayer ourToneToMod;
	TextView displayProgress;
	SensorManager sensorManager = null;
//	SensorEvent gyroEvent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tonemod);
		displayProgress = (TextView) findViewById(R.id.seekBarProgressDelta);
		Play = (Button) findViewById(R.id.bPlay);
		Stop = (Button) findViewById(R.id.bStop);
		actionButton = (Button) findViewById(R.id.bAction);
		seeker = (SeekBar) findViewById(R.id.seekBar1);
		ourToneToMod = MediaPlayer.create(ToneMod.this, R.raw.spashsound);
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
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		synchronized(this){
			switch(event.sensor.getType()){
				
				case Sensor.TYPE_GYROSCOPE:
					displayProgress.setText("X value = " + Float.toString(event.values[0]));
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
}
