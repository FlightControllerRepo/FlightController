package com.cs492.drone_model.implementation;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.enums.MAV_DATA_STREAM;
import com.cs492.drone_model.Drone;
import com.cs492.drone_model.DroneEvent;
import com.cs492.drone_model.DroneEvent.DroneEventListener;
import com.cs492.drone_model.DroneVariable;
import com.cs492.drone_model.attributes.GPSPosition;
import com.cs492.drone_model.attributes.HeartbeatMonitor;
import com.cs492.drone_model.attributes.Orientation;
import com.cs492.drone_model.attributes.parameters.Parameters;
import com.cs492.flightcontroller.LogManager;
import com.cs492.flightcontroller.LogManager.LogSeverity;
import com.cs492.flightcontroller.MainActivity;
import com.cs492.mavlink.command_wrappers.MavLinkModes;
import com.cs492.mavlink.command_wrappers.MavLinkStreamRates;
import com.cs492.mavlink.usb.UsbConnection;
import com.cs492.mavlink_connection.MavLinkConnectionListener;

public enum DroneObject implements Drone, MavLinkConnectionListener {
	INSTANCE;

	private CopyOnWriteArrayList<DroneEventListener> listeners_;
	
	private boolean connected_;
	private UsbConnection connection_;
	
	private byte sysid_;
	private byte componentid_;
	
	private ConcurrentSkipListMap<Integer, ArrayList<DroneVariable> > messageHandlers_;
	private ConcurrentSkipListMap<String, DroneVariable> attributes_;
	
	private DroneObject() {
		listeners_ = new CopyOnWriteArrayList<DroneEventListener>();
		
		sysid_ = componentid_ = -1;
		connected_ = false;
		attributes_ = new ConcurrentSkipListMap<String, DroneVariable>();
		messageHandlers_ = new ConcurrentSkipListMap<Integer, ArrayList<DroneVariable> >();
	}
	
	@Override
	public void connect() {
		if (connection_ == null)
			connection_ = new UsbConnection(MainActivity.getMainContext());
		
		connection_.addMavLinkConnectionListener(this);
		connection_.connect();
	}
	
	public void disconnect() {
		connection_.disconnect();
	}
	
	@Override
	public void setupComponents() {
		LogManager.INSTANCE.addEntry("Setting up components", LogSeverity.INFO);
		addDroneVariable(new Parameters());
		addDroneVariable(new HeartbeatMonitor());
		addDroneVariable(new GPSPosition());
		addDroneVariable(new Orientation());
	}
	
	@Override
	public byte getSysid() {
		return sysid_;
	}

	@Override
	public byte getCompid() {
		return componentid_;
	}

	@Override
	public void postEvent(DroneEvent event) {
		try {
		if (event == DroneEvent.HEARTBEAT_FIRST) {
			sendPacket(MavLinkModes.getChangeFlightModePacket(ApmModes.ROTOR_LOITER, this));
			sendPacket(MavLinkStreamRates.getStreamRequestPacket(this, MAV_DATA_STREAM.MAV_DATA_STREAM_POSITION, 500));
			sendPacket(MavLinkStreamRates.getStreamRequestPacket(this, MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS, 500));
		}
		
		LogManager.INSTANCE.addEntry("POSTING event" + event.name(), LogSeverity.INFO);
		for (DroneEventListener lis : listeners_)
			lis.onDroneEvent(event, this);
		} catch (Exception ex) { LogManager.INSTANCE.addEntry(LogManager.stringFromException(ex), LogSeverity.ERROR); }
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
	public void addDroneVariable(DroneVariable attrib) {
		attrib.setDrone(this);
		for (Integer type : attrib.getMessageHandleTypes()) {
			if (!messageHandlers_.containsKey(type))
				messageHandlers_.put(type, new ArrayList<DroneVariable>());
		
			if (!messageHandlers_.get(type).contains(attrib))
				messageHandlers_.get(type).add(attrib);
		}
		attributes_.put(attrib.getIdentifier(), attrib);
	}
	
	public DroneVariable getDroneAttribute(String string) {
		return attributes_.get(string);
	}
	
	@Override
	public void onConnect() {	
		connected_ = true;
		postEvent(DroneEvent.CONNECTED);
	}
	
	@Override
	public void onDisconnect() {
		connected_ = false;
		postEvent(DroneEvent.DISCONNECTED);
	}

	@Override
	public void onReceiveMessage(MAVLinkMessage msg) {
		if (componentid_ == -1) {
			componentid_ = (byte) msg.compid;
			sysid_ = (byte) msg.sysid;
		}
		
		try {
			if (!messageHandlers_.containsKey(msg.msgid)) return;
			for (DroneVariable v : messageHandlers_.get(msg.msgid))
				v.handleMessage(msg);
		} catch (Exception ex) { LogManager.INSTANCE.addEntry(LogManager.stringFromException(ex), LogSeverity.ERROR); }
	}

	@Override
	public void onComError(String errMsg) { }

	public boolean isIntilized() {
		return attributes_.size() > 0;
	}
	
	public boolean isConnected() { 
		return connected_;
	}

	public boolean isArmed() {
		return ((HeartbeatMonitor) getDroneAttribute("HeartbeatMonitor")).isArmed();
	}

	public boolean isInAir() {
		return false;
	}

	public void addUSBListener(MavLinkConnectionListener listener) {
		if (connection_ == null)
			connection_ = new UsbConnection(MainActivity.getMainContext());
		
		connection_.addMavLinkConnectionListener(listener);
	}
	
}
