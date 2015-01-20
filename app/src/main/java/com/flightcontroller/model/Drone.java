package com.flightcontroller.model;

import com.MAVLink.MAVLinkPacket;
import com.flightcontroller.model.DroneEvent.DroneEventListener;

/**
 * An basic template interface for implementation of the app's backend
 * copter representation. This interface outlines the required methods to
 * establish a connection with the drone, and query its current state, through
 * DroneAttributes. Further it provides an way of interfacing with the radio transmitter
 * through sendPacket() and allowing the user to handle key events (as defined in DroneEvent)
 */
public interface Drone {

    /**
     * Responsible for interfacing with the radio transmitter, to allow communication
     * with the quadcopter. This call should be asynchronous, (and is within DroneImp)
     * since the connection is done within its own thread. use isConnected() to determine
     * if the drone is connected
     */
	void connectRadio();

    /**
     * Disconnects the radio transmitter and frees any resources held by the
     * IO connection
     */
	void disconnectRadio();

    /**
     * Returns the initialization state of the app's backend. This is done
     * automatically on startup, so this should always be true.
     *
     * @return true if the core components have been initialized
     */
	boolean isInitialized();

    /**
     *
     * @return true if the radio transmitter has been connected to the app. This DOES NOT
     * imply that a connection has been established with the copter
     */
	boolean isConnected();

    /**
     *
     * @return true if the copter is currently in air (ie has taken off).
     */
	boolean isInAir();

    /**
     *
     * @return true if the copter has been armed. The IRIS+ copter requires a preflight
     * arming to be one before any commands can be run
     */
	boolean isArmed();

    /**
     *
     * @return the system ID of the copter. If the copter has not been connected yet, -1 will
     * be returned
     */
	byte getSysid();

    /**
     *
     * @return the component ID of the copter. IF the copter has not been connected yet,
     * -1 will be returned
     */
	byte getCompid();

    /**
     * Posts a drone event to all the listeners. DroneEvents should be
     * passed to all listeners as soon as possible, in the order they came
     *
     * @param event event to be posted
     */
	void postEvent(DroneEvent event);

    /**
     * Sends a MAVLinkPacket to the copter through the radio transmitter.
     *
     * @param packet Packet to be sent
     */
	void sendPacket(MAVLinkPacket packet);

    /**
     * Adds the listener to this drone, such that any new events will also be forwarded to this
     * listener as well.
     *
     * @param listener
     */
	void addDroneEventListener(DroneEventListener listener);

    /**
     * Removes the listener from the drone
     *
     * @param listener
     */
	void removeDroneEventListener(DroneEventListener listener);

    /**
     * Adds the DroneAttribute to the drone. Internally the DroneImp is
     * responsible for allocating the core attributes (model.attributes.core),
     * but this method may be used to add any other attributes at runtime.
     *
     * @param attribute
     */
	void addDroneVariable(DroneAttribute attribute);

    /**
     * Gets the DroneAttribute from the identifying tag. This tag should match
     * the return value of the overridden method DroneAttribute.getIdentifier();
     *
     * @param tag the identifier
     * @return the drone attribute object, null if not found
     */
	DroneAttribute getDroneAttribute(String tag);

}
