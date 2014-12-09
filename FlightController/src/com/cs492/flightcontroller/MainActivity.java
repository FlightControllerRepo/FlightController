package com.cs492.flightcontroller;

import org.acra.ACRA;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.MAVLink.Messages.MAVLinkMessage;
import com.cs492.drone_model.implementation.DroneObject;
import com.cs492.flightcontroller.LogManager.LogSeverity;
import com.cs492.flightcontroller.fragments.LoggerFragment;
import com.cs492.flightcontroller.fragments.ManualCommandsFragment;
import com.cs492.flightcontroller.fragments.speech.SpeechFragment;
import com.cs492.mavlink_connection.MavLinkConnectionListener;


public class MainActivity extends Activity implements MavLinkConnectionListener {
    
	private static Context mainContext_;
	
	private MenuItem connectDisconnectItem_;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainContext_ = this;
        
        LogManager.INSTANCE.addEntry("OnCreate MainActivity", LogSeverity.INFO);
        setContentView(R.layout.activity_main);
        getActionBar().setDisplayShowHomeEnabled(false); 
         
        DroneObject.INSTANCE.setupComponents();
        DroneObject.INSTANCE.addUSBListener(this);
        
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        
        SpeechFragment speech = new SpeechFragment();
        LoggerFragment logger = new LoggerFragment();
        ManualCommandsFragment manual = new ManualCommandsFragment();
        fragmentTransaction.add(R.id.top_container, speech, "speech");
        fragmentTransaction.add(R.id.below_container, logger, "logger");
        fragmentTransaction.commit();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        connectDisconnectItem_ = menu.findItem(R.id.connect_disconnect_ab);
    	connectDisconnectItem_.setTitle(DroneObject.INSTANCE.isConnected() ? "Disconnect" : "Connect");
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.connect_disconnect_ab) {
        	if (DroneObject.INSTANCE.isConnected())
        		DroneObject.INSTANCE.disconnect();
        	else 
        		DroneObject.INSTANCE.connect();
        }
        
        
        return super.onOptionsItemSelected(item);
    }

	public static Context getMainContext() {
		return mainContext_;
	}

	@Override
	public void onConnect() { 
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				connectDisconnectItem_.setTitle("Disconnect");
			}
		});
	}

	@Override
	public void onReceiveMessage(MAVLinkMessage msg) { }

	@Override
	public void onDisconnect() { 
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				connectDisconnectItem_.setTitle("Connect");
			}
		});
	}

	@Override
	public void onComError(String errMsg) { }
}
