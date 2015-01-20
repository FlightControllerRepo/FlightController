package com.flightcontroller.model.attributes.core;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.enums.MAV_MODE_FLAG;
import com.flightcontroller.utils.LogManager;
import com.flightcontroller.utils.LogManager.LogSeverity;
import com.flightcontroller.model.DroneAttribute;
import com.flightcontroller.model.DroneEvent;

import java.util.Timer;
import java.util.TimerTask;

public class HeartbeatMonitor extends DroneAttribute {
	
	private static final int HEARTBEAT_TIMEOUT = 10000;
	private Timer heartbeatTimeout_;
	
	private boolean firstHeartbeat_;
	private msg_heartbeat previousHeartbeat_;
	
	public HeartbeatMonitor() {
		heartbeatTimeout_ = new Timer();
		firstHeartbeat_ = true;
	}
	
	private void heartbeatTimeout() {
		previousHeartbeat_ = null;
		firstHeartbeat_ = true;
		drone_.postEvent(DroneEvent.HEARTBEAT_ENDED);
	}
	
	@Override
	public int[] getMessageHandleTypes() {
		return new int[] { msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT };
	}

	@Override
	public void handleMessage(MAVLinkMessage msg) {
		heartbeatTimeout_.cancel();
		
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
			
		heartbeatTimeout_.schedule(new TimerTask() {
			public void run() {
				heartbeatTimeout();
			}
		}, HEARTBEAT_TIMEOUT);
		
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
