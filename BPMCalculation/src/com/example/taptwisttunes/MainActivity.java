 package com.example.taptwisttunes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.example.taptwisttunes.MusicService.MusicBinder;
import com.example.taptwisttunes.MusicService;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;

import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

public class MainActivity extends ActionBarActivity {
	
	private ArrayList<Song> songList;
	private ListView sView;
	
	private MusicService mServe;
	private Intent pIntent;
	private boolean mBound = false;
	
	Button select; //Declaring the selection button
	Button restart; //Declaring restart button
	TextView current; //Declaring the text view for the current playing song
	
	long time1;
	long time2;
	long averageBPM;
	int buttonPress;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        select = (Button) findViewById(R.id.sButton); //initialization of select button for future use
        //restart = (Button) findViewById(R.id.sRestartButton);
        current = (TextView) findViewById(R.id.cPlaying); //initialization of the currently playing text view
        sView = (ListView) findViewById(R.id.sList); //initialization of the selection list
        songList = new ArrayList<Song>(); //initialization of the list array
        mServe = new MusicService();
        time1 = System.currentTimeMillis();
        time2 = System.currentTimeMillis();
        averageBPM = 0;
        buttonPress = 0;
        
        select.setOnClickListener(new View.OnClickListener() { //This function will eventually open up a file browser to select a audio file
			@Override
			public void onClick(View v) {
				if(buttonPress == 4) {
					buttonPress = 0;
					String stringBPM = String.valueOf(averageBPM);
					current.setText("Average BPM: " + stringBPM + "BPM");
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
			     	current.setText("Press button " + numOfPresses + " more times");
				}
				
			}
		});
        
        getSongList(); //Compiling the song list here
        Collections.sort(songList, new Comparator<Song>(){
        	public int compare(Song a, Song b){
        		return a.getTitle().compareTo(b.getTitle());
        	}
        });
        
        SongAdapter sAdapt = new SongAdapter(this, songList);
        sView.setAdapter(sAdapt);
        
    }
    
    protected void onStart(){ //This makes sure the MusicService is started when the main activity starts
    	super.onStart();
    	if(pIntent == null){
    		pIntent = new Intent(this, MusicService.class);
    		bindService(pIntent, musicConnection, Context.BIND_AUTO_CREATE);
    		startService(pIntent);
    	}
    }
    
    private ServiceConnection musicConnection = new ServiceConnection(){
    	@Override
    	public void onServiceConnected(ComponentName name, IBinder service){
    		MusicBinder binder = (MusicBinder) service; 
    		mServe = binder.getService(); //this gets the service
    		// mServe.setList(songList); //this passes the list to service
    		mBound = true;
    	}
    	@Override
    	public void onServiceDisconnected(ComponentName name){
    		mBound = false;
    	}
    };
    
    public void getSongList(){ //this method will compile the song list
    	ContentResolver mResolver = getContentResolver(); //this creates a content resolver instance
    	Uri mUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI; //this creates a URI that will retrieve song info
    	Cursor mCursor = mResolver.query(mUri, null, null, null, null); //this creates the cursor to sift through the device storage
    	
    	if(mCursor != null && mCursor.moveToFirst()){
    		int tColumn = mCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE); //retrieving the song title
    		int iColumn = mCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID); //retrieving the song id
    		int aColumn = mCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST); //retrieving the artist
    		
    		do{ //this will add songs to the list
    			long thisId = mCursor.getLong(iColumn);
    			String thisTitle = mCursor.getString(tColumn);
    			String thisArtist = mCursor.getString(aColumn);
    			songList.add(new Song(thisId, thisTitle, thisArtist));
    		}
    		while (mCursor.moveToNext()); //moves the cursor to the next song
    		
    		System.out.println("setList function about to run");
    		mServe.setList(songList);
    		
    	}
    	
    }

    public void songPicked(View view){
    	System.out.println(view.getTag().toString());
    	mServe.setSong(Integer.parseInt(view.getTag().toString()));
    	mServe.playSong(); 
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
        case R.id.action_end:
        	stopService(pIntent); //this stops a service instance
        	mServe = null;
        	System.exit(0); //this ends the app
        	break;
        }
       
    	/*int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    protected void onDestroy(){
    	stopService(pIntent);
    	mServe = null;
    	super.onDestroy();
    }
}
