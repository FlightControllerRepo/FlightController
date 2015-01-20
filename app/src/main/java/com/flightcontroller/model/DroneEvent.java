package com.flightcontroller.model;


import com.flightcontroller.MainActivity;
import com.flightcontroller.R;

/**
 * This class represents critical events that should be handled be the application
 * or backend copter model. The events are fairly self descript, and should be posted using
 * Drone.postEvent(event). For simplicly each event is attached a small blurb defining the
 * effect which can than can be used for exception text of posted to the UI. Further events
 * contain a boolean as to whetherm they should be considered a problem.
 *
 */
public enum DroneEvent {

	RADIO_CONNECTED(R.string.radio_connected_sb, false),
    RADIO_DISCONNECTED(R.string.radio_disconnected_sb, true),
    HEARTBEAT_FIRST(R.string.first_heartbeat_sb, false),
    HEARTBEAT_ENDED(R.string.lost_heartbeat_sb, true),
    GPS_CHANGED(),
    DISARMED(R.string.disarmed_sb, false),
    ARMED(R.string.armed_sb, false),
    ARM_TIMEOUT(R.string.arm_timeout_sb, true);

    private String displayText_;
    private boolean isProblem_;

    private DroneEvent() { }

    private DroneEvent(int info, boolean isProblem) {
        displayText_ = MainActivity.getMainContext().getString(info);
        isProblem_ = isProblem;
    }

    public boolean shouldNotify() {
        return displayText_ != null;
    }

    public String getText() {
        return displayText_;
    }


    /**
     * Interface that classes should implement if they want to handle DroneEvents.
     * To start receiving events you should register your listener with the Drone using
     * addDroneEventListener(listener);
     *
     */
    public interface DroneEventListener {
	
		void onDroneEvent(DroneEvent event, Drone drone);

	}
	
}
