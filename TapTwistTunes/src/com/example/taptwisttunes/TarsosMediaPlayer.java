package com.example.taptwisttunes;

import java.io.File; //may or may not be used

import android.support.v7.app.ActionBarActivity;
import android.media.AudioFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import be.tarsos.dsp.AudioDispatcher;
//import be.tarsos.dsp.BlockingAudioPlayer;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd.Parameters;

//import be.tarsos.dsp.WaveformWriter;

public class TarsosMediaPlayer extends ActionBarActivity implements
		OnSeekBarChangeListener {

	int prog;
	SeekBar tempo;
	TextView Value;
	Button Start;
	Button Stop;
	private AudioDispatcher dispatcher;
	private WaveformSimilarityBasedOverlapAdd wsola;
	MediaPlayer TestSound;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tarsos_media_player);
		tempo = (SeekBar) findViewById(R.id.TempoSlider);
		Value = (TextView) findViewById(R.id.Value);
		TestSound = MediaPlayer.create(this, R.raw.loudpipes);
		Start = (Button) findViewById(R.id.StartSongt);
		Stop = (Button) findViewById(R.id.StopSongT);
		Start.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				beginS();

			}
		});

		Stop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				stopS();

			}
		});

		tempo.setOnSeekBarChangeListener(this);
	}

	public void beginS() {
		TestSound.start();
		String musicUri = TestSound.toString();
		System.out.println(musicUri);
		File Tester = new File(musicUri);
		System.out.println(Tester);// THIS DOES WORK
		if (dispatcher != null) {
			dispatcher.stop();
		}
		AudioFormat format;
		// try{
		// format
		// wsola = new WaveformSimilarityBasedOverlapAdd()
		// }
		// wsola.setParameters();
		// dispatcher =
		// AudioDispatcher.fromFile(Tester,wsola.getInputBufferSize(),wsola.getOverlap());

		/*
		 * if(dispatcher != null){ TRY MOVING THIS TO THE ON PROGRESS CHANGE
		 * METHOD dispatcher.stop(); } AudioFormat format; try { //format =
		 * AudioSystem.getAudioFileFormat(Tester).getFormat(); wsola = new
		 * WaveformSimilarityBasedOverlapAdd
		 * (format,Parameters.slowdownDefaults(tempo
		 * .getValue()/100.0,format.getSampleRate())); try {
		 * wsola.setBlockingAudioPlayer(new BlockingAudioPlayer(format,
		 * wsola.getOutputBufferSize(),0)); } catch (LineUnavailableException e)
		 * { // TODO Auto-generated catch block e.printStackTrace(); }
		 * dispatcher =
		 * AudioDispatcher.fromFile(inputFile,wsola.getInputBufferSize
		 * (),wsola.getOverlap());
		 * 
		 * wsola.setDispatcher(dispatcher); dispatcher.addAudioProcessor(wsola);
		 * Thread t = new Thread(dispatcher); t.start(); } catch
		 * (UnsupportedAudioFileException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
	}

	public void stopS() {
		TestSound.stop();
		TestSound.release();
		TestSound = MediaPlayer.create(this, R.raw.loudpipes);
	}

	@Override
	public void onProgressChanged(SeekBar seekbar, int progress,
			boolean fromUser) {
		prog = progress;
		Value.setText("Your Badass Level is:" + prog);
		wsola.setParameters(new Parameters(prog / 50.0, 44100, 82, 28, 12));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tarsos_media_player, menu);
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
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}
}
