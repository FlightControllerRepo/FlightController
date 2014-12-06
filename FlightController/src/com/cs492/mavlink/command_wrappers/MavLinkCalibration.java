package com.cs492.mavlink.command_wrappers;



import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_CMD_ACK;
import com.MAVLink.enums.MAV_COMPONENT;

public class MavLinkCalibration {

	public static MAVLinkPacket sendCalibrationAckMessage(int count) {
		msg_command_ack msg = new msg_command_ack();
		msg.command = (short) count;
		msg.result = MAV_CMD_ACK.MAV_CMD_ACK_OK;
		return msg.pack();
	}

	public static MAVLinkPacket sendStartCalibrationMessage(byte sis) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = sis;
		msg.target_component = (byte) MAV_COMPONENT.MAV_COMP_ID_SYSTEM_CONTROL;
		
		msg.command = MAV_CMD.MAV_CMD_PREFLIGHT_CALIBRATION;
		msg.param1 = 0;
		msg.param2 = 0;
		msg.param3 = 0;
		msg.param4 = 0;
		msg.param5 = 1;
		msg.param6 = 0;
		msg.param7 = 0;
		msg.confirmation = 0;
		return msg.pack();
	}

}
