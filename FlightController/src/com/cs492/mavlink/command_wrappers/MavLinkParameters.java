package com.cs492.mavlink.command_wrappers;



import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_param_request_list;
import com.MAVLink.common.msg_param_request_read;
import com.MAVLink.common.msg_param_set;
import com.cs492.drone_model.Drone;
import com.cs492.drone_model.attributes.parameters.Parameter;

public class MavLinkParameters {
	public static MAVLinkPacket getRequestParametersListPacket(Drone drone) {
		msg_param_request_list msg = new msg_param_request_list();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		return msg.pack();
	}

	public static MAVLinkPacket getSendParameterPacket(Parameter parameter, Drone drone) {
		msg_param_set msg = new msg_param_set();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		msg.setParam_Id(parameter.name);
		msg.param_type = (byte) parameter.type;
		msg.param_value = (float) parameter.value;
		return msg.pack();
	}

	public static MAVLinkPacket getReadParameterPacket(String name, Drone drone) {
		msg_param_request_read msg = new msg_param_request_read();
		msg.param_index = -1;
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		msg.setParam_Id(name);
		return msg.pack();
	}

	public static MAVLinkPacket getReadParameterPacket(int index, Drone drone) {
		msg_param_request_read msg = new msg_param_request_read();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getSysid();
		msg.param_index = (short) index;
		return msg.pack();
	}
}
