/**
 * Copyright 2011, Felix Palmer
 */

package com.example.taptwisttunes.visualizer;

// Data class to explicitly indicate that these bytes are raw audio data
public class AudioData {
	public AudioData(byte[] bytes) {
		this.bytes = bytes;
	}

	public byte[] bytes;
}
