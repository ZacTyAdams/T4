/*
*      _______                       _____   _____ _____  
*     |__   __|                     |  __ \ / ____|  __ \ 
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
*                                                         
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*  
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*  
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
* 
*/

package be.tarsos.dsp.example;

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

public class PitchShiftingExample extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3830419374132803358L;

	private JFileChooser fileChooser;

	private AudioDispatcher dispatcher;
	private WaveformSimilarityBasedOverlapAdd wsola;
	private GainProcessor gain;
	private AudioPlayer audioPlayer;
	private RateTransposer rateTransposer;
	private double currentFactor;// pitch shift factor
	private double sampleRate;
	
	private final JSlider factorSlider;
	private final JLabel factorLabel;
	private final JSlider gainSlider;
	private final JCheckBox originalTempoCheckBox;
	private final JSpinner centsSpinner;
	
	
	//set up a change listener that is activated when an item's parameter's change
	private ChangeListener parameterSettingChangedListener = new ChangeListener(){
		@Override
		//define behavior when states change
		public void stateChanged(ChangeEvent arg0) {
			//check if the thing being changed was a spinner
			//this correlates to the "Pitch Shift in Cents" element on the UI
			if (arg0.getSource() instanceof JSpinner) {
				//format number on UI and save as integer
				int centValue = Integer.valueOf(((JSpinner) arg0.getSource())
						.getValue().toString());
				//convert cent value into a factor value (this function is declared below)
				//complicated math
				currentFactor = centToFactor(centValue);
				//remove this change listener construct
				factorSlider.removeChangeListener(this);
				//set factor slider's value to new value
				factorSlider.setValue((int) Math.round(currentFactor * 100));
				//add new change listener
				factorSlider.addChangeListener(this);
			//check if thing being changed was a slider
			//this correlates to the "Factor X%" slider element on the UI
			} else if (arg0.getSource() instanceof JSlider) {
				//pull new factor value from slider UI
				currentFactor = factorSlider.getValue() / 100.0;
				//remove this change listener construct
				centsSpinner.removeChangeListener(this);
				//change value on cents spinner (after running through conversion function)
				//this value reflects changed made by factor slider
				centsSpinner.setValue(factorToCents(currentFactor));
				//add new change listener
				centsSpinner.addChangeListener(this);
			}
			//change label on factor to represent changes made with UI elements
			factorLabel.setText("Factor " + Math.round(currentFactor * 100) + "%");
			//check to see if there is a dispatcher initialized
			if (PitchShiftingExample.this.dispatcher != null) {	
				 //check to see if "Keep Original Tempo" UI box is checked
				 if(originalTempoCheckBox.getModel().isSelected()){
					 //pass the wsola the current sample rate, AND the factor we set up above
					 wsola.setParameters(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(currentFactor, sampleRate));
				 } 
				 else {
					 //pass the wsola the current sample rate, and a factor of 1 (doesn't change the pitch factor)
					 //changes the pitch and plays the voice slower
					 //!!SOUNDS CHOPPY, WE SHOULDN'T USE THIS
					 wsola.setParameters(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(1, sampleRate));
				 }
				 //pass the rate transposer the new pitch factor set up by the ui elements
				 //the rate transposer is in charge of changing the sample rate, which affects the pitch directly
				 //I believe this is where the actual shift occurs
				 rateTransposer.setFactor(currentFactor);
			 }
		}}; 
		
		//main class
	public PitchShiftingExample(){
		//set up main window
		this.setLayout(new BorderLayout());
		//define exit behavior
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//set title of program
		this.setTitle("Pitch shifting: change the pitch of your audio.");
		
		//set up checkbox, add the change listener defined earlier to it
		originalTempoCheckBox = new JCheckBox("Keep original tempo?", true);
		originalTempoCheckBox.addChangeListener(parameterSettingChangedListener);
		
		//set the default factor to 1, so that pitch is not immediately affected
		currentFactor = 1;
		
		//set up the "Factor %" slider on UI, and define a base value
		//apply the change listener defined earlier
		factorSlider = new JSlider(20, 250);
		factorSlider.setValue(100);
		factorSlider.setPaintLabels(true);
		factorSlider.addChangeListener(parameterSettingChangedListener);
		
		//add fileChooser panel so that users can upload a.wav file
		JPanel fileChooserPanel = new JPanel(new BorderLayout());
		fileChooserPanel.setBorder(new TitledBorder("1... Or choose your audio (wav mono)"));
		
		//declare chooser
		fileChooser = new JFileChooser();
		
		//add button to open file chooser
		JButton chooseFileButton = new JButton("Choose a file...");
		//add behavior called when button is pressed
		//this will be different for our android version
		chooseFileButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fileChooser.showOpenDialog(PitchShiftingExample.this);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                File file = fileChooser.getSelectedFile();
	                //automatically begin to process the file by passing it to startFile
	                startFile(file,null);
	            } else {
	                //canceled
	            }
			}			
		});
		//add the button to the file chooser panel
		fileChooserPanel.add(chooseFileButton);
		//set the layout for the file chooser
		fileChooser.setLayout(new BoxLayout(fileChooser, BoxLayout.PAGE_AXIS));
		
		//I'm not sure what this does, any ideas?
		//I believe it has to do with establishing an audio mixer, but I'm not sure
		//I think if we can't figure it out we leave it on
		JPanel inputSubPanel = new JPanel(new BorderLayout());
		JPanel inputPanel = new InputPanel();
		inputPanel.addPropertyChangeListener("mixer",
				new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent arg0) {
						//whenever whatever (arg0) is changed
						//things seem to be re-initialzed by calling StartFile
						//with no input file, and a different mixer
						startFile(null,(Mixer) arg0.getNewValue());
					}
				});
		inputSubPanel.add(inputPanel,BorderLayout.NORTH);
		inputSubPanel.add(fileChooserPanel,BorderLayout.SOUTH);
		
		//set up and define some default values for the gain slider
		gainSlider = new JSlider(0,200);
		gainSlider.setValue(100);
		gainSlider.setPaintLabels(true);
		//set up and add change listener for the event slider
		gainSlider.addChangeListener(new ChangeListener() {
			@Override
			//when state of slider changes...
			public void stateChanged(ChangeEvent arg0) {
				//make sure there is an audio dispatcher defined
				if (PitchShiftingExample.this.dispatcher != null) {
					//save value from UI element as double-type variable
					double gainValue = gainSlider.getValue() / 100.0;
					//send gain audio element new gain value
					//defined as GainProcessor object above
					gain.setGain(gainValue);
				}
			}
		});
		
		//set up second part of UI Panel
		JPanel params = new JPanel(new BorderLayout());
		params.setBorder(new TitledBorder("2. Set the algorithm parameters"));
		
		//set up label for factor percentage, and example of what it does
		JLabel label = new JLabel("Factor 100%");
		label.setToolTipText("The pitch shift factor in % (100 is no change, 50 is double pitch, 200 half).");
		//save label to variable that was defined earlier for some reason
		factorLabel = label;
		//add the label to the new panel
		params.add(label,BorderLayout.NORTH);
		//add the factor slider we defined in the beginning of main to this panel
		params.add(factorSlider,BorderLayout.CENTER);
		
		//add a new sub-panel
		JPanel subPanel = new JPanel(new GridLayout(2, 2));
		
		//create a new spinner for cents shifting and add the change listener to it
		centsSpinner = new JSpinner();
		centsSpinner.addChangeListener(parameterSettingChangedListener);
		//add labels and tool-tips
		label = new JLabel("Pitch shift in cents");
		label.setToolTipText("Pitch shift in cents.");
		//add the centsSpinner to the panel
		subPanel.add(label);
		subPanel.add(centsSpinner);

	    //add a label and tool-tip for the tempo-change checkbox
		label = new JLabel("Keep original tempo");
		label.setToolTipText("Pitch shift in cents.");
		//add label and earlier-defined checkbox to the panel 
		subPanel.add(label);
		subPanel.add(originalTempoCheckBox);
		
		//add another sub-panel
		params.add(subPanel,BorderLayout.SOUTH);
		
		//new panel for gain
		JPanel gainPanel = new JPanel(new BorderLayout());
		//add label and tooltip
		label = new JLabel("Gain (in %)");
		label.setToolTipText("Volume in % (100 is no change).");
		//add the label and the earlier-defined gain slider to the panel
		gainPanel.add(label,BorderLayout.NORTH);
		gainPanel.add(gainSlider,BorderLayout.CENTER);
		gainPanel.setBorder(new TitledBorder("3. Optionally change the volume"));
		
		//add all the sub-panels to one main panel
		this.add(inputSubPanel,BorderLayout.NORTH);
		this.add(params,BorderLayout.CENTER);
		this.add(gainPanel,BorderLayout.SOUTH);
		
	}
	
	//cent and factor utility conversion method, we will probably need these
	public static double centToFactor(double cents){
		return 1 / Math.pow(Math.E,cents*Math.log(2)/1200/Math.log(Math.E)); 
	}
	//cent and factor utility conversion method, we will probably need these
	private static double factorToCents(double factor){
		return 1200 * Math.log(1/factor) / Math.log(2); 
	}
	
	//function to initiate pitch shifting when a file is used as input
	private void startFile(File inputFile,Mixer mixer){
		//if there is currently a dispatcher running, stop that dispatcher
		if(dispatcher != null){
			dispatcher.stop();
		}
		//set up java audio format object
		AudioFormat format;
		try {
			//if an inputFile exists
			if(inputFile != null){
				//use java functions to pull the format of that file
				//don't know if these will work with android, may have to use equivalent functions
				format = AudioSystem.getAudioFileFormat(inputFile).getFormat();
			}else{
				//else set up a default format with values
				//THIS FUNCTION COULD USE MORE RESEARCH IF NECESSARY
				format = new AudioFormat(44100, 16, 1, true,true);
			}
			//initialize the RateTransposer , pass it the current Factor variable 
			rateTransposer = new RateTransposer(currentFactor);
			//set up the gain processor, and initialize it with value of 1
			gain = new GainProcessor(1.0);
			//set up the TARSOS audio player, and pass the format of the input file so it knows what to deal with
			audioPlayer = new AudioPlayer(format);
			//initialize the sample rate with a base value pulled from the format object
			sampleRate = format.getSampleRate();
			
			//can not time travel, unfortunately. It would be nice to go back and kill Hitler or something...
			
			//^ don't know what that comment means, it was in the original lololol
			//repeat code of whether or not the "keep tempo" checkbox above was checked
			 if(originalTempoCheckBox.getModel().isSelected() && inputFile != null){
				 wsola = new WaveformSimilarityBasedOverlapAdd(Parameters.musicDefaults(currentFactor, sampleRate));
			 } else {
				 wsola = new WaveformSimilarityBasedOverlapAdd(Parameters.musicDefaults(1, sampleRate));
			 }
			 
			 //if the inputFile is null
			 if(inputFile == null){
				 //get dataline information for the device using java functionality
				 //passes the format variable we set up earlier
				 //documentation here: https://docs.oracle.com/javase/7/docs/api/javax/sound/sampled/DataLine.Info.html
				 DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
					TargetDataLine line;
					//set a line variable to the media line we found the information for 
					line = (TargetDataLine) mixer.getLine(dataLineInfo);
					//open the line
					line.open(format, wsola.getInputBufferSize());
					//start the line
					//more line stuff here
					//https://docs.oracle.com/javase/7/docs/api/javax/sound/sampled/Line.html
					line.start();
					//stream info here
					//https://docs.oracle.com/javase/7/docs/api/javax/sound/sampled/AudioInputStream.html
					final AudioInputStream stream = new AudioInputStream(line);
					//pass the stream we just created through a utility function that makes it tarsos compatible
					JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
					// create a new dispatcher, passing it the audio stream, and some wsola parameters
					dispatcher = new AudioDispatcher(audioStream, wsola.getInputBufferSize(),wsola.getOverlap()); 
			 }else{
				 	//check format of file to see how we will dispatch audio
					if(format.getChannels() != 1){
						dispatcher = AudioDispatcherFactory.fromFile(inputFile,wsola.getInputBufferSize() * format.getChannels(),wsola.getOverlap() * format.getChannels());
						dispatcher.addAudioProcessor(new MultichannelToMono(format.getChannels(),true));
					}else{
						dispatcher = AudioDispatcherFactory.fromFile(inputFile,wsola.getInputBufferSize(),wsola.getOverlap());
					}
			 }
			
			 //associate the dispatcher we just set up with the wsola
			wsola.setDispatcher(dispatcher);
			//add all of our separate audio processor components into the dispatcher
			dispatcher.addAudioProcessor(wsola);
			dispatcher.addAudioProcessor(rateTransposer);
			dispatcher.addAudioProcessor(gain);
			dispatcher.addAudioProcessor(audioPlayer);

			//give the dispatcher its own thread and start it
			Thread t = new Thread(dispatcher);
			t.start();
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	//main 
	public static void main(String[] argv) {
		if (argv.length == 3) {
			try {
				//start the client
				startCli(argv[0],argv[1],Double.parseDouble(argv[2]));
				
				//error reporting from catching them
			} catch (NumberFormatException e) {
				printHelp("Please provide a well formatted number for the time stretching factor. See Synopsis.");
			} catch (UnsupportedAudioFileException e) {
				printHelp("Unsupported audio file, please check if the input is 16bit 44.1kHz MONO PCM wav.");
			} catch (IOException e) {
				printHelp("IO error, could not read from, or write to the audio file, does it exist?");
			}
		} else if(argv.length!=0){
			printHelp("Please provide exactly 3 arguments, see Synopsis.");
		}else{
			try {
				startGui();
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new Error(e);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new Error(e);
			}
		}
	}
	
	//function to start the gui and present its elements
	private static void startGui() throws InterruptedException, InvocationTargetException{
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					//ignore failure to set default look en feel;
				}
				JFrame frame = new PitchShiftingExample();
				frame.pack();
				frame.setSize(400,450);
				frame.setVisible(true);
			}
		});
	}
	
	//function that does everything through command line
	//won't comment this because it is basically above code, but it looks like it may be most similar to what
	//we need to do. it doesn't play any audio, just converts a target and saves to a new file
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

	//print help
	private static final void printHelp(String error){
		SharedCommandLineUtilities.printPrefix();
		System.err.println("Name:");
		System.err.println("\tTarsosDSP Pitch shifting utility.");
		SharedCommandLineUtilities.printLine();
		System.err.println("Synopsis:");
		System.err.println("\tjava -jar PitchShift.jar source.wav target.wav cents");
		SharedCommandLineUtilities.printLine();
		System.err.println("Description:");
		System.err.println("\tChange the play back speed of audio without changing the pitch.\n");
		System.err.println("\t\tsource.wav\tA readable, mono wav file.");
		System.err.println("\t\ttarget.wav\tTarget location for the pitch shifted file.");
		System.err.println("\t\tcents\t\tPitch shifting in cents: 100 means one semitone up, -100 one down, 0 is no change. 1200 is one octave up.");
		if(!error.isEmpty()){
			SharedCommandLineUtilities.printLine();
			System.err.println("Error:");
			System.err.println("\t" + error);
		}
    }
}
