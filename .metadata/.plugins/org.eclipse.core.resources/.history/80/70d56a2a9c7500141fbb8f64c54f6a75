package com.example.taptwisttunes;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SongAdapter extends BaseAdapter {
	
	private ArrayList<Song> songs; //Song list from main activity
	private LayoutInflater songInf; //used to map the title and artist to layout textviews
	
	public SongAdapter(Context c, ArrayList<Song> theSongs){ //initializes the variables
		songs = theSongs;
		songInf = LayoutInflater.from(c); 
	}

	@Override
	public int getCount() {
		return songs.size(); //edited from return 0 to return number of songs
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout songLay = (LinearLayout)songInf.inflate(R.layout.song, parent, false); //this maps the song to the layout
		
		TextView songView = (TextView)songLay.findViewById(R.id.song_title); //this retrieves the title view
		TextView artistView = (TextView)songLay.findViewById(R.id.song_artist); //this retrieves the artist view
		
		Song currSong = songs.get(position); //this is used to get the current song using position
		
		songView.setText(currSong.getTitle()); //setting the current song title in the text view
		artistView.setText(currSong.getArtist()); //setting the current song artist in the text view
		
		songLay.setTag(position); //this sets the tag (no idea what that is)
		return songLay;
	}

}
