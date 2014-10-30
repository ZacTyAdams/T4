package com.example.taptwisttunes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.example.taptwisttunes.MusicService.MusicBinder;

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
import android.app.Activity;

import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

public class AudioPlayer extends ActionBarActivity {
	
	private ArrayList<Song> songList;
	private ListView songView;
	
	private MusicService musicSrv;
	private Intent playIntent;
	private boolean musicBound = false; // not sure where this is needed or even used -z
	static final int REQUEST_AUDIO_MP3 = 1;
	
	Button select; //Declaring the selection button
	TextView current; //Declaring the text view for the current playing song
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        select = (Button) findViewById(R.id.sButton); //initialization of select button for future use
        current = (TextView) findViewById(R.id.cPlaying); //initialization of the currently playing text view
        songView = (ListView) findViewById(R.id.song_list); //initialization of the selection list
        songList = new ArrayList<Song>(); //initialization of the list array
        getSongList();
        
        Collections.sort(songList, new Comparator<Song>(){
			public int compare(Song a, Song b){
				return a.getTitle().compareTo(b.getTitle());
			}
		});
        
        SongAdapter songAdt = new SongAdapter(this, songList);
		songView.setAdapter(songAdt);
        
        select.setOnClickListener(new View.OnClickListener() { //This function opens up a file browser to select an audio file
		
			@Override
			public void onClick(View v) {
				Intent browseIntent = new Intent();
			    	browseIntent.setType("audio/mp3");
				browseIntent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(browseIntent, "Open audio file"), REQUEST_AUDIO_MP3);
			}
	}); 
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == REQUEST_AUDIO_MP3 && resultCode == RESULT_OK) {
    		Uri audioFileUri = data.getData();
    		String currentSong; //just trying something
    		current.setText(audioFileUri.getLastPathSegment()); //display song name 
        	//current.setText(Integer.parseInt(view.getTag().getTitle()));
        	musicSrv.setSong(audioFileUri.getPort());
        	currentSong = audioFileUri.getLastPathSegment(); 
        	currentSong = musicSrv.playSong();
    	}
    }
    
    private ServiceConnection musicConnection = new ServiceConnection(){
    	@Override
    	public void onServiceConnected(ComponentName name, IBinder service){
    		MusicBinder binder = (MusicBinder) service; 
    		musicSrv = binder.getService(); //this gets the service
    		musicSrv.setList(songList); //this passes the list to service
    		musicBound = true;
    	}
    	@Override
    	public void onServiceDisconnected(ComponentName name){
    		musicBound = false;
    	}
    };
    
    
    protected void onStart(){ //This makes sure the MusicService is started when the main activity starts
    	super.onStart();
    	if(playIntent == null){
    		playIntent = new Intent(this, MusicService.class);
    		bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
    		startService(playIntent);
    	}
    }
    
    public void songPicked(View view){
    	String curSon;
    	System.out.println(view.getTag().toString());
    	//current.setText(Integer.parseInt(view.getTag().getTitle()));
    	musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
    	curSon = musicSrv.playSong();
    	current.setText(curSon); //sets the current song playing
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
        	stopService(playIntent); //this stops a service instance
        	musicSrv = null;
        	System.exit(0); //this ends the app
        	break;
        }
       
        return super.onOptionsItemSelected(item);
    }
    
    public void getSongList(){ //this method will compile the song list
    	ContentResolver musicResolver = getContentResolver(); //this creates a content resolver instance
    	Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI; //this creates a URI that will retrieve song info
    	Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null); //this creates the cursor to sift through the device storage
    	
    	if(musicCursor != null && musicCursor.moveToFirst()){
    		int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE); //retrieving the song title
    		int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID); //retrieving the song id
    		int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST); //retrieving the artist
    		
    		do {
				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				songList.add(new Song(thisId, thisTitle, thisArtist));
			} 
			while (musicCursor.moveToNext());
    		
    	}
    	
    }
    

    
    protected void onDestroy(){
    	stopService(playIntent);
    	musicSrv = null;
    	super.onDestroy();
    }
}
