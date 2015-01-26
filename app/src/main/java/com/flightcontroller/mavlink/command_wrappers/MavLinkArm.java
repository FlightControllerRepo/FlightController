package com.flightcontroller.mavlink.command_wrappers;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_COMPONENT;
import com.flightcontroller.model.Drone;


public class MavLinkArm {

	public static final int MAV_COMP_ID_SYSTEM_CONTROL = 250;
	public static final int MAV_CMD_COMPONENT_ARM_DISARM = 400;
	
	public static MAVLinkPacket getArmMessagePacket(boolean arm, Drone drone) { //Same but removed the drone parameter

		msg_command_long msg = new msg_command_long();
		msg.target_system = drone.getSysid();
		msg.target_component = (byte) MAV_COMPONENT.MAV_COMP_ID_SYSTEM_CONTROL;

		msg.command = MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM;//MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM;
		msg.param1 = arm ? 1 : 0;
		msg.param2 = 0;
		msg.param3 = 0;
		msg.param4 = 0;
		msg.param5 = 0;
		msg.param6 = 0;
		msg.param7 = 0;
		msg.confirmation = 0;

		//Remove comment to send
		
		//As before removed drone packet
		return msg.pack();
	}
}