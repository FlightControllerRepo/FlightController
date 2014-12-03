package com.cs492.flightcontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class LogActivity extends Activity {

	private static LogActivity logActivity_;
	private TextView textView_;
	
	public static void appendText(final String string) {
		if (logActivity_ == null || logActivity_.textView_ == null) return;
		
		synchronized(logActivity_) { 
			logActivity_.textView_.post(new Runnable() {
			    public void run() {
			    	logActivity_.textView_.append(Html.fromHtml(string));
			    }
			});
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
		getActionBar().setDisplayShowHomeEnabled(false);
		logActivity_ = this;
		textView_ = (TextView) this.findViewById(R.id.logtext);
		textView_.setKeyListener(null);
		textView_.setGravity(Gravity.START);
		textView_.setText(Html.fromHtml(LogManager.INSTANCE.getLogText()));
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.log, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_back) {
        	Intent logActivity = new Intent(this, MainActivity.class);
        	startActivity(logActivity);
        }
        return super.onOptionsItemSelected(item);
    }
}
