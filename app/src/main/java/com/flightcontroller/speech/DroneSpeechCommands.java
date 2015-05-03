package com.flightcontroller.speech;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.speech.tts.TextToSpeech;

import com.flightcontroller.MainActivity;
import com.flightcontroller.model.DroneActions;
import com.flightcontroller.model.DroneImp;
import com.flightcontroller.utils.LogManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
* Class to parse the recognized speech into commands for the quadcopter.
 * The parsed commands are then run using the DroneActions class.
*/
public class DroneSpeechCommands {

    private TextToSpeech textToSpeech;

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

    private static final Map<String, Integer> RELATIVE_DIRECTION_ANGLES =
            new HashMap<String, Integer>() {
                {
                    put("forward", 0);
                    put("right", 90);
                    put("backward", 180);
                    put("left", 270);
                }
            };

    private static final Map<String, Integer> ABSOLUTE_DIRECTION_ANGLES =
            new HashMap<String, Integer>() {
                {
                    put("north", 0);
                    put("east", 90);
                    put("south", 180);
                    put("west", 270);
                }
            };

    public DroneSpeechCommands(Context context) {
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status !=  TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });
    }

    protected void shutdown() {
        textToSpeech.shutdown();
    }

    /**
    * parse the speech. once the desired command is known, display an alert on
     * the screen asking the user to confirm the command.
    */
    public void parseSpeech(final String speech) {
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    @Override
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
	    	        case DialogInterface.BUTTON_POSITIVE:
				        if (speech.equals("start engine")) {
				            startEngine();
				        }
				        if (speech.equals("stop engine")) {
				            stopEngine();
				        }
				        if (speech.equals("launch")) {
				            launch();
				        }
				        if (speech.equals("land")) {
				            land();
				        }
				        if (speech.equals("stay")) {
				            stay();
				        }
				        if (speech.startsWith("go")) {
				            if (speech.contains("home")) {
				                goHome();
                                return;
				            }
				            if (speech.contains("to way point")) {
				                int waypoint = parseNumbers(speech.substring(speech.indexOf("to way point") + "to way point ".length()));
				                goToWaypoint(waypoint);
                                return;
				            }
				            // else, the command is "go <direction> <distance>"
				            String direction = speech.substring("go ".length());
				            direction = direction.substring(0, direction.indexOf(' '));
				            int distance = parseNumbers(speech);
				            go(direction, distance);
				        }
				        if (speech.equals("status")) {
				            status();
				        }
				        if (speech.startsWith("turn")) {
				            int degrees = parseNumbers(speech) * (speech.contains("negative") ? -1 : 1);
				            turn(degrees);
				        }
				        if (speech.startsWith("rotate")) {
				            rotate(parseNumbers(speech));
				        }
				        break;
	    	        case DialogInterface.BUTTON_NEGATIVE:
	    	            //No button clicked
	    	            break;
    	        }
    	    }
    	};
    	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getMainContext());
    	builder.setMessage("Confirm command \"" + speech + "\"?").setPositiveButton("Yes", dialogClickListener)
        	    .setNegativeButton("No", dialogClickListener).show();    
    }

    /**
    * parse instances of numbers found in the input string into their numeric value ("one two three" -> 123)
    */
    private int parseNumbers(String speech) {
        int retVal = 0;
        for (String token : speech.split("\\s+")) {
            for (Map.Entry<String, Integer> digitPair : DIGITS.entrySet()) {
                if (!token.equals(digitPair.getKey())) {
                    continue;
                }
                retVal = retVal * 10 + digitPair.getValue();
            }
        }
        return retVal;
    }

    private void startEngine() {
        DroneActions.arm(DroneImp.INSTANCE);
    }

    private void stopEngine() {
    	DroneActions.disarm(DroneImp.INSTANCE);
    }

    private void launch() {
    	DroneActions.guidedTakeoff(DroneImp.INSTANCE, 5.0f);
    }

    private void land() {
    	DroneActions.guidedLand(DroneImp.INSTANCE);
    }

    private String stay() {
        return "stay()";
    }

    private String goHome() {
        return "goHome()";
    }

    private void go(String direction, int distance) {
        if (direction.equals("up") || direction.equals("down")) {
            int delta = direction.equals("up") ? distance : -distance;
            DroneActions.goAltitude(DroneImp.INSTANCE, delta);
        } else if (RELATIVE_DIRECTION_ANGLES.containsKey(direction)) { //forward, left, backwards, right
            int bearing = RELATIVE_DIRECTION_ANGLES.get(direction);
            DroneActions.goForwardByBearing(DroneImp.INSTANCE, bearing, distance, true);
        } else if (ABSOLUTE_DIRECTION_ANGLES.containsKey(direction)) { //north, east, south, west
            int bearing = ABSOLUTE_DIRECTION_ANGLES.get(direction);
            DroneActions.goForwardByBearing(DroneImp.INSTANCE, bearing, distance, false);
        }
    }

    private String goToWaypoint(int waypoint) {
        return "goToWayPoint(" + waypoint + ")";
    }

    private void turn(int degrees) {
        DroneActions.turn(DroneImp.INSTANCE, degrees, true);
    }

    private String rotate(int degreesPerSecond) {
        return "rotate(" + degreesPerSecond + ")";
    }

    private void status() {
        ArrayList<String> statusText = DroneActions.getStatusText(DroneImp.INSTANCE);
        final StringBuffer dispText = new StringBuffer();
        for (String status : statusText) {
            dispText.append(status + "\n");
        }

        LogManager.INSTANCE.addEntry("STATUS:" + dispText, LogManager.LogSeverity.INFO);
        textToSpeech.speak(dispText.toString(), TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
    }
}