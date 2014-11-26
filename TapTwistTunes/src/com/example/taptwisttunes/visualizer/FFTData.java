/**
 * Copyright 2011, Felix Palmer
 */

//Gets sent to visualizer and to create the bar renderer based on the bytes of the song
package com.example.taptwisttunes.visualizer;

// Data class to explicitly indicate that these bytes are the FFT of audio data
public class FFTData {
	public FFTData(byte[] bytes) {
		this.bytes = bytes;
	}

	public byte[] bytes;
}
