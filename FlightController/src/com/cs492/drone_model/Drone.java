package com.cs492.drone_model;

import com.MAVLink.MAVLinkPacket;
import com.cs492.drone_model.DroneEvent.DroneEventListener;

/**
Basic interface of a drone
*/
public interface Drone {

	void connect();
	void setupComponents();
	
	boolean isIntilized();
	boolean isConnected();
	boolean isInAir();
	boolean isArmed();
	
	byte getSysid();
	byte getCompid();

	void postEvent(DroneEvent event);
	void sendPacket(MAVLinkPacket requestParametersListPacket);

	void addDroneVariable(DroneVariable attribute);
	void addDroneEventListener(DroneEventListener listener);
	void removeDroneEventListener(DroneEventListener listener);

}
