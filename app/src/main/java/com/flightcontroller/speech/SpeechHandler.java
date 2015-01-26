package com.flightcontroller.speech;

import android.os.AsyncTask;

import com.flightcontroller.utils.LogManager;
import com.flightcontroller.utils.LogManager.LogSeverity;
import com.flightcontroller.MainActivity;
import com.flightcontroller.R;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

/**
* Fragment that deals with the speech recognition. includes the button that enables speech and alerts to confirm speech.
*/
public class SpeechHandler implements RecognitionListener {

	private static final String KWS_SEARCH = "wakeup";
	private static final String KEYPHRASE = "start";
	private static final String COMMANDS_SEARCH = "commands";
	
	@SuppressWarnings("serial")
	private static final Map<String, Integer> DIGITS = new HashMap<String, Integer>() {
    	{
    		 put("oh", 0);
    	     put("zero", 0);
    	     put("one", 1);
    	     put("two", 2);
    	     put("three", 3);
    	     put("four", 4);
    	     put("five", 5);
    	     put("six", 6);
    	     put("seven", 7);
    	     put("eight", 8);
    	     put("nine", 9);
    	}
    };
 
	private SpeechRecognizer recognizer_;
	private DroneSpeechCommands commandParser_;
	
    private HashMap<String, Integer> captions_;
 
    
    public SpeechHandler() {
    	commandParser_ = new DroneSpeechCommands(DIGITS);
        captions_ = new HashMap<>();
        captions_.put(COMMANDS_SEARCH, R.string.commands_caption);
        
        //initialize the sphinx engine. once it is ready, make the speech button visible.
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(MainActivity.getMainContext());
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    LogManager.INSTANCE.addEntry("Failed to init recognizer " + result, 
                    		LogSeverity.ERROR);
                } 
            }
        }.execute();
    }

    public void startListening() {
    	if (recognizer_ != null)
    		recognizer_.startListening(COMMANDS_SEARCH);
    }
    
    public void stopListening() {
        if (recognizer_ != null)
    	    recognizer_.stop();
    }

    public int getAudioVolume() {
        if (recognizer_ != null)
            return recognizer_.getAudioVolume();

        return -1;
    }
    
    /**
    * method called when sphinx detects a partial result. we don't need to do anything here.
    */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
    	if ( hypothesis == null) return;

        String text = hypothesis.getHypstr();
        LogManager.INSTANCE.addEntry("Partial speech found: " + text, LogSeverity.INFO);
    }

    /**
    * method called when sphinx gets a full result. parse the detected speech.
    */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            LogManager.INSTANCE.addEntry("Speech found: " + text, LogSeverity.INFO);
            commandParser_.parseSpeech(text);
        }
    }

    @Override
    public void onBeginningOfSpeech() { }

    @Override
    public void onEndOfSpeech() { }

    /**
    * set up the recognizer by referencing the grammar and dictionary
    */
    private void setupRecognizer(File assetsDir) {
        File modelsDir = new File(assetsDir, "models");
        recognizer_ = defaultSetup()
                .setAcousticModel(new File(modelsDir, "hmm/en-us-semi"))
                .setDictionary(new File(modelsDir, "dict/cmu07a.dic"))
                .setRawLogDir(assetsDir).setKeywordThreshold(1e-20f)
                .getRecognizer();
        recognizer_.addListener(this);
        
        // Create keyword-activation search.
        recognizer_.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        File commandsGrammar = new File(modelsDir, "grammar/commands.gram");
        recognizer_.addGrammarSearch(COMMANDS_SEARCH, commandsGrammar);
    }

	@Override
	public void onError(Exception arg0) { }

	@Override
	public void onTimeout() { }
	
}