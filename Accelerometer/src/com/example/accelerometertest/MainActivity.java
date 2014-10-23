package com.example.accelerometertest;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MainActivity extends ActionBarActivity implements SensorEventListener{

	//from tutorial
	private SensorManager senSensorManager;
	private Sensor senAccelerometer;
	
	//from tutorial
	private long lastUpdate = 0;
	Button hold;
	public boolean holdStatus = false;
	private float last_x, last_y, last_z;
	private static final int SHAKE_THRESHOLD = 600;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//from tutorial
		senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
	
	    hold = (Button) findViewById(R.id.button1);
	    hold.setOnTouchListener(new OnTouchListener() {
	        @Override
			public boolean onTouch(View v, MotionEvent event) {
	            switch(event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	                holdStatus = true;
	                break;
	            case MotionEvent.ACTION_UP:
	                holdStatus = false;
	                break;
	            }
				return false;
			}
	    });
	    
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		//all from tutorial
		 Sensor mySensor = event.sensor;
		 
		    if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
		    	float x = event.values[0];
		        float y = event.values[1];
		        float z = event.values[2];	
		        
		        long curTime = System.currentTimeMillis();
		        
		        if ((curTime - lastUpdate) > 100) {
		            long diffTime = (curTime - lastUpdate);
		            lastUpdate = curTime;
		            
		            float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;
		            
		            if (speed > SHAKE_THRESHOLD) {
		            	incrementNumber();
		            }
		            
		            last_x = x;
		            last_y = y;
		            last_z = z;
		        }
		    }
		
	}
	
	//from tutorial
	protected void onPause() {
	    super.onPause();
	    senSensorManager.unregisterListener(this);
	}
	
	//from tutorial
	protected void onResume() {
	    super.onResume();
	    senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private void incrementNumber(){
		
		TextView display = (TextView) findViewById(R.id.display);
	
		String curText = (String) display.getText();
	
		if (isInteger(curText)){
			if(holdStatus == true){
				Integer value = Integer.parseInt(curText);
				value++;
				display.setText(value.toString());
			}
		}
		else{
			if(holdStatus == true){
			Integer value = 0;
			display.setText(value.toString());
			}
		}
	}
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    // only got here if we didn't return false
	    return true;
	}
	
}
