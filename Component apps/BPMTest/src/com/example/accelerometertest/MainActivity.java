package com.example.accelerometertest;

import java.text.DecimalFormat;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.CountDownTimer;
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



//MAIN ACTIVITY FOR SETTING UP THE BPM FACTOR
//THIS IS ALL THE BARE FUNCTIONALITY WE WILL THROW INTO MAIN IMPORT CLASS

public class MainActivity extends ActionBarActivity{


	Button tapMe;
	long milliSecondsElapsed = 0;
	long BPM;
	long taps;
	Boolean isRunning = false;
	CountDownTimer timer;
	TextView display;
	TextView bpmDisplay;
	Double BPMFactor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		taps = 0;
		BPM = 0;
		
		display = (TextView)findViewById(R.id.display);
		bpmDisplay = (TextView)findViewById(R.id.bpmDisplay);
		
		
		//set up a format for returning the factor
		DecimalFormat oneDigit = new DecimalFormat("#,##0.0");
		
		//set up the timer and it's functions
		timer = new CountDownTimer(10000, 1000){
			//update necessary variables
			public void onTick(long millisUntilFinished) {
				//timer is running
				isRunning = true;
				//show time remaining as button text
				tapMe.setText(new Integer((int) (millisUntilFinished/1000)).toString());
				//show current taps
				bpmDisplay.setText(new Long(taps).toString());
		     }
			//timer is finished, calculate total bpm based on taps
		    public void onFinish() {
		    	//timer isn't running
				isRunning = false;
				//take taps and mult by 6
				BPM = (int) (taps * 6);
				//present final 
				display.setText("Your total BPM was: " + BPM);
				//reset text
				tapMe.setText("Tap Me Bro");
				
				//this is where we will turn bpm into double factor
				//!!THIS IS WHERE WE SET THE FACTOR TO PASS THE PLAYER
				BPMFactor = (double)BPM/100;
				if(BPMFactor > 2)
					BPMFactor = 2.0;
				else if(BPMFactor <= 0){
					BPMFactor = 0.1;
				}
				
				//just display the text
				bpmDisplay.setText(new Double(BPMFactor).toString());
		    }
		};
		
	    //position, where 'true' indicates that it is being held down and false is opposite
	    tapMe = (Button) findViewById(R.id.tapMe);
	    tapMe.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View arg0) {
				//check if timer is running
				if(isRunning == true){
					taps = taps + 1;
				}
				//reset taps and start the timer
				else{
					taps = 1;
					timer.start();
				}
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
}
