package com.flightcontroller.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.flightcontroller.R;
import com.flightcontroller.utils.LogManager;

public class LoggerActivity extends Activity {

	private static TextView loggerText_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		loggerText_ = (TextView) findViewById(R.id.log_text_id);
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

