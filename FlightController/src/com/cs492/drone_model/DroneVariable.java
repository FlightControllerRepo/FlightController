package com.cs492.drone_model;

import com.MAVLink.Messages.MAVLinkMessage;

/**
 * 
 * @author Nicholas
 *
 * A drone attribute is basicly anything that belongs to the drone. It can
 * be battery, its altitude, the heartbeats received. The idea is that
 * by extending this class, the subclasses can intercept and deal with 
 * copter sent messages. For instance, the HearbeatMonitor is set up
 * to read heartbeat messages from the copter. 
 *
 */
public abstract class DroneVariable {

	protected Drone drone_;
	
	public DroneVariable() { }
	
	public void setDrone(Drone owner) { drone_ = owner; }
	
	public abstract int[] getMessageHandleTypes();
	public abstract void handleMessage(MAVLinkMessage msg);
	public abstract String getIdentifier();
	
}
