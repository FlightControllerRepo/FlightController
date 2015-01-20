package com.flightcontroller.model.attributes.parameters;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_param_value;
import com.flightcontroller.mavlink.command_wrappers.MavLinkParameters;
import com.flightcontroller.model.Drone;
import com.flightcontroller.model.DroneAttribute;
import com.flightcontroller.model.DroneEvent;
import com.flightcontroller.model.DroneEvent.DroneEventListener;
import com.flightcontroller.model.DroneImp;

import java.util.TreeMap;

/**
 * Class to manage the communication of parameters to the MAV, These
 * parameters DONT include battery, altitude, but rather hardware/firmware
 * specific settings.
 */
public class Parameters extends DroneAttribute implements DroneEventListener {

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
			DroneImp.INSTANCE.sendPacket(MavLinkParameters.getSendParameterPacket(fsparam, DroneImp.INSTANCE));
		}
	}
	
	@Override
	public String getIdentifier() {
		return "Parameters";
	}

}
