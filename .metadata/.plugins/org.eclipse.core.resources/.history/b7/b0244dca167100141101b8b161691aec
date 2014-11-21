package com.example.taptwisttunes;

import java.io.IOException;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Import extends ActionBarActivity {

	Button browse, stop;
	TextView text;
	MediaPlayer mediaPlayer;
	private static final int READ_REQUEST_CODE = 42;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import);
		
		browse = (Button) findViewById(R.id.bBrowser);
		stop = (Button) findViewById(R.id.bStop);
		text = (TextView) findViewById(R.id.bText);
		
      //This function opens up a file browser to select an audio file
		browse.setOnClickListener(new View.OnClickListener() {
		
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				browse(v);
			}
		});
		
        //this stops the music playing 
		stop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				text.setText("working button");
				if (mediaPlayer!=null) {
					mediaPlayer.release();
					mediaPlayer = null;
				}
			}
		});
	}
		
	// open the file browser to select a song
	private void browse(View v) {
		// TODO Auto-generated method stub
		Intent browseIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		browseIntent.addCategory(Intent.CATEGORY_OPENABLE);
		browseIntent.setType("*/*");
		startActivityForResult(browseIntent, READ_REQUEST_CODE);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
	    if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
	        // The document selected by the user won't be returned in the intent.
	        // Instead, a URI to that document will be contained in the return intent
	        // provided to this method as a parameter.
	        // Pull that URI using resultData.getData().
	        Uri browseUri = null;
	        if (resultData != null) {
	        	browseUri = resultData.getData();
	        	text.setText(browseUri.getLastPathSegment());
	        	if(mediaPlayer != null) mediaPlayer.release();
	        	mediaPlayer = new MediaPlayer();
	        	mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	        	try {
					mediaPlayer.setDataSource(getApplicationContext(), browseUri);
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
	            //song plays
	            mediaPlayer.start();
	        }
	    }
	}
}
