package com.example.taptwisttunes;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BPMCalc extends ActionBarActivity {
	
	Button bButton;
	TextView bCalc;

	long time1;
	long time2;
	long averageBPM;
	int buttonPress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bpmcalc);
		
		bButton = (Button) findViewById(R.id.bButton);
		bCalc = (TextView) findViewById(R.id.bCalc);
		
		time1 = System.currentTimeMillis();
        time2 = System.currentTimeMillis();
        averageBPM = 0;
        buttonPress = 0;
        
        bButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(buttonPress == 4) {
					buttonPress = 0;
					String stringBPM = String.valueOf(averageBPM);
					bCalc.setText("Average BPM: " + stringBPM + "BPM");
					averageBPM = 0;
				}
				else {
					time2 = time1;
				    time1 = System.currentTimeMillis();
			        long difference = time1 - time2;
			        long BPM = 60000/difference;
			        averageBPM = (averageBPM * buttonPress) + BPM;
			        buttonPress++;
			        averageBPM = (averageBPM/buttonPress);
			        String numOfPresses = String.valueOf(5-buttonPress);
			     	bCalc.setText("Press button " + numOfPresses + " more times");
				}
				
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bpmcalc, menu);
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
