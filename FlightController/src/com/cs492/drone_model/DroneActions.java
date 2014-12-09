package com.cs492.drone_model;

import com.MAVLink.Messages.ApmModes;
import com.cs492.flightcontroller.LogManager;
import com.cs492.flightcontroller.LogManager.LogSeverity;
import com.cs492.mavlink.command_wrappers.MavLinkArm;
import com.cs492.mavlink.command_wrappers.MavLinkModes;
import com.cs492.mavlink.command_wrappers.MavLinkTakeoff;

public class DroneActions {

	public static void arm(Drone drone) {
		LogManager.INSTANCE.addEntry("Arming!", LogSeverity.INFO);
		if (drone.isArmed()) return;
		
		drone.sendPacket(MavLinkArm.getArmMessagePacket(true, drone));
	}
	
	public static void disarm(Drone drone) {
		if (!drone.isArmed()) return;
		drone.sendPacket(MavLinkArm.getArmMessagePacket(false, drone));
	}
	
	public static void guidedTakeoff(Drone drone, float altmeters) {
		//if (!drone.isInAir()) return;
		drone.sendPacket(MavLinkModes.getChangeFlightModePacket(ApmModes.ROTOR_GUIDED, drone));
		drone.sendPacket(MavLinkTakeoff.getTakeoffPacket(drone, altmeters));
	}
	
	public static void guidedLand(Drone drone) {
		//if (drone.isInAir()) return;
		drone.sendPacket(MavLinkModes.getChangeFlightModePacket(ApmModes.ROTOR_LAND, drone));
	}
	
}
