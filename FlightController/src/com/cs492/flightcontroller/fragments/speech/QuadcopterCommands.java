package com.cs492.flightcontroller.fragments.speech;


import java.util.Map;

import com.cs492.drone_model.DroneActions;
import com.cs492.drone_model.implementation.DroneObject;
import com.cs492.flightcontroller.MainActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;

public class QuadcopterCommands {

    private Map<String, Integer> digits;

    public QuadcopterCommands(Map<String, Integer> digits) {
        this.digits = digits;
    }

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
        DroneActions.arm(DroneObject.INSTANCE);
    }

    private void stopEngine() {
    	DroneActions.disarm(DroneObject.INSTANCE);
    }

    private void launch() {
    	DroneActions.guidedTakeoff(DroneObject.INSTANCE, 5.0f);
    }

    private void land() {
    	DroneActions.guidedLand(DroneObject.INSTANCE);
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
