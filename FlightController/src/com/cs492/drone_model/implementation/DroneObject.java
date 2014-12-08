package com.cs492.drone_model.implementation;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.enums.MAV_DATA_STREAM;
import com.cs492.drone_model.Drone;
import com.cs492.drone_model.DroneAttribute;
import com.cs492.drone_model.DroneEvent;
import com.cs492.drone_model.DroneEventListener;
import com.cs492.drone_model.attributes.HeartbeatMonitor;
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
	
	private ConcurrentSkipListMap<Integer, ArrayList<DroneAttribute> > messageHandlers_;
	private ConcurrentSkipListMap<String, DroneAttribute> attributes_;
	
	private DroneObject() {
		listeners_ = new CopyOnWriteArrayList<DroneEventListener>();
		
		sysid_ = componentid_ = -1;
		connected_ = false;
		attributes_ = new ConcurrentSkipListMap<String, DroneAttribute>();
		messageHandlers_ = new ConcurrentSkipListMap<Integer, ArrayList<DroneAttribute> >();
	}
	
	@Override
	public void connect() {
		if (connection_ == null)
			connection_ = new UsbConnection(MainActivity.getMainContext());
		
		connection_.addMavLinkConnectionListener(this);
		connection_.connect();
	}
	
	@Override
	public void setupComponents() {
		LogManager.INSTANCE.addEntry("Setting up components", LogSeverity.INFO);
		addDroneAttribute(new Parameters());
		addDroneAttribute(new HeartbeatMonitor());
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
		if (event == DroneEvent.HEARTBEAT_FIRST) {
			sendPacket(MavLinkModes.changeFlightMode(ApmModes.ROTOR_LOITER, this));
			sendPacket(MavLinkStreamRates.getStreamRequestPacket(this, MAV_DATA_STREAM.MAV_DATA_STREAM_POSITION, 500));
			sendPacket(MavLinkStreamRates.getStreamRequestPacket(this, MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS, 500));
		}
		
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
	public void addDroneAttribute(DroneAttribute attrib) {
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
	}
	
	@Override
	public void onDisconnect() {
		connected_ = false;
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

	public boolean isIntilized() {
		return attributes_.size() > 0;
	}
	
}
