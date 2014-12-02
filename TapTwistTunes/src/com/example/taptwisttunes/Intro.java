package com.example.taptwisttunes;

import java.io.BufferedInputStream;
import java.io.InputStream;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd.Parameters;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.resample.RateTransposer;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class Intro extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// this code hides the action bar for this activity
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.intro);
		setTitle("");
		
	}

	public void gotoFileBrowser(View view) {

		Intent intent = new Intent(this, Import.class);
		startActivity(intent);
	}

	public void gotoRecorder(View view) {

		Intent intent = new Intent(this, Recorder.class);
		startActivity(intent);
	}

}
