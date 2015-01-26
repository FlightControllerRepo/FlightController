package com.flightcontroller.model;


import com.MAVLink.Messages.ApmModes;
import com.flightcontroller.MainActivity;
import com.flightcontroller.mavlink.command_wrappers.MavLinkArm;
import com.flightcontroller.mavlink.command_wrappers.MavLinkGuidedMode;
import com.flightcontroller.mavlink.command_wrappers.MavLinkTakeoff;
import com.flightcontroller.model.attributes.core.GPSPosition;
import com.flightcontroller.model.attributes.core.HeartbeatMonitor;
import com.flightcontroller.model.attributes.core.Orientation;
import com.flightcontroller.utils.GeoTools;
import com.flightcontroller.utils.LogManager;
import com.flightcontroller.utils.LogManager.LogSeverity;
import com.google.android.gms.maps.model.LatLng;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class provides wrapper methods around a few core actions that a drone
 * should be capable of. Although this app was designed around a singleton Drone object,
 * this method takes in a Drone object, such that the app may be extended to handle multiple Drones
 * later.
 *
 */
public class DroneActions {

    /**
     * This method attempts to arm the given drone. Obviously communication with the drone
     * is done asynchronously through radio, and as such this method has a timeout, that will
     * post the ARM_DISARM_TIMEOUT event, if the drone is not armed after 5 seconds.
     *
     * @param drone
     */
	public static void arm(final Drone drone) {
		LogManager.INSTANCE.addEntry("Arming!", LogSeverity.INFO);
		if (drone.isArmed()) return;
		
		LogManager.INSTANCE.addEntry("Sending arming message!", LogSeverity.INFO);
		drone.sendPacket(MavLinkArm.getArmMessagePacket(true, drone));

        Timer timeout = new Timer();
        timeout.schedule(new TimerTask() {

            @Override
            public void run() {
                if (!drone.isArmed())
                    drone.postEvent(DroneEvent.ARM_TIMEOUT);
            }
        }, 5000);
	}

    /**
     * This method attempts to disarm the given drone. Obviously communication with the drone
     * is done asynchronously through radio, and as such this method has a timeout, that will
     * post the ARM_DISARM_TIMEOUT event, if the drone is not disarmed after 5 seconds.
     *
     * @param drone
     */
	public static void disarm(final Drone drone) {
		if (!drone.isArmed()) return;
		drone.sendPacket(MavLinkArm.getArmMessagePacket(false, drone));

        Timer timeout = new Timer();
        timeout.schedule(new TimerTask() {

            @Override
            public void run() {
                if (drone.isArmed())
                    drone.postEvent(DroneEvent.ARM_TIMEOUT);
            }
        }, 5000);
	}

    /**
     * This method attempts make the given drone takeoff. The drone will takeoff to the given
     * altitude, and as a consequence, will set the targetAltitude in the Orientation DroneAttribute
     * to the given value.
     *
     * @param drone
     * @param altmeters
     */
	public static void guidedTakeoff(Drone drone, float altmeters) {
		//if (drone.isInAir()) return;
		altmeters = Math.max(3.0f, altmeters);
        Orientation orien = (Orientation) drone.getDroneAttribute("Orientation");
        orien.setTargetAltitude(altmeters);

		drone.sendPacket(MavLinkGuidedMode.getChangeFlightModePacket(ApmModes.ROTOR_GUIDED, drone));
		drone.sendPacket(MavLinkTakeoff.getTakeoffPacket(drone, altmeters));
	}

    /**
     * This methods attempts to lands the given drone.
     *
     * @param drone
     */
	public static void guidedLand(Drone drone) {
		//if (!drone.isInAir()) return;
		drone.sendPacket(MavLinkGuidedMode.getChangeFlightModePacket(ApmModes.ROTOR_LAND, drone));
	}

    /**
     * This method attempts to move the given drone to the given GPS coordinate.
     * The drone will move to the TARGET ALTITUDE set in the Orientation DroneAttribute
     *
     * @param drone
     * @param location location to move to
     */
    public static void moveToPosition(Drone drone, final LatLng location) {
        //if (!drone.isInAir()) return;

        MainActivity.getMainContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DecimalFormat dec = new DecimalFormat("#.##");
                SnackbarManager.show(
                        Snackbar.with(MainActivity.getMainContext())
                                .text("Moving to (" + dec.format(location.latitude) + ", " +
                                        dec.format(location.longitude) + ")"),
                        MainActivity.getMainContext());
            }
        });

        LogManager.INSTANCE.addEntry("Moving to " + location, LogSeverity.INFO);
        drone.sendPacket(MavLinkGuidedMode.getChangeFlightModePacket(ApmModes.ROTOR_GUIDED, drone));

        Orientation orien = (Orientation) drone.getDroneAttribute("Orientation");
        float altitude = orien.getTargetAltitude();

        drone.sendPacket(MavLinkGuidedMode.getWaypointPacket(drone, location.latitude,
                location.longitude, altitude));
    }

    /**
     * Attempts to move the drone forward by the given amount. Forward is defined by the
     * TARGET YAW ANGLE defined in the Orientation DroneAttribute. The drone will move to the
     * TARGET ALTITUDE set in the Orientation DroneAttribute.
     *
     * @param drone
     * @param meters amount to move forward
     */
    public static void moveForward(Drone drone, float meters) {
        GPSPosition gps = (GPSPosition) drone.getDroneAttribute("GPSPosition");
        Orientation orien = (Orientation) drone.getDroneAttribute("Orientation");

        LatLng currentPos = new LatLng(gps.getLatitude(), gps.getLongitude());
        LatLng pos = GeoTools.newCoordFromBearingAndDistance(currentPos, orien.getTargetYaw(), meters);
        moveToPosition(drone, pos);
    }

	public static void turn(Drone drone, int degrees) {
		//drone.sendPacket(MavLinkModes.getYawChangePacket(drone, degrees));
	}

	public static ArrayList<String> getStatusText(Drone drone) {
		ArrayList<String> array = new ArrayList<String>();
		
		HeartbeatMonitor mon = (HeartbeatMonitor) drone.getDroneAttribute("HeartbeatMonitor");
		array.add("Is armed:" + (mon.isArmed() ? "Yes" : "No"));
		Orientation or = (Orientation) drone.getDroneAttribute("Orientation");
		array.add("Altitude:" + or.getAltitude());
		array.add("Yaw:" + or.getYaw());
		array.add("Pitch:" + or.getPitch());
		array.add("Roll:" + or.getRoll());
		
		return array;
	}
	
}