package com.example.tarsostesting;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
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
import android.app.Activity;
import android.media.AudioFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	Button bPlay;
	Button bPause;
	Button incButton;
	Button decButton;
	TextView dText;
	SeekBar seekBar;
	MediaPlayer mpAudio;
	private AudioDispatcher dispatcher;
	private WaveformSimilarityBasedOverlapAdd wsola;
	private RateTransposer rateTransposer;
	private SoundTouchRateTransposer strateTransposer;
	private double currentFactor;// pitch shift factor
	private double sampleRate;
	private double tempo;
	private Thread t;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		LinearLayout layout = new LinearLayout(this);
		bPlay = new Button(this);
		bPlay.setText("Play");
		bPlay.setOnClickListener(this);
		bPause = new Button(this);
		bPause.setText("Pause");
		bPause.setOnClickListener(this);
		dText = new TextView(this);
		dText.setText("1");
		incButton = new Button(this);
		incButton.setText("+");
		incButton.setOnClickListener(this);
		decButton = new Button(this);
		decButton.setText("-");
		decButton.setOnClickListener(this);
		
		layout.addView(bPlay);
		layout.addView(bPause);
		layout.addView(dText);
		layout.addView(incButton);
		layout.addView(decButton);
		setContentView(layout);
		
		
		//mpAudio = MediaPlayer.create(this, R.raw.dreaming_converted);
		
		//AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
		
		
		//beginning of new test stuff
		
		InputStream wavStream;
		wavStream = new BufferedInputStream(getResources().openRawResource(R.raw.last_dance_mono));
		
		System.out.println(wavStream.toString());
		
		//this line is probably wrong, try with variations
		TarsosDSPAudioFormat audioFormat = new TarsosDSPAudioFormat(44100, 16, 1, true, false);
		
		UniversalAudioInputStream audioStream = new UniversalAudioInputStream(wavStream,audioFormat);
		//UniversalAudioInputStream audioStream = new UniversalAudioInputStream(wavStream,audioFormat);
		
		currentFactor = 1;
		tempo = .5;
		sampleRate = audioFormat.getSampleRate();
		Log.i("****SAMPLE RATE*****", "" + sampleRate);
		wsola = new WaveformSimilarityBasedOverlapAdd(Parameters.musicDefaults(tempo, sampleRate));
		
		System.out.println(wsola.getInputBufferSize());
		System.out.println(wsola.getOverlap());
		System.out.println(wsola.getOutputBufferSize());
		
		
		try {
			dispatcher = AudioDispatcherFactory.fromFile(audioStream, wsola.getInputBufferSize(), (int)(wsola.getInputBufferSize()/2));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//AudioDispatcher dispatcher = new AudioDispatcher(audioStream, wsola.getInputBufferSize(), (wsola.getInputBufferSize()/2)); //trying wsola grab
		AndroidAudioPlayer player = new AndroidAudioPlayer(audioFormat, wsola.getInputBufferSize());
		
		rateTransposer = new RateTransposer(currentFactor);
		
		wsola.setDispatcher(dispatcher);
		dispatcher.addAudioProcessor(wsola);
		dispatcher.addAudioProcessor(rateTransposer);
		dispatcher.addAudioProcessor(player);
		
		t = new Thread(dispatcher);
		t.start();
		
        /*dispatcher.run();
        try {
            audioStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
		
		
		
		/////THIS WORKS AS A PITCH DETECTOR 
		/*
		AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);
		dispatcher.addAudioProcessor(new PitchProcessor(PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, new PitchDetectionHandler() {
			
			@Override
			public void handlePitch(PitchDetectionResult pitchDetectionResult,
					AudioEvent audioEvent) {
				final float pitchInHz = pitchDetectionResult.getPitch();
				runOnUiThread(new Runnable() {
				     @Override
				     public void run() {
						dText.setText("" + pitchInHz);
				    }
				});
				
			}
		}));
		new Thread(dispatcher,"Audio Dispatcher").start();*/
		
		
		
		
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
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == bPlay){
			tempo = tempo - .1;
			wsola.setParameters(Parameters.musicDefaults(tempo, sampleRate));
			dText.setText("" + tempo);
		}
		else if(v == bPause){
			tempo = tempo + .1;
			wsola.setParameters(Parameters.musicDefaults(tempo, sampleRate));
			dText.setText("" + tempo);
		}
		else if(v==incButton){
			if(currentFactor != 0.1){
				currentFactor = currentFactor - 0.1;
				tempo = tempo - .06;
			}
			rateTransposer.setFactor(currentFactor);
			wsola.setParameters(Parameters.musicDefaults(tempo, sampleRate));
			dText.setText("" + currentFactor);
		}
		else if(v==decButton){
			currentFactor = currentFactor + 0.1;
			tempo = tempo + .06;
			rateTransposer.setFactor(currentFactor);
			wsola.setParameters(Parameters.musicDefaults(tempo, sampleRate));
			dText.setText("" + currentFactor);
		}
		
	}
	
	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
}
