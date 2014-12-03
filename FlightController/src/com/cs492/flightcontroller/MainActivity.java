package com.cs492.flightcontroller;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import com.cs492.flightcontroller.LogManager.LogSeverity;
import com.cs492.mavlink.usb.UsbConnection;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;


public class MainActivity extends Activity implements RecognitionListener {

	private static final String KWS_SEARCH = "wakeup";
	private static final String FORECAST_SEARCH = "forecast";
	private static final String DIGITS_SEARCH = "digits";
	private static final String MENU_SEARCH = "menu";
	private static final String KEYPHRASE = "start";
	private static final String COMMANDS_SEARCH = "commands";
	
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.INSTANCE.addEntry("OnCreate MainActivity", LogSeverity.INFO);
        setContentView(R.layout.activity_main);
        getActionBar().setDisplayShowHomeEnabled(false); 
        
        speechButton_ = (ImageButton) findViewById(R.id.speech_button);
        UsbConnection connection = new UsbConnection(this);
        connection.connect();    
        
        captions = new HashMap<String, Integer>();
        captions.put(COMMANDS_SEARCH, R.string.commands_caption);
        initializeDigitsMap();
        
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(MainActivity.this);
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
                	switchSearch(COMMANDS_SEARCH);
                }
            }
        }.execute();
        
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
        if (id == R.id.action_log) {
        	Intent logActivity = new Intent(this, LogActivity.class);
        	startActivity(logActivity);
        }
        
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
    	if (!speechButton_.isPressed()) return;
    	
        String text = hypothesis.getHypstr();
        if (text.equals(COMMANDS_SEARCH))
        	switchSearch(COMMANDS_SEARCH);
        else
            setResultText(text);
    }

    private void setResultText(String text) {
    	LogManager.INSTANCE.addEntry("Speech found:" + text, LogSeverity.INFO);
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
    	if (!speechButton_.isPressed()) return;
        
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            setResultText(new QuadcopterCommands(digits).parseSpeech(text));
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
        switchSearch(COMMANDS_SEARCH);
    }

    private void switchSearch(String searchName) {
        recognizer.stop();
        recognizer.startListening(searchName);
        String caption = getResources().getString(captions.get(searchName));
        LogManager.INSTANCE.addEntry("Caption:" + caption, LogSeverity.INFO);
    }

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
        // Create grammar-based searches.
        File menuGrammar = new File(modelsDir, "grammar/menu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
        File digitsGrammar = new File(modelsDir, "grammar/digits.gram");
        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);
        File commandsGrammar = new File(modelsDir, "grammar/commands.gram");
        recognizer.addGrammarSearch(COMMANDS_SEARCH, commandsGrammar);
        
        // Create language model search.
        File languageModel = new File(modelsDir, "lm/weather.dmp");
        recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);
    }
}
