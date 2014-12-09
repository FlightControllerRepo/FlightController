package com.cs492.flightcontroller.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.cs492.drone_model.Drone;
import com.cs492.drone_model.DroneActions;
import com.cs492.drone_model.DroneEvent;
import com.cs492.drone_model.DroneEvent.DroneEventListener;
import com.cs492.drone_model.implementation.DroneObject;
import com.cs492.flightcontroller.LogManager;
import com.cs492.flightcontroller.LogManager.LogSeverity;
import com.cs492.flightcontroller.R;

public class ManualCommandsFragment extends Fragment implements OnClickListener, DroneEventListener {

	private Button armDisarmButton_;
	private Button takeoffLandButton_;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.fragment_manual, container, false);
        
        armDisarmButton_ = (Button) V.findViewById(R.id.arm_disarm_button_id);
        takeoffLandButton_ = (Button) V.findViewById(R.id.takeoff_land_button_id);
        
        armDisarmButton_.setOnClickListener(this);
        takeoffLandButton_.setOnClickListener(this);
        
        return V;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (!DroneObject.INSTANCE.isIntilized() || !DroneObject.INSTANCE.isConnected()) {
        	armDisarmButton_.setEnabled(false);
        	takeoffLandButton_.setEnabled(false);
        } else {
        	armDisarmButton_.setText(DroneObject.INSTANCE.isArmed() ? "Disarm" : "Arm");
        	takeoffLandButton_.setText(DroneObject.INSTANCE.isInAir() ? "Land" : "Takeoff");
        }
		DroneObject.INSTANCE.addDroneEventListener(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		DroneObject.INSTANCE.removeDroneEventListener(this);
	}
	
	@Override
    public void onClick(View v) {
		switch (v.getId()) {
			case R.id.arm_disarm_button_id:
				if (!DroneObject.INSTANCE.isArmed())
					DroneActions.arm(DroneObject.INSTANCE);
				else 
					DroneActions.disarm(DroneObject.INSTANCE);
				break;
				
			case R.id.takeoff_land_button_id:
				if (!DroneObject.INSTANCE.isInAir())
					DroneActions.guidedTakeoff(DroneObject.INSTANCE, 5.0f);
				else 
					DroneActions.guidedLand(DroneObject.INSTANCE);
				break;
		}
	}

	@Override
	public void onDroneEvent(final DroneEvent event, Drone drone) {
		
		try{
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				LogManager.INSTANCE.addEntry(event.name(), LogSeverity.INFO);
				switch (event) {
					case ARMED:
						armDisarmButton_.setText("Disarm");
						break;
					case DISARMED:
						armDisarmButton_.setText("Arm");
						break;
					case CONNECTED:
						armDisarmButton_.setEnabled(true);
				    	takeoffLandButton_.setEnabled(true);
						armDisarmButton_.setText(DroneObject.INSTANCE.isArmed() ? "Disarm" : "Arm");
			        	takeoffLandButton_.setText(DroneObject.INSTANCE.isInAir() ? "Land" : "Takeoff");
			        	break;
					case DISCONNECTED:
						armDisarmButton_.setEnabled(false);
				    	takeoffLandButton_.setEnabled(false);
				    	break;
					default:
						break;
				}
			}
		});
	} catch (Exception ex) { LogManager.INSTANCE.addEntry(LogManager.stringFromException(ex), LogSeverity.ERROR); }

	}
	
}
