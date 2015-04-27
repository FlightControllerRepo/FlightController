package com.flightcontroller.model.attributes.core;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_statustext;
import com.flightcontroller.MainActivity;
import com.flightcontroller.model.DroneAttribute;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

public class CollisionHandler extends DroneAttribute {

    private final static int REPORT_INTERVAL = 1000;
	private long lastReportTime_;
	
	@Override
	public int[] getMessageHandleTypes() {
		return new int[] { msg_statustext.MAVLINK_MSG_ID_STATUSTEXT };
	}

	@Override
	public void handleMessage(MAVLinkMessage msg) {
        final msg_statustext m_sys = (msg_statustext) msg;
		if (m_sys.getText().contains("Collision")) {
            if (System.currentTimeMillis() - lastReportTime_ > REPORT_INTERVAL) {
                MainActivity.getMainContext().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SnackbarManager.show(
                                Snackbar.with(MainActivity.getMainContext())
                                        .text("Collision Detected:" + m_sys.getText()),
                                MainActivity.getMainContext());
                    }
                });

                lastReportTime_ = System.currentTimeMillis();
            }
        }
	}

	@Override
	public String getIdentifier() {
		return "CollisionHandler";
	}

}
