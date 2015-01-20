package com.flightcontroller.speech;


import android.app.AlertDialog;
import android.content.DialogInterface;

import com.flightcontroller.MainActivity;
import com.flightcontroller.model.DroneActions;
import com.flightcontroller.model.DroneImp;

import java.util.Map;

/**
* Class to parse the recognized speech into commands for the quadcopter.
*/
public class DroneSpeechCommands {

    private Map<String, Integer> digits; // map from the word representation of digits to the digits themselves.

    public DroneSpeechCommands(Map<String, Integer> digits) {
        this.digits = digits;
    }

    /**
    * parse the speech. once the desired command is known, display an alert on the screen asking the user to confirm the command.
    */
    protected void parseSpeech(final String speech) {
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
				            }
				            if (speech.contains("to way point")) {
				                int waypoint = parseNumbers(speech.substring(speech.indexOf("to way point") + "to way point ".length()));
				                goToWaypoint(waypoint);
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
            for (Map.Entry<String, Integer> digitPair : digits.entrySet()) {
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

    private String go(String direction, int distance) {
        return "go(" + direction + "," + distance + ")";
    }

    private String goToWaypoint(int waypoint) {
        return "goToWayPoint(" + waypoint + ")";
    }

    private String turn(int degrees) {
        return "turn(" + degrees + ")";
    }

    private String rotate(int degreesPerSecond) {
        return "rotate(" + degreesPerSecond + ")";
    }

    private String status() {
        return "status()";
    }
}