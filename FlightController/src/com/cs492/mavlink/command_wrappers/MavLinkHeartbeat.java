package com.cs492.mavlink.command_wrappers;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.enums.MAV_AUTOPILOT;
import com.MAVLink.enums.MAV_TYPE;


/**
 * This class contains logic used to send an heartbeat to a
 * {@link com.cs492.drone_model.droidplanner.core.model.Drone}.
 */
public class MavLinkHeartbeat {

	/**
	 * This is the msg heartbeat used to check the drone is present, and
	 * responding.
	 */
	private static final msg_heartbeat sMsg = new msg_heartbeat();
	static {
		sMsg.type = MAV_TYPE.MAV_TYPE_GCS;
		sMsg.autopilot = MAV_AUTOPILOT.MAV_AUTOPILOT_GENERIC;
	}

	/**
	 * This is the mavlink packet obtained from the msg heartbeat, and used for
	 * actual communication.
	 */
	private static final MAVLinkPacket sMsgPacket = sMsg.pack();

	/**
	 * Sends the heartbeat to the {@link com.cs492.drone_model.droidplanner.core.model.Drone}
	 * object.
	 * 
	 * @param drone
	 *            drone to send the heartbeat to
	 */
	public static MAVLinkPacket sendMavHeartbeat() {
		return sMsgPacket;
	}

}