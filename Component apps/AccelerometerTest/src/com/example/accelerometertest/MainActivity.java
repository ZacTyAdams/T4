package com.example.accelerometertest;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

//imports from tarsos example
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.GainProcessor;
import be.tarsos.dsp.MultichannelToMono;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd.Parameters;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.io.jvm.WaveformWriter;
import be.tarsos.dsp.resample.RateTransposer;
//end tarsos imports



public class MainActivity extends ActionBarActivity implements SensorEventListener{

	//from tutorial
	private SensorManager senSensorManager;
	private Sensor senAccelerometer;
	
	//from tutorial
	private long lastUpdate = 0;
	Button hold;
	public boolean holdStatus = false;
	private float last_x, last_y, last_z;
	private static final int SHAKE_THRESHOLD = 600;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//from tutorial
		senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
	
	    hold = (Button) findViewById(R.id.button1);
	    hold.setOnTouchListener(new OnTouchListener() {
	        @Override
			public boolean onTouch(View v, MotionEvent event) {
	            switch(event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	                holdStatus = true;
	                break;
	            case MotionEvent.ACTION_UP:
	                holdStatus = false;
	                break;
	            }
				return false;
			}
	    });
	    
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		//all from tutorial
		 Sensor mySensor = event.sensor;
		 
		    if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
		    	float x = event.values[0];
		        float y = event.values[1];
		        float z = event.values[2];	
		        
		        long curTime = System.currentTimeMillis();
		        
