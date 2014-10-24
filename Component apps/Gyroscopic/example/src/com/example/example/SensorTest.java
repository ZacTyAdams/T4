package com.example.example;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SensorTest extends Activity implements SensorEventListener {
	
	SensorManager sensorManager = null;
	
	Button actionButton;
	
	//For accelerometer values
	TextView outputX;
	TextView outputY;
	TextView outputZ;
	
	//For orientation values
	TextView outputX2;
	TextView outputY2;
	TextView outputZ2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sensors);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		
		//Button for button
		actionButton = (Button) findViewById(R.id.bAction);
		
		//TextViews for data output
		outputX = (TextView) findViewById(R.id.Xaccel);
		outputY = (TextView) findViewById(R.id.Yaccel);
		outputZ = (TextView) findViewById(R.id.Zaccel);
		
		outputX2 = (TextView) findViewById(R.id.Xgrav);
		outputY2 = (TextView) findViewById(R.id.Ygrav);
		outputZ2 = (TextView) findViewById(R.id.Zgrav);
		
		actionButton.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				onGoHam();
				return false;
			}
		});
	}

	protected void onGoHam(){
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
	}
	
	/*
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
	}
	*/
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
		sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		synchronized(this){
			switch(event.sensor.getType()){
				case Sensor.TYPE_ACCELEROMETER:
					outputX.setText("X value = " + Float.toString(event.values[0]));
					outputY.setText("Y value = " + Float.toString(event.values[1]));
					outputZ.setText("Z value = " + Float.toString(event.values[2]));
					break;
				
				case Sensor.TYPE_GYROSCOPE:
					outputX2.setText("X value = " + Float.toString(event.values[0]));
					outputY2.setText("Y value = " + Float.toString(event.values[1]));
					outputZ2.setText("Z value = " + Float.toString(event.values[2]));
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	
	
}
