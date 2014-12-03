package com.cs492.mavlink.commands;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_COMPONENT;


public class MavLinkArm {

	//Taken from MAV_COMPONENT.java
	public static final int MAV_COMP_ID_SYSTEM_CONTROL = 250;
	
	//Taken from MAV_CMD.java
	public static final int MAV_CMD_COMPONENT_ARM_DISARM = 400;
	
	public static MAVLinkPacket sendArmMessage(boolean arm, byte sis, byte cis) { //Same but removed the drone parameter

		msg_command_long msg = new msg_command_long();
		msg.target_system = sis;
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