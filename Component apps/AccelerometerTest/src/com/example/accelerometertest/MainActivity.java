package com.example.accelerometertest;

import java.text.DecimalFormat;

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
//end tarsos imports



public class MainActivity extends ActionBarActivity implements SensorEventListener{

	//from tutorial
	private SensorManager senSensorManager;
	private Sensor senAccelerometer;
	
	//from tutorial
	private long lastUpdate = 0;
	Button pitchUp;
	Button pitchDown;
	public boolean upHoldStatus = false;
	public boolean downHoldStatus = false;
	private float last_x, last_y, last_z;
	private static final int SHAKE_THRESHOLD = 500;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//from tutorial
		//sets up connection with accelerometer
		senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
	
	    //set up connections with each button and set up listeners
	    //each listener will toggle between a "pushed down" and "released"
	    //position, where 'true' indicates that it is being held down and false is opposite
	    pitchUp = (Button) findViewById(R.id.pitchUp);
	    pitchUp.setOnTouchListener(new OnTouchListener() {
	        @Override
			public boolean onTouch(View v, MotionEvent event) {
	            switch(event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	                upHoldStatus = true;
	                break;
	            case MotionEvent.ACTION_UP:
	                upHoldStatus = false;
	                break;
	            }
				return false;
			}
	    });
	    
	    
	    //pitch down button, see above...
	    pitchDown = (Button) findViewById(R.id.pitchDown);
	    pitchDown.setOnTouchListener(new OnTouchListener() {
	        @Override
			public boolean onTouch(View v, MotionEvent event) {
	            switch(event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	                downHoldStatus = true;
	                break;
	            case MotionEvent.ACTION_UP:
	                downHoldStatus = false;
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
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		//set up sensor
		Sensor mySensor = event.sensor;
		 
		    if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
		    	//get values from sensor
		    	float x = event.values[0];
		        float y = event.values[1];
		        float z = event.values[2];	
		        
		        //get current time
		        long curTime = System.currentTimeMillis();
		        
		        if ((curTime - lastUpdate) > 150) {
		        	//calculate time since last update
		            long diffTime = (curTime - lastUpdate);
		            lastUpdate = curTime;
		            
		            //calculate general speed from acceleration in each axes
		            float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;
		            
		            //shake speed exceeds the threshold set, we need to change the current factor
		            //or variable
		            if (speed > SHAKE_THRESHOLD) {
		            	changeNumber();
		            }
		            
		            //update references to positional accelerations
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
	
	//function to change a number when the shake speed exceeds the threshold
	//we will use this to change the currentFactor
	private void changeNumber(){
		//set up association with text element
		TextView display = (TextView) findViewById(R.id.display);
		String curText = (String) display.getText();

		//set a format for displaying the value
		DecimalFormat oneDigit = new DecimalFormat("#,##0.0");//format to 1 decimal place
		
		Double value;
		
		if (isDouble(curText)){
			//increment or decrement current value based on button held
			if(upHoldStatus == true){
				value = Double.parseDouble(curText);
				if(value < 2.0)
					value = value + 0.1;
				display.setText(oneDigit.format(value).toString());
			}
			if(downHoldStatus == true){
				value = Double.parseDouble(curText);
				if(value > 0.1)
					value = value - 0.1;
				display.setText(oneDigit.format(value).toString());
			}
		}
		else{
			//set baseline of 1.0
			if(upHoldStatus == true){
				value = (double) 1;
				display.setText(oneDigit.format(value).toString());
			}
			if(downHoldStatus == true){
				value = (double) 1;
				display.setText(oneDigit.format(value).toString());
			}
		}
		//if we want this function to return the variable so we can use it in as currentFactor
		//we will have to change references of 'value' to references to the 'currentFactor' variable
	}
	
	public static boolean isDouble(String s) {
	    try { 
	        Double.parseDouble(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    // only got here if we didn't return false
	    return true;
	}
}
