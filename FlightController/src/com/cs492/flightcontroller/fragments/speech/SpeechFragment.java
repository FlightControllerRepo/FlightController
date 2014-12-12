package com.cs492.flightcontroller.fragments.speech;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.cs492.flightcontroller.LogManager;
import com.cs492.flightcontroller.LogManager.LogSeverity;
import com.cs492.flightcontroller.MainActivity;
import com.cs492.flightcontroller.R;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

/**
* Fragment that deals with the speech recognition. includes the button that enables speech and alerts to confirm speech.
*/
public class SpeechFragment extends Fragment implements RecognitionListener {

	private static final String KWS_SEARCH = "wakeup";
	private static final String KEYPHRASE = "start";
	private static final String COMMANDS_SEARCH = "commands";
	
    /**
    * initialize the digits map that will be used to recognize numbers in speech.
    */
	private void initializeDigitsMap() {
        digits.put("oh", 0);
        digits.put("zero", 0);
        digits.put("one", 1);
        digits.put("two", 2);
        digits.put("three", 3);
        digits.put("four", 4);
        digits.put("five", 5);
        digits.put("six", 6);
        digits.put("seven", 7);
        digits.put("eight", 8);
        digits.put("nine", 9);
    }
	
	private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;
    private final Map<String, Integer> digits = new HashMap<String, Integer>();
	
    private ImageButton speechButton_;
    private boolean isPressed = false;
    
    /**
    * initialize the view. initialize an invisible speech button, which will be made visible when the recognizer is ready.
    */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.fragment_speech, container, false);
             
        speechButton_ = (ImageButton) V.findViewById(R.id.speech_button);
        speechButton_.setVisibility(View.INVISIBLE);
        speechButton_.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        speechButton_.setImageResource(R.drawable.mic2);
                        //start listening commands only when button is pressed
                        if(recognizer !=null)
                        	recognizer.startListening(COMMANDS_SEARCH);
                       
                        return true;
                    case MotionEvent.ACTION_UP:
                    	//stop recognizer if button is not pressed
                    	recognizer.stop();
                        speechButton_.setImageResource(R.drawable.mic);

                        return true;
                }
                return false;
            }
        });
        
        captions = new HashMap<String, Integer>();
        captions.put(COMMANDS_SEARCH, R.string.commands_caption);
        initializeDigitsMap();
        
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
                } else {
                    speechButton_.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
        
        return V;
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
            new QuadcopterCommands(digits).parseSpeech(text);
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
    	
    }

    /**
    * set up the recognizer by referencing the grammar and dictionary
    */
    private void setupRecognizer(File assetsDir) {
        File modelsDir = new File(assetsDir, "models");
        recognizer = defaultSetup()
                .setAcousticModel(new File(modelsDir, "hmm/en-us-semi"))
                .setDictionary(new File(modelsDir, "dict/cmu07a.dic"))
                .setRawLogDir(assetsDir).setKeywordThreshold(1e-20f)
                .getRecognizer();
        recognizer.addListener(this);
        
        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        File commandsGrammar = new File(modelsDir, "grammar/commands.gram");
        recognizer.addGrammarSearch(COMMANDS_SEARCH, commandsGrammar);
        
    }

	@Override
	public void onError(Exception arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTimeout() {
		// TODO Auto-generated method stub
		
	}
	
}