		        if ((curTime - lastUpdate) > 100) {
		            long diffTime = (curTime - lastUpdate);
		            lastUpdate = curTime;
		            
		            float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;
		            
		            if (speed > SHAKE_THRESHOLD) {
		            	incrementNumber();
		            }
		            
		            last_x = x;
		            last_y = y;
		            last_z = z;
		        }
		    }
		
	}
	
	//from tutorial
	protected void onPause() {
	    super.onPause();
	    senSensorManager.unregisterListener(this);
	}
	
	//from tutorial
	protected void onResume() {
	    super.onResume();
	    senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private void incrementNumber(){
		
		TextView display = (TextView) findViewById(R.id.display);
	
		String curText = (String) display.getText();
	
		if (isInteger(curText)){
			if(holdStatus == true){
				Integer value = Integer.parseInt(curText);
				value++;
				display.setText(value.toString());
			}
		}
		else{
			if(holdStatus == true){
			Integer value = 0;
			display.setText(value.toString());
			}
		}
	}
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    // only got here if we didn't return false
	    return true;
	}
	
	//beginning of functions from example program... these functions not implemented
	/*
	public void PitchShiftingExample(){
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Pitch shifting: change the pitch of your audio.");
		
		originalTempoCheckBox = new JCheckBox("Keep original tempo?", true);
		originalTempoCheckBox.addChangeListener(parameterSettingChangedListener);
		
		currentFactor = 1;
		
		factorSlider = new JSlider(20, 250);
		factorSlider.setValue(100);
		factorSlider.setPaintLabels(true);
		factorSlider.addChangeListener(parameterSettingChangedListener);
		
		JPanel fileChooserPanel = new JPanel(new BorderLayout());
		fileChooserPanel.setBorder(new TitledBorder("1... Or choose your audio (wav mono)"));
		
		fileChooser = new JFileChooser();
		
		JButton chooseFileButton = new JButton("Choose a file...");
		chooseFileButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fileChooser.showOpenDialog(PitchShiftingExample.this);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                File file = fileChooser.getSelectedFile();
	                startFile(file,null);
	            } else {
	                //canceled
	            }
			}			
		});
		fileChooserPanel.add(chooseFileButton);
		fileChooser.setLayout(new BoxLayout(fileChooser, BoxLayout.PAGE_AXIS));
		

		JPanel inputSubPanel = new JPanel(new BorderLayout());
		JPanel inputPanel = new InputPanel();
		inputPanel.addPropertyChangeListener("mixer",
				new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent arg0) {
						startFile(null,(Mixer) arg0.getNewValue());
					}
				});
		inputSubPanel.add(inputPanel,BorderLayout.NORTH);
		inputSubPanel.add(fileChooserPanel,BorderLayout.SOUTH);
		
		gainSlider = new JSlider(0,200);
		gainSlider.setValue(100);
		gainSlider.setPaintLabels(true);
		gainSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (PitchShiftingExample.this.dispatcher != null) {
					double gainValue = gainSlider.getValue() / 100.0;
					gain.setGain(gainValue);
				}
			}
		});
		
		
		JPanel params = new JPanel(new BorderLayout());
		params.setBorder(new TitledBorder("2. Set the algorithm parameters"));
		
		JLabel label = new JLabel("Factor 100%");
		label.setToolTipText("The pitch shift factor in % (100 is no change, 50 is double pitch, 200 half).");
		factorLabel = label;
		params.add(label,BorderLayout.NORTH);
		params.add(factorSlider,BorderLayout.CENTER);
		
		JPanel subPanel = new JPanel(new GridLayout(2, 2));
			
		centsSpinner = new JSpinner();
		centsSpinner.addChangeListener(parameterSettingChangedListener);
		label = new JLabel("Pitch shift in cents");
		label.setToolTipText("Pitch shift in cents.");
		subPanel.add(label);
		subPanel.add(centsSpinner);

		label = new JLabel("Keep original tempo");
		label.setToolTipText("Pitch shift in cents.");
		subPanel.add(label);
		subPanel.add(originalTempoCheckBox);
		
		params.add(subPanel,BorderLayout.SOUTH);
		
		JPanel gainPanel = new JPanel(new BorderLayout());
		label = new JLabel("Gain (in %)");
		label.setToolTipText("Volume in % (100 is no change).");
		gainPanel.add(label,BorderLayout.NORTH);
		gainPanel.add(gainSlider,BorderLayout.CENTER);
		gainPanel.setBorder(new TitledBorder("3. Optionally change the volume"));
		
		this.add(inputSubPanel,BorderLayout.NORTH);
		this.add(params,BorderLayout.CENTER);
		this.add(gainPanel,BorderLayout.SOUTH);
		
	}
	
	public static double centToFactor(double cents){
		return 1 / Math.pow(Math.E,cents*Math.log(2)/1200/Math.log(Math.E)); 
	}
	private static double factorToCents(double factor){
		return 1200 * Math.log(1/factor) / Math.log(2); 
	}
	private static void startCli(String source,String target,double cents) throws UnsupportedAudioFileException, IOException{
		File inputFile = new File(source);
		AudioFormat format = AudioSystem.getAudioFileFormat(inputFile).getFormat();	
		double sampleRate = format.getSampleRate();
		double factor = PitchShiftingExample.centToFactor(cents);
		RateTransposer rateTransposer = new RateTransposer(factor);
		WaveformSimilarityBasedOverlapAdd wsola = new WaveformSimilarityBasedOverlapAdd(Parameters.musicDefaults(factor, sampleRate));
		WaveformWriter writer = new WaveformWriter(format,target);
		AudioDispatcher dispatcher;
		if(format.getChannels() != 1){
			dispatcher = AudioDispatcherFactory.fromFile(inputFile,wsola.getInputBufferSize() * format.getChannels(),wsola.getOverlap() * format.getChannels());
			dispatcher.addAudioProcessor(new MultichannelToMono(format.getChannels(),true));
		}else{
			dispatcher = AudioDispatcherFactory.fromFile(inputFile,wsola.getInputBufferSize(),wsola.getOverlap());
		}
		wsola.setDispatcher(dispatcher);
		dispatcher.addAudioProcessor(wsola);
		dispatcher.addAudioProcessor(rateTransposer);
		dispatcher.addAudioProcessor(writer);
		dispatcher.run();
	}	
	*/
}
