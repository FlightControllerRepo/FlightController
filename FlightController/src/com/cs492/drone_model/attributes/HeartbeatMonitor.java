package com.cs492.drone_model.attributes;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.enums.MAV_MODE_FLAG;
import com.cs492.drone_model.DroneEvent;
import com.cs492.drone_model.DroneVariable;
import com.cs492.flightcontroller.LogManager;
import com.cs492.flightcontroller.LogManager.LogSeverity;

public class HeartbeatMonitor extends DroneVariable {

	private boolean firstHeartbeat_;
	private msg_heartbeat previousHeartbeat_;
	
	public HeartbeatMonitor() {
		firstHeartbeat_ = true;
	}
	
	@Override
	public int[] getMessageHandleTypes() {
		return new int[] { msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT };
	}

	@Override
	public void handleMessage(MAVLinkMessage msg) {
		if (firstHeartbeat_) {
			LogManager.INSTANCE.addEntry("Receieved first heartbeat!", LogSeverity.INFO);
			drone_.postEvent(DroneEvent.HEARTBEAT_FIRST);
		} else {
			msg_heartbeat currentHeartbeat_ = (msg_heartbeat) msg;
			boolean currentArmed = (currentHeartbeat_.base_mode & (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED) ==
									(byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED;
			boolean prevArmed = (previousHeartbeat_.base_mode & (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED) ==
					(byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED;
			if (prevArmed != currentArmed)
				drone_.postEvent(currentArmed ? DroneEvent.ARMED : DroneEvent.DISARMED);
		}
			
		previousHeartbeat_ = (msg_heartbeat) msg;
		firstHeartbeat_ = false;
	}

	public boolean isArmed() {
		if (previousHeartbeat_ == null)
			return false;		
		
		return (previousHeartbeat_.base_mode & (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED) ==
				(byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED;
	}
	
	@Override
	public String getIdentifier() {
		return "HeartbeatMonitor";
	}

}
