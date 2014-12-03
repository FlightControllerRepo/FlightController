package com.cs492.flightcontroller;


import java.util.Map;

public class QuadcopterCommands {

    private Map<String, Integer> digits;

    public QuadcopterCommands(Map<String, Integer> digits) {
        this.digits = digits;
    }

    protected String parseSpeech(String speech) {
        if (speech.equals("start engine")) {
            return startEngine();
        }
        if (speech.equals("stop engine")) {
            return stopEngine();
        }
        if (speech.equals("launch")) {
            return launch();
        }
        if (speech.equals("land")) {
            return land();
        }
        if (speech.equals("stay")) {
            return stay();
        }
        if (speech.startsWith("go")) {
            if (speech.contains("home")) {
                return goHome();
            }
            if (speech.contains("to way point")) {
                int waypoint = parseNumbers(speech.substring(speech.indexOf("to way point") + "to way point ".length()));
                return goToWaypoint(waypoint);
            }
            // else, the command is "go <direction> <distance>"
            String direction = speech.substring("go ".length());
            direction = direction.substring(0, direction.indexOf(' '));
            int distance = parseNumbers(speech);
            return go(direction, distance);
        }
        if (speech.equals("status")) {
            return status();
        }
        if (speech.startsWith("turn")) {
            int degrees = parseNumbers(speech) * (speech.contains("negative") ? -1 : 1);
            return turn(degrees);
        }
        if (speech.startsWith("rotate")) {
            return rotate(parseNumbers(speech));
        }
        return "";
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

    private String startEngine() {
        return "startEngine()";
    }

    private String stopEngine() {
        return "stopEngine()";
    }

    private String launch() {
        return "launch()";
    }

    private String land() {
        return "land()";
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
