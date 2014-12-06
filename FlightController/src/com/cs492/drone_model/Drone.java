package com.cs492.drone_model;

import com.MAVLink.MAVLinkPacket;

/**
Basic interface of a drone
*/
public interface Drone {

	void connect();
	void setupComponents();
	
	byte getSysid();
	byte getCompid();

	void postEvent(DroneEvent event);
	void sendPacket(MAVLinkPacket requestParametersListPacket);

	void addDroneAttribute(DroneAttribute attribute);
	void addDroneEventListener(DroneEventListener listener);

}
