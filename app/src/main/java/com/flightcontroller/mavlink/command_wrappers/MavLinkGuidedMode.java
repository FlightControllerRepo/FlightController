package com.flightcontroller.mavlink.command_wrappers;



import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.ApmModes;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_set_mode;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;
import com.flightcontroller.model.Drone;

public class MavLinkGuidedMode {

    public static MAVLinkPacket getWaypointPacket(Drone drone, double latitude,
                                              double longitude, double alititude) {
        msg_mission_item msg = new msg_mission_item();
        msg.seq = 0;
        msg.current = 2; // TODO use guided mode enum
        msg.frame = MAV_FRAME.MAV_FRAME_GLOBAL;
        msg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT; //
        msg.param1 = 0; // TODO use correct parameter
        msg.param2 = 0; // TODO use correct parameter
        msg.param3 = 0; // TODO use correct parameter
        msg.param4 = 0; // TODO use correct parameter
        msg.x = (float) latitude;
        msg.y = (float) longitude;
        msg.z = (float) alititude;
        msg.autocontinue = 1; // TODO use correct parameter
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
       return msg.pack();
    }

	public static MAVLinkPacket getChangeFlightModePacket(ApmModes mode, Drone drone) {
		msg_set_mode msg = new msg_set_mode();
		msg.target_system = drone.getSysid();
		msg.base_mode = 1; // TODO use meaningful constant
		msg.custom_mode = mode.getNumber();
		return msg.pack();
	}
}
