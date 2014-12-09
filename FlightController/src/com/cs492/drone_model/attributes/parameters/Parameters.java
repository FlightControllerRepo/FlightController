package com.cs492.drone_model.attributes.parameters;

import java.util.TreeMap;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_param_value;
import com.cs492.drone_model.Drone;
import com.cs492.drone_model.DroneEvent;
import com.cs492.drone_model.DroneEvent.DroneEventListener;
import com.cs492.drone_model.DroneVariable;
import com.cs492.drone_model.implementation.DroneObject;
import com.cs492.flightcontroller.LogManager;
import com.cs492.flightcontroller.LogManager.LogSeverity;
import com.cs492.mavlink.command_wrappers.MavLinkParameters;

/**
 * Class to manage the communication of parameters to the MAV, These
 * parameters DONT include battery, altitude, but rather hardware/firmware
 * specific settings.
 */
public class Parameters extends DroneVariable implements DroneEventListener {

	private TreeMap<Integer, Parameter> parameters;

	public Parameters() {
		super();
		parameters = new TreeMap<Integer, Parameter>();
	}

	@Override
	public void setDrone(Drone owner) {
		super.setDrone(owner);
		owner.addDroneEventListener(this);
	}
	
	public void refreshParameters() {
		drone_.sendPacket(MavLinkParameters.getRequestParametersListPacket(drone_));
	}

	public void sendParameter(Parameter parameter) {
		drone_.sendPacket(MavLinkParameters.getSendParameterPacket(parameter, drone_));
	}

	public void ReadParameter(String name) {
		drone_.sendPacket(MavLinkParameters.getReadParameterPacket(name, drone_));
	}

	public Parameter getParameter(String name) {
		synchronized(parameters) {
			for (int key : parameters.keySet()) {
				if (parameters.get(key).name.equalsIgnoreCase(name))
					return new Parameter(parameters.get(key));
			}
		}
		return null;
	}

	@Override
	public void onDroneEvent(DroneEvent event, Drone drone) {
		switch (event) {
		case HEARTBEAT_FIRST:
			refreshParameters();
			break;
		default:
			break;
		}
	}

	@Override
	public int[] getMessageHandleTypes() {
		return new int[] { msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE };
	}

	@Override
	public void handleMessage(MAVLinkMessage msg) {
		msg_param_value param_msg = (msg_param_value) msg;
		Parameter param = new Parameter(param_msg);
			
		synchronized(parameters) {
			parameters.put((int) param_msg.param_index, param);
		}
		
		if (param.name.equalsIgnoreCase("FS_THR_ENABLE")) {
			Parameter fsparam = getParameter("FS_THR_ENABLE");
			fsparam.value = 0;
			DroneObject.INSTANCE.sendPacket(MavLinkParameters.getSendParameterPacket(fsparam, DroneObject.INSTANCE));
		}
	}
	
	@Override
	public String getIdentifier() {
		return "Parameters";
	}

}
