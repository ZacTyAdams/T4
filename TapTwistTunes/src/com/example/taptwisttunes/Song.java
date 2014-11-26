package com.example.taptwisttunes;

//The function of this class is to model audio file id to be read in by the ListView

public class Song {
	private long id;
	private String title;
	private String artist;

	public Song(long songID, String songTitle, String songArtist) { // this
																	// method
																	// assigns
																	// the
																	// variables
																	// a value
		id = songID;
		title = songTitle;
		artist = songArtist;
	}

	// these methods retrieve the information of the audio files
	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getArtist() {
		return artist;
	}

}
