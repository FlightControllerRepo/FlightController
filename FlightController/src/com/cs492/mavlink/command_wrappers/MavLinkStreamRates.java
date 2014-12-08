package com.cs492.mavlink.command_wrappers;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_request_data_stream;
import com.cs492.drone_model.Drone;

public class MavLinkStreamRates {


	public static MAVLinkPacket getStreamRequestPacket(Drone drone, int stream_id, int rate) {
		msg_request_data_stream msg = new msg_request_data_stream();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();

		msg.req_message_rate = (short) rate;
		msg.req_stream_id = (byte) stream_id;

		if (rate > 0) {
			msg.start_stop = 1;
		} else {
			msg.start_stop = 0;
		}
		return msg.pack();
	}
}
