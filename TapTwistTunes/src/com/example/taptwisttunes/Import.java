package com.example.taptwisttunes;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd.Parameters;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import be.tarsos.dsp.resample.RateTransposer;
import be.tarsos.dsp.resample.SoundTouchRateTransposer;

import com.example.taptwisttunes.utils.TunnelPlayerWorkaround;
import com.example.taptwisttunes.visualizer.VisualizerView;
import com.example.taptwisttunes.visualizer.renderer.BarGraphRenderer;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class Import extends ActionBarActivity implements SensorEventListener {

	ImageButton browse, stop, play;
	Button pitchUp, pitchDown, toneUp, timeStretch;
	TextView song;
	MediaPlayer mediaPlayer;
	Uri browseUri = null;
	private MediaPlayer mSilentPlayer; /* to avoid tunnel player issue */
	long currentSongPosition = 0;

	static final int REQUEST_AUDIO = 1;
	boolean songSelected; // used to stop play from working until a song is
							// selected
	boolean isPlaying;
	
	AudioDispatcher dispatcher;
	private WaveformSimilarityBasedOverlapAdd wsola;
	private RateTransposer rateTransposer;
	private AndroidAudioPlayer player;
	UniversalAudioInputStream audioStream;
	TarsosDSPAudioFormat audioFormat;
	private long currentRate;
	private double tempo;
	private double sampleRate;
	InputStream wavStream;
	private Thread t;
	
	//Accelerometer variables
	private SensorManager senSensorManager;
	private Sensor senAccelerometer;
	private long lastUpdateA = 0;
	public boolean upHoldStatus = false;
	public boolean downHoldStatus = false;
	private float last_x, last_y, last_z;
	private static final int SHAKE_THRESHOLD = 500;
	
	//Gyroscopic variables
	private SensorManager gyroSensorManager;
	private Sensor senGyro;
	//private long lastUpdate = 0;
	public boolean toneHoldStatus = false;
	private long lastUpdateG = 0;
	
	//BPM variables
	long milliSecondsElapsed = 0;
	long BPM;
	long taps;
	Boolean isRunning = false;
	CountDownTimer timer;
	Double BPMFactor;

	
	double currentFactor;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_import);
		setTitle("Tap Twist Tunes");
		
		//starting up accelerometer
		senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
		
	    //starting up gyroscope
	    gyroSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    senGyro = senSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	    gyroSensorManager.registerListener(this, senGyro , SensorManager.SENSOR_DELAY_NORMAL);
	    
	    //starting up BPM calc
	    taps = 0;
		BPM = 0;
	    
		tempo = .5;

		browse = (ImageButton) findViewById(R.id.upload);
		play = (ImageButton) findViewById(R.id.bRefresh);
		stop = (ImageButton) findViewById(R.id.stop);
		song = (TextView) findViewById(R.id.songTitle);
		pitchUp = (Button) findViewById(R.id.bPitchUp);
		pitchDown = (Button) findViewById(R.id.bPitchDown);
		toneUp = (Button) findViewById(R.id.bTone);
		timeStretch = (Button) findViewById(R.id.bBPM);
		

		songSelected = false;
		isPlaying = false;
		
		//setting up BPM Timer
		timer = new CountDownTimer(10000, 1000){
			//update necessary variables
			public void onTick(long millisUntilFinished) {
				//timer is running
				isRunning = true;
				//show time remaining as button text
				timeStretch.setText(new Integer((int) (millisUntilFinished/1000)).toString());
		     }
			//timer is finished, calculate total bpm based on taps
		    public void onFinish() {
		    	//timer isn't running
				isRunning = false;
				//take taps and mult by 6
				BPM = (int) (taps * 6);
				//reset text
				timeStretch.setText("BPM");
				
				//this is where we will turn bpm into double factor
				//!!THIS IS WHERE WE SET THE FACTOR TO PASS THE PLAYER
				BPMFactor = (double)BPM/100;
				if(BPMFactor > 2)
					BPMFactor = 2.0;
				else if(BPMFactor <= 0){
					BPMFactor = 0.1;
				}
				
				tempo = BPMFactor - .5;
				wsola.setParameters(Parameters.musicDefaults(tempo, sampleRate));
				
		    }
		};
		
	    timeStretch.setOnClickListener(new View.OnClickListener() {	
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
		
	    toneUp.setOnTouchListener(new View.OnTouchListener() {
	        @Override
			public boolean onTouch(View v, MotionEvent event) {
	            switch(event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	                toneHoldStatus = true;
	                break;
	            case MotionEvent.ACTION_UP:
	                toneHoldStatus = false;
	                break;
	            }
				return false;
			}
	    });
		
		pitchDown.setOnTouchListener(new View.OnTouchListener() {
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
		
	    pitchUp.setOnTouchListener(new View.OnTouchListener() {
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
		

		// this stops the music playing
		stop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dispatcher.stop();
				dispatcher.removeAudioProcessor(rateTransposer);
				dispatcher.removeAudioProcessor(wsola);
			}
		});

		play.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				currentFactor = 1;
				tempo = .5;
				rateTransposer.setFactor(currentFactor);
				wsola.setParameters(Parameters.musicDefaults(tempo, sampleRate));
				
			}
		});

		// open a file browser to select an audio file
		browse.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				browse(v);
			}
		});
	}

	// open the file browser to select a song
	private void browse(View v) {
		// TODO Auto-generated method stub
		Intent browseIntent = new Intent();
		browseIntent.setType("*/*");
		browseIntent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(
				Intent.createChooser(browseIntent, "Open audio file"),
				REQUEST_AUDIO);
	}

	//@SuppressWarnings("deprecation")
	@Override
	public void onActivityResult(int requestCode, int resultCode,
			Intent resultData) {
		if (requestCode == REQUEST_AUDIO
				&& resultCode == Activity.RESULT_OK) {
			browseUri = null;
			if (resultData != null) {
				
				currentFactor = 1;
				tempo = .5;
				browseUri = resultData.getData();
				Cursor returnCursor = getContentResolver().query(browseUri, null, null, null, null);
				int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
				returnCursor.moveToFirst();
				String filename = returnCursor.getString(nameIndex);
				String filenameArray[] = filename.split("\\.");
				String extension = filenameArray[filenameArray.length - 1];
				String titleName = filenameArray[0];
				
				//checking for wave file
				if(extension.matches("mp3")){
					Toast toast = Toast.makeText(getApplicationContext(), "Non-wav files are not yet supported", Toast.LENGTH_LONG);
					toast.show();
					return;
				}
				
				song.setText(titleName);
				
				try {
					wavStream = getContentResolver().openInputStream(browseUri);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("wavStream set successfully");
				audioFormat = new TarsosDSPAudioFormat(44100, 16, 1, true, false);
				audioStream = new UniversalAudioInputStream(wavStream, audioFormat);
				sampleRate = audioFormat.getSampleRate();
				wsola = new WaveformSimilarityBasedOverlapAdd(Parameters.musicDefaults(tempo, sampleRate));
				
				Log.i("****SAMPLE RATE*****", "" + sampleRate);
				
				dispatcher = new AudioDispatcher(audioStream, wsola.getInputBufferSize(), (int)(wsola.getInputBufferSize()/2)); //trying wsola grab
				player = new AndroidAudioPlayer(audioFormat, wsola.getInputBufferSize());
				
				wsola.setDispatcher(dispatcher);
				dispatcher.addAudioProcessor(wsola);
				wsola.setParameters(Parameters.musicDefaults(tempo, sampleRate));
				
				rateTransposer = new RateTransposer(currentFactor);
				dispatcher.addAudioProcessor(rateTransposer);
				dispatcher.addAudioProcessor(player);
				t = new Thread(dispatcher);
				t.start();
				
				songSelected = true;
				isPlaying = true;
				
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSensorChanged(SensorEvent event) {
		//set up sensor
		Sensor mySensor = event.sensor;
		 
		//if accelerometer
	    if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
	    	//get values from sensor
	    	float x = event.values[0];
	        float y = event.values[1];
	        float z = event.values[2];	
	        
	        //get current time
	        long curTime = System.currentTimeMillis();
	        
	        if ((curTime - lastUpdateA) > 150) {
	        	//calculate time since last update
	            long diffTime = (curTime - lastUpdateA);
	            lastUpdateA = curTime;
	            
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
	    
	    //if gyroscope
	    if (mySensor.getType() == Sensor.TYPE_ORIENTATION) {
	    	//get values from sensor
	        float z = event.values[2];	
	        
	        //get current time
	        long curTime = System.currentTimeMillis();
	        
	        if ((curTime - lastUpdateG) > 150) {
	            lastUpdateG = curTime;
	            
	            //shake speed exceeds the threshold set, we need to change the current factor
	            //or variable
	            if (z > 20){
	            	decreaseToneNumber();
	            }
	            else if (z < -20){
	            	increaseToneNumber();
	            }	            
	        }
	    }
		
	}
	
	
	private void changeNumber(){
		
		//increment or decrement current value based on button held
		if(upHoldStatus == true){
			if(currentFactor < 2.0){
				currentFactor = currentFactor + 0.1;
				tempo = tempo + .05;
				rateTransposer.setFactor(currentFactor);
				wsola.setParameters(Parameters.musicDefaults(tempo, sampleRate));
			}
		}
		if(downHoldStatus == true){
			if(currentFactor > 0.1){
				currentFactor = currentFactor - 0.1;
				tempo = tempo - .05;
				rateTransposer.setFactor(currentFactor);
				wsola.setParameters(Parameters.musicDefaults(tempo, sampleRate));
			}
		}
		//if we want this function to return the variable so we can use it in as currentFactor
		//we will have to change references of 'value' to references to the 'currentFactor' variable
	}
	
	
	private void increaseToneNumber(){
			//increment or decrement current value based on button held
			if(toneHoldStatus == true){
				if(currentFactor < 2.00){
					currentFactor = currentFactor + 0.01;
					tempo = tempo + .005;
					rateTransposer.setFactor(currentFactor);
					wsola.setParameters(Parameters.musicDefaults(tempo, sampleRate));
				}
			}
	}
	
	private void decreaseToneNumber(){
		//increment or decrement current value based on button held
		if(toneHoldStatus == true){
			if(currentFactor > .1){
				currentFactor = currentFactor - 0.01;
				tempo = tempo - .005;
				rateTransposer.setFactor(currentFactor);
				wsola.setParameters(Parameters.musicDefaults(tempo, sampleRate));
			}
		}
}
	
	

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) { //this menu here has nothing to do with menu class we created
		// TODO Auto-generated method stub
	    super.onCreateOptionsMenu(menu); //delete return here
	    MenuInflater blowup = getMenuInflater(); 
	    blowup.inflate(R.menu.import_menu, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.controls:
			Intent i = new Intent("com.example.taptwisttunes.CONTROLS");
			startActivity(i);
			break;
		}
		return false; //give no input back
	}
	
	public void onDestroy(){
		super.onDestroy();
	}
}
