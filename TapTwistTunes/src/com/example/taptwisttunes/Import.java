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
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Import extends ActionBarActivity {

	ImageButton browse, stop, play, pause;
	SeekBar pitch, tempoS;
	TextView song;
	MediaPlayer mediaPlayer;
	Uri browseUri = null;
	private MediaPlayer mSilentPlayer; /* to avoid tunnel player issue */
	int currentSongPosition = 0;
	private VisualizerView mVisualizerView;
	static final int REQUEST_AUDIO = 1;
	boolean songSelected; // used to stop play from working until a song is
							// selected
	boolean isPlaying;
	
	private AudioDispatcher dispatcher;
	private WaveformSimilarityBasedOverlapAdd wsola;
	private RateTransposer rateTransposer;
	private double currentRate;
	private double tempo;
	private double sampleRate;
	InputStream wavStream;
	private Thread t;
	
	double currentFactor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_import);
		tempo = .5;

		browse = (ImageButton) findViewById(R.id.upload);
		play = (ImageButton) findViewById(R.id.play);
		pause = (ImageButton) findViewById(R.id.pause);
		stop = (ImageButton) findViewById(R.id.stop);
		song = (TextView) findViewById(R.id.songTitle);
		pitch = (SeekBar) findViewById(R.id.seekBar1);
		tempoS = (SeekBar) findViewById(R.id.seekBar2);

		songSelected = false;
		isPlaying = false;
		
		song.setText("" + pitch.getProgress());
		
		pitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				//song.setText("" + pitch.getProgress());
				rateTransposer.setFactor(currentFactor);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				//song.setText("" + pitch.getProgress());
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(pitch.getProgress() == 0){
					currentFactor = (double)(pitch.getProgress() + 10)/10;
					//tempo = tempo - .7;
				}
				else{
					currentFactor = (double)pitch.getProgress()/10;
				}
				song.setText("" + currentFactor);
			}
		});
		
		tempoS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				wsola.setParameters(Parameters.musicDefaults(tempo, sampleRate));
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(tempoS.getProgress() == 0){
					tempo = (double)(tempoS.getProgress() + 10)/100;
				}
				else{
					tempo = (double)tempoS.getProgress()/100;
				}
				song.setText("" + tempo);				
			}
		});

		// this stops the music playing
		stop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mediaPlayer != null) {
					cleanUp();
					currentSongPosition = 0;
					isPlaying = false;
				}
			}
		});

		// releases the media player and saves the last know location of the
		// song
		pause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*if (mediaPlayer != null) {
					if (mediaPlayer.isPlaying()) {
						currentSongPosition = mediaPlayer.getCurrentPosition();
						cleanUp();
						isPlaying = false;
					}
				}*/
				
				currentFactor = currentFactor -.1;
				rateTransposer.setFactor(currentFactor);
			}
		});

		// mediaPlayer.pause() wasn't working occasionally so instead the
		// mediaPlayer is released on
		// pause and reopened on play at the location it left off. It's been
		// tested and there is no
		// lag or issues with this way.
		play.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				/*if (songSelected && !isPlaying) { // if no song can't press play
					// TODO Auto-generated method stub
					initTunnelPlayerWorkaround();
					if (mediaPlayer != null) {
						mediaPlayer.release();
						mediaPlayer = null;
						mVisualizerView.clearRenderers();
						mVisualizerView.release();
					}
					mVisualizerView = (VisualizerView) findViewById(R.id.visualizerView);
					addBarGraphRenderers();
					mediaPlayer = new MediaPlayer();
					mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					try {
						mediaPlayer.setDataSource(getApplicationContext(),
								browseUri);
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					try {
						mediaPlayer.prepare();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// song plays
					mediaPlayer.start();
					mediaPlayer.seekTo(currentSongPosition);
					mVisualizerView.setEnabled(true);
					mVisualizerView.link(mediaPlayer);
					isPlaying = true;
				}*/
				
				currentFactor = currentFactor +.1;
				rateTransposer.setFactor(currentFactor);
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
				initTunnelPlayerWorkaround();
				currentFactor = 1;
				tempo = .5;
				browseUri = resultData.getData();
				Cursor returnCursor = getContentResolver().query(browseUri, null, null, null, null);
				int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
				returnCursor.moveToFirst();
				song.setText(returnCursor.getString(nameIndex));
				
				try {
					wavStream = getContentResolver().openInputStream(browseUri);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("wavStream set successfully");
				TarsosDSPAudioFormat audioFormat = new TarsosDSPAudioFormat(44100, 16, 1, true, false);
				UniversalAudioInputStream audioStream = new UniversalAudioInputStream(wavStream, audioFormat);
				sampleRate = audioFormat.getSampleRate();
				wsola = new WaveformSimilarityBasedOverlapAdd(Parameters.musicDefaults(tempo, sampleRate));
				
				Log.i("****SAMPLE RATE*****", "" + sampleRate);
				
				AudioDispatcher dispatcher = new AudioDispatcher(audioStream, wsola.getInputBufferSize(), (int)(wsola.getInputBufferSize()/2)); //trying wsola grab
				AndroidAudioPlayer player = new AndroidAudioPlayer(audioFormat, wsola.getInputBufferSize());
				
				wsola.setDispatcher(dispatcher);
				dispatcher.addAudioProcessor(wsola);
				wsola.setParameters(Parameters.musicDefaults(tempo, sampleRate));
				
				rateTransposer = new RateTransposer(currentFactor);
				dispatcher.addAudioProcessor(rateTransposer);
				dispatcher.addAudioProcessor(player);
				t = new Thread(dispatcher);
				t.start();
				
				
				if (mediaPlayer != null) {
					cleanUp();
				}
				mVisualizerView = (VisualizerView) findViewById(R.id.visualizerView);
				addBarGraphRenderers();
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				try {
					mediaPlayer.setDataSource(getApplicationContext(),
							browseUri);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					mediaPlayer.prepare();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// song plays
				//mediaPlayer.start();
				//mediaPlayer.setVolume(0, 0); //trying to silent media player for the visualizer
				mVisualizerView.setEnabled(true);
				// We need to link the visualizer view to the media player so
				// that
				// it displays something
				mVisualizerView.link(mediaPlayer);
				songSelected = true;
				isPlaying = true;
				
			}
		}
	}

	// Trying to get the song title
	/*
	 * private void getTrackInfo(Uri audioFileUri) { MediaMetadataRetriever
	 * metaRetriever= new MediaMetadataRetriever();
	 * metaRetriever.setDataSource(getRealPathFromURI(audioFileUri)); String
	 * title =
	 * metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
	 * song.setText(title); }
	 * 
	 * 
	 * private String getRealPathFromURI(Uri browseUri) { File myFile = new
	 * File(browseUri.getPath().toString()); String s =
	 * myFile.getAbsolutePath(); return s; }
	 */

	private void cleanUp() {
		if (mediaPlayer != null) {
			mVisualizerView.release();
			mediaPlayer.release();
			mediaPlayer = null;
		}

		if (mSilentPlayer != null) {
			mSilentPlayer.release();
			mSilentPlayer = null;
		}
	}

	// This is the work around for the galaxy S4
	private void initTunnelPlayerWorkaround() {
		// Read "tunnel.decode" system property to determine
		// the workaround is needed
		//if (TunnelPlayerWorkaround.isTunnelDecodeEnabled(this)) {
			mSilentPlayer = TunnelPlayerWorkaround
					.createSilentMediaPlayer(this);
		//}
	}

	// Method for adding the bar graph visualizer to the the visualizer view
	private void addBarGraphRenderers() {
		Paint paint = new Paint();
		paint.setStrokeWidth(50f);
		paint.setAntiAlias(true);
		paint.setColor(Color.argb(200, 46, 204, 113));
		BarGraphRenderer barGraphRendererBottom = new BarGraphRenderer(16,
				paint, false);
		mVisualizerView.addRenderer(barGraphRendererBottom);

		Paint paint2 = new Paint();
		paint2.setStrokeWidth(50f);
		paint2.setAntiAlias(true);
		paint2.setColor(Color.argb(200, 231, 76, 60));
		BarGraphRenderer barGraphRendererTop = new BarGraphRenderer(16, paint2,
				true);
		mVisualizerView.addRenderer(barGraphRendererTop);
	}
}
