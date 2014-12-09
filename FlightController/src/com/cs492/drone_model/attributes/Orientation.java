package com.cs492.drone_model.attributes;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_vfr_hud;
import com.cs492.drone_model.DroneVariable;

public class Orientation extends DroneVariable {

	private float altitude_;
	private float pitch_;
	private float roll_;
	private float yaw_;
	
	@Override
	public int[] getMessageHandleTypes() {
		return new int[] {  msg_attitude.MAVLINK_MSG_ID_ATTITUDE, 
							msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD,
						 };
	}

	@Override
	public void handleMessage(MAVLinkMessage msg) {
		switch (msg.msgid) {
			case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
				msg_attitude m_att = (msg_attitude) msg;
				roll_ = (float) (m_att.roll * 180.0 / Math.PI);
				pitch_ = (float) (m_att.pitch * 180.0 / Math.PI);
				yaw_ = (float) (m_att.yaw * 180.0 / Math.PI);
				break;
			case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
				msg_vfr_hud m_hud = (msg_vfr_hud) msg;
				altitude_ = m_hud.alt;
				break;
		}
	}	

	public float getAltitude() {
		return altitude_;
	}
	
	public float getPitch() { 
		return pitch_;
	}
	
	public float getRoll() { 
		return roll_;
	}
	
	public float getYaw() {
		return yaw_;
	}
	
	@Override
	public String getIdentifier() {
		return "Orientation";
	}

}
