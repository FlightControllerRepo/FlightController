package com.cs492.drone_model;


public enum DroneEvent {

	CONNECTED, DISCONNECTED, HEARTBEAT_FIRST, GPS_CHANGED, DISARMED, ARMED;
	
	public interface DroneEventListener {
	
		void onDroneEvent(DroneEvent event, Drone drone);

	}
	
}
