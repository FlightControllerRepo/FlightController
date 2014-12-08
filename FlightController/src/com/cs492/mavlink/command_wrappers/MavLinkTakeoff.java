package com.cs492.mavlink.command_wrappers;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.enums.MAV_CMD;
import com.cs492.drone_model.Drone;

public class MavLinkTakeoff {
	public static MAVLinkPacket getSendTakeoffPacket(Drone drone, float meter) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		msg.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;

		msg.param7 = meter;

		return msg.pack();
	}
}
