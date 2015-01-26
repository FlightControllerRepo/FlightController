package com.flightcontroller.model;

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
public abstract class DroneAttribute {

	protected Drone drone_;
	
	public DroneAttribute() { }
	
	public void setDrone(Drone owner) { drone_ = owner; }

    /**
     * This method should return the type of the mavlink messages it
     * accepts. This includes values such as
     * msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS,
     * msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT,
     * etc.
     *
     * This essentially subscribes this attribute to only messages
     * that were returned by this method.
     *
     * @return Any array of messages types that this Attribute can handle
     */
	public abstract int[] getMessageHandleTypes();

    /**
     * Called when a the copter receives a message of the
     * type returned by getMessageHandleTypes().
     *
     * @param msg Message to be handled
     */
	public abstract void handleMessage(MAVLinkMessage msg);

    /**
     * Returns the string identifier that this Attribute
     * can be queried for in the Drone object, using
     * Drone.getDroneAttribute()
     *
     * @return A string "name" for this Attribute
     */
	public abstract String getIdentifier();
	
}
