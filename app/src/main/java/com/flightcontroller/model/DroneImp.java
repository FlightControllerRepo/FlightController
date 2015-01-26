package com.flightcontroller.model;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.enums.MAV_DATA_STREAM;
import com.flightcontroller.MainActivity;
import com.flightcontroller.mavlink.MavLinkConnectionListener;
import com.flightcontroller.mavlink.command_wrappers.MavLinkGuidedMode;
import com.flightcontroller.mavlink.command_wrappers.MavLinkStreamRates;
import com.flightcontroller.mavlink.usb.UsbConnection;
import com.flightcontroller.model.DroneEvent.DroneEventListener;
import com.flightcontroller.model.attributes.core.Battery;
import com.flightcontroller.model.attributes.core.GPSPosition;
import com.flightcontroller.model.attributes.core.HeartbeatMonitor;
import com.flightcontroller.model.attributes.core.Orientation;
import com.flightcontroller.model.attributes.parameters.Parameters;
import com.flightcontroller.utils.LogManager;
import com.flightcontroller.utils.LogManager.LogSeverity;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Our Drone implementation. This class is implemented as a singleton, since
 * the original specification on required us to command a single drone. However
 * it should not be hard to extend functionality to multiple Copters, using
 * this class a a starting point.
 *
 */
public enum DroneImp implements Drone, MavLinkConnectionListener {
	INSTANCE;

	private CopyOnWriteArrayList<DroneEventListener> listeners_;
	
	private boolean connected_;
	private UsbConnection connection_;
	
	private byte sysid_;
	private byte componentid_;
	
	private ConcurrentSkipListMap<Integer, ArrayList<DroneAttribute> > messageHandlers_;
	private ConcurrentSkipListMap<String, DroneAttribute> attributes_;
	
	private DroneImp() {
		listeners_ = new CopyOnWriteArrayList<DroneEventListener>();
		
		sysid_ = componentid_ = -1;
		
		connected_ = false;
		attributes_ = new ConcurrentSkipListMap<String, DroneAttribute>();
		messageHandlers_ = new ConcurrentSkipListMap<Integer, ArrayList<DroneAttribute> >();
        setupCoreAttributes();
	}
	
	@Override
	public void connectRadio() {
		if (connection_ == null)
			connection_ = new UsbConnection(MainActivity.getMainContext());
		
		connection_.addMavLinkConnectionListener(this);
		connection_.connect();
	}
	
	@Override
	public void disconnectRadio() {
		connection_.disconnect();
	}

	private void setupCoreAttributes() {
		LogManager.INSTANCE.addEntry("Setting up components", LogSeverity.INFO);
		addDroneVariable(new Battery());
		addDroneVariable(new Parameters());
		addDroneVariable(new HeartbeatMonitor());
		addDroneVariable(new GPSPosition());
		addDroneVariable(new Orientation());
	}

	@Override
	public void postEvent(final DroneEvent event) {
        if (event.shouldNotify()) {
            MainActivity.getMainContext().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SnackbarManager.show(
                            Snackbar.with(MainActivity.getMainContext())
                                    .text(event.getText()), MainActivity.getMainContext());
                }
            });
        }

        if (event == DroneEvent.HEARTBEAT_FIRST) {
            sendPacket(MavLinkGuidedMode.getChangeFlightModePacket(ApmModes.ROTOR_LOITER, this));
            sendPacket(MavLinkStreamRates.getStreamRequestPacket(this, MAV_DATA_STREAM.MAV_DATA_STREAM_POSITION, 500));
            sendPacket(MavLinkStreamRates.getStreamRequestPacket(this, MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS, 500));
        }

        LogManager.INSTANCE.addEntry("Posting event" + event.name(), LogSeverity.INFO);
        for (DroneEventListener lis : listeners_)
            lis.onDroneEvent(event, this);
    }
	
	@Override
	public void sendPacket(MAVLinkPacket packet) {
		if (connected_) 
			connection_.sendMavPacket(packet);
	}

	@Override
	public void addDroneEventListener(DroneEventListener listener) {
		listeners_.add(listener);
	}
	
	@Override
	public void removeDroneEventListener(DroneEventListener listener) {
		listeners_.remove(listener);
	}
	
	@Override 
	public void addDroneVariable(DroneAttribute attrib) {
		attrib.setDrone(this);
		for (Integer type : attrib.getMessageHandleTypes()) {
			if (!messageHandlers_.containsKey(type))
				messageHandlers_.put(type, new ArrayList<DroneAttribute>());
		
			if (!messageHandlers_.get(type).contains(attrib))
				messageHandlers_.get(type).add(attrib);
		}
		attributes_.put(attrib.getIdentifier(), attrib);
	}
	
	public DroneAttribute getDroneAttribute(String string) {
		return attributes_.get(string);
	}
	
	@Override
	public void onConnect() {
		connected_ = true;
		postEvent(DroneEvent.RADIO_CONNECTED);
	}
	
	@Override
	public void onDisconnect() {
		connected_ = false;
		postEvent(DroneEvent.RADIO_DISCONNECTED);
	}

	@Override
	public void onReceiveMessage(MAVLinkMessage msg) {
		if (componentid_ == -1) {
			componentid_ = (byte) msg.compid;
			sysid_ = (byte) msg.sysid;
		}
		
		try {
			if (!messageHandlers_.containsKey(msg.msgid)) return;
			for (DroneAttribute v : messageHandlers_.get(msg.msgid))
				v.handleMessage(msg);
		} catch (Exception ex) { LogManager.INSTANCE.addEntry(LogManager.stringFromException(ex), LogSeverity.ERROR); }
	}

	@Override
	public void onComError(String errMsg) { }

	@Override
	public byte getSysid() {
		return sysid_;
	}

	@Override
	public byte getCompid() {
		return componentid_;
	}
	
	@Override
	public boolean isInitialized() {
		return attributes_.size() > 0;
	}
	
	@Override
	public boolean isConnected() { 
		return connected_;
	}

	@Override
	public boolean isArmed() {
		return ((HeartbeatMonitor) getDroneAttribute("HeartbeatMonitor")).isArmed();
	}

	@Override
	public boolean isInAir() {
		return false;
	}

	public void addUSBListener(MavLinkConnectionListener listener) {
		if (connection_ == null)
			connection_ = new UsbConnection(MainActivity.getMainContext());
		
		connection_.addMavLinkConnectionListener(listener);
	}
	
}
