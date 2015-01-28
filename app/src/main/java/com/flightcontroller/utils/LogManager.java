package com.flightcontroller.utils;

import android.util.Log;

import com.flightcontroller.ui.LoggerActivity;

import java.util.ArrayList;
import java.util.Date;

public enum LogManager {
    INSTANCE;
    
    private ArrayList<LogEntry> entries_;
    
    private LogManager() {
    	entries_ = new ArrayList<LogEntry>();
    }
    
    public static String stringFromException(Exception e) {
    	StringBuilder string = new StringBuilder();
    	for (StackTraceElement elem : e.getStackTrace())
    		string.append(elem.toString() + "\n");
    		
    	return string.toString();
    }
    
    public synchronized void addEntry(String message, LogSeverity severity) {
    	LogEntry newEntry = new LogEntry(severity, message);
    	entries_.add(newEntry);
    	LoggerActivity.appendText(newEntry + "<br>");
        Log.v("LOGGER", message);
    }
    
    public String getLogText() {
    	StringBuilder string = new StringBuilder();
    	for (LogEntry entry : entries_)
    		string.append(entry + "<br>");
    	
    	return string.toString();
    }
    
    public enum LogSeverity {
    	WARNING("#FFCC00"),
    	INFO("#003366"),
    	ERROR("#FF0000");
    	
    	private String color_;
    	private LogSeverity(String color) {
    		color_ = color;
    	}
    	
    	public String toString() { return color_; }
    	
    }
    
    private class LogEntry {
    	private LogSeverity severity_;
    	private String message_;
    	private Date timestamp_;
    	
    	public LogEntry(LogSeverity severity, String message) {
    		severity_ = severity;
    		message_ = message;
    		timestamp_ = new Date();
    	}  	
    	
    	public String toString() {
    		return "<small><font color=\"" + severity_ + "\">[" + timestamp_.toString() + "]-" + message_ + "</font></small>";
    	}
    	
    }
}