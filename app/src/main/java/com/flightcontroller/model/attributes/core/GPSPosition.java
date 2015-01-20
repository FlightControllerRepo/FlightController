package com.flightcontroller.model.attributes.core;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_global_position_int;
import com.flightcontroller.model.DroneAttribute;

public class GPSPosition extends DroneAttribute {

	private float latitude_;
	private float longitude_;
	
	@Override
	public int[] getMessageHandleTypes() {
		return new int[] { msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT };
	}

	@Override
	public void handleMessage(MAVLinkMessage msg) {
		switch (msg.msgid) {
			case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
				msg_global_position_int gpsmsg = (msg_global_position_int) msg;
				latitude_ = gpsmsg.lat / 1.0e7f;
				longitude_ = gpsmsg.lon / 1.0e7f;
				break;		
		};
	}

	@Override
	public String getIdentifier() {
		return "GPSPosition";
	}

	public float getLatitude() {
		return latitude_;
	}
	
	public float getLongitude() {
		return longitude_;
	}
	
}
