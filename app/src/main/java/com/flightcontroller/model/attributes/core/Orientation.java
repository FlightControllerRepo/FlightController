package com.flightcontroller.model.attributes.core;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_vfr_hud;
import com.flightcontroller.model.DroneAttribute;
import com.flightcontroller.model.DroneEvent;
import com.flightcontroller.model.DroneImp;

public class Orientation extends DroneAttribute {

	private float altitude_;
    private float targetAltitude_;

	private float pitch_;
	private float roll_;

    private float targetYaw_;
	private float yaw_;

    public Orientation() {
        targetYaw_ = -1;
        targetAltitude_ = -1;
    }

	@Override
	public int[] getMessageHandleTypes() {
		return new int[] {  
							msg_attitude.MAVLINK_MSG_ID_ATTITUDE, 
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

                if (targetYaw_ == -1) targetYaw_ = yaw_;
				break;
			case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
				msg_vfr_hud m_hud = (msg_vfr_hud) msg;
                float oldalt = altitude_;
				altitude_ = m_hud.alt;

                if (oldalt < 3 && m_hud.alt >= 3)
                    DroneImp.INSTANCE.postEvent(DroneEvent.LAUNCHED);
                else if (oldalt >= 1.5f && m_hud.alt < 1.5f)
                    DroneImp.INSTANCE.postEvent(DroneEvent.LANDED);

                if (targetAltitude_ == -1) targetAltitude_ = altitude_;
				break;
		}
	}

    public void setTargetAltitude(float altitude) { targetAltitude_ = altitude; }

    public float getTargetAltitude() { return targetAltitude_; }

	public float getAltitude() {
		return altitude_;
	}
	
	public float getPitch() { 
		return pitch_;
	}
	
	public float getRoll() { 
		return roll_;
	}

    public void setTargetYaw(float targetYaw) { targetYaw_ = targetYaw; }

    public float getTargetYaw() { return targetYaw_; }

	public float getYaw() {
		return yaw_;
	}
	
	@Override
	public String getIdentifier() {
		return "Orientation";
	}

}
