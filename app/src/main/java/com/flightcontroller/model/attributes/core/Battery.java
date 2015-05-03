package com.flightcontroller.model.attributes.core;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_sys_status;
import com.flightcontroller.model.DroneAttribute;

/**
 * This call holds the battery information of the copter
 *
 */
public class Battery extends DroneAttribute {

	private float voltage_;
	private float current_;
	private float remaining_;
	
	@Override
	public int[] getMessageHandleTypes() {
		return new int[] { msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS };
	}

	@Override
	public void handleMessage(MAVLinkMessage msg) {
		msg_sys_status m_sys = (msg_sys_status) msg;
		voltage_ = m_sys.voltage_battery / 1000.0f;
		remaining_ = m_sys.battery_remaining;
		current_ = m_sys.current_battery / 100.0f;
	}
	
	public float getVoltage() {
		return voltage_;
	}
	
	public float getCurrent() {
		return current_;
	}
	
	public float getRemaining() {
		return remaining_;
	}

	@Override
	public String getIdentifier() {
		return "Battery";
	}

}
