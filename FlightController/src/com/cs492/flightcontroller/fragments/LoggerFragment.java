package com.cs492.flightcontroller.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cs492.flightcontroller.LogManager;
import com.cs492.flightcontroller.R;

public class LoggerFragment extends Fragment {

	private static TextView loggerText_;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	    return inflater.inflate(R.layout.fragment_log, container, false);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		loggerText_ = (TextView) getView().findViewById(R.id.log_text_id);
		loggerText_.setMovementMethod(new ScrollingMovementMethod());
		loggerText_.setText(Html.fromHtml(LogManager.INSTANCE.getLogText()));
	}
	
	@Override
	public void onStop() {
		super.onStop();
		loggerText_ = null;
	}
	
	public static void appendText(final String string) {
		if (loggerText_ == null) return;
		
		synchronized(loggerText_) { 
			loggerText_.post(new Runnable() {
			    public void run() {
			    	loggerText_.append(Html.fromHtml(string));
			    }
			});
		}
	}
	
}

