package com.flightcontroller.mavlink.command_wrappers;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.enums.MAV_AUTOPILOT;
import com.MAVLink.enums.MAV_TYPE;


/**
 * This class contains logic used to send an heartbeat
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

	public static MAVLinkPacket getMavHeartbeatPacket() {
		return sMsgPacket;
	}

}
