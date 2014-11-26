package com.example.taptwisttunes;

import java.io.File;
import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class Recorder extends ActionBarActivity {

	Button play, record, stop;
	TextView display;
	MediaRecorder mRecorder;
	MediaPlayer mPlayer;
	String mediaFile = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_recorder);

		// initialize all the buttons
		record = (Button) findViewById(R.id.bRecord);
		stop = (Button) findViewById(R.id.bStop);
		play = (Button) findViewById(R.id.bPlay);
		display = (TextView) findViewById(R.id.actionText);

		// create TapTwistTunes folder in SD card and store recordings in it
		String newFolder = "/TapTwistTunes";
		String extStorageDirectory = Environment.getExternalStorageDirectory()
				.toString();
		File myNewFolder = new File(extStorageDirectory + "/" + newFolder);
		myNewFolder.mkdir();
		mediaFile = Environment.getExternalStorageDirectory().toString() + "/"
				+ newFolder + "/myRecordings.mp3";

		// disable buttons that can't use until we record
		stop.setEnabled(false);
		play.setEnabled(false);
		// start recording when I click record button
		record.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				display.setText("RECORDING AUDIO");
				record(v);
			}
		});

		// stop recording when I click the first stop button
		stop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				display.setText("AUDIO RECORDED");
				stopRec(v);
			}
		});

		// play recorded audio when I click play button
		play.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				display.setText("PLAYING THE RECORDING");
				play(v);
			}
		});
	}

	// call to record audio
	public void record(View v) {
		// TODO Auto-generated method stub
		// create new instance of MediaRecorder class
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		mRecorder.setOutputFile(mediaFile);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		// exception handler
		try {
			mRecorder.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mRecorder.start();
		// enable the stopRec button and disable the rest
		stop.setEnabled(true);
		record.setEnabled(false);
		play.setEnabled(false);
	}

	// call to play recorded audio
	public void play(View v) {
		// TODO Auto-generated method stub
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mediaFile);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// enable the stopPlay button and disable the rest
		if (mPlayer == null) {
			play.setEnabled(true);
			record.setEnabled(true);
		}
		play.setEnabled(false);
		stop.setEnabled(true);
		record.setEnabled(false);
		// stopRec.setEnabled(false);
	}

	// call to stop recording audio
	public void stopRec(View v) {
		// TODO Auto-generated method stub
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
			// enable play and record button and disable the rest
			stop.setEnabled(false);
			record.setEnabled(true);
			play.setEnabled(true);
		} else if (mPlayer != null) {
			// call to stop playing audio
			mPlayer.release();
			mPlayer = null;
			// enable the playButton and record and disable the rest
			stop.setEnabled(false);
			play.setEnabled(true);
			record.setEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recorder, menu);
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
