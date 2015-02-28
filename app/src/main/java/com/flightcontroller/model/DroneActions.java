package com.flightcontroller.model;


import com.MAVLink.Messages.ApmModes;
import com.flightcontroller.MainActivity;
import com.flightcontroller.mavlink.command_wrappers.MavLinkArm;
import com.flightcontroller.mavlink.command_wrappers.MavLinkGuidedMode;
import com.flightcontroller.mavlink.command_wrappers.MavLinkTakeoff;
import com.flightcontroller.model.attributes.core.Battery;
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

    public static void goToAltitude(Drone drone, float altmeters) {
        if (!drone.isInAir()) return;

        //truncate altitude to at least 0
        altmeters = Math.max(0, altmeters);

        Orientation orien = (Orientation) drone.getDroneAttribute("Orientation");
        orien.setTargetAltitude(altmeters);

        final float finalAltmeters = altmeters;
        MainActivity.getMainContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DecimalFormat dec = new DecimalFormat("#.##");
                SnackbarManager.show(
                        Snackbar.with(MainActivity.getMainContext())
                                .text("Moving to altitude " + finalAltmeters),
                        MainActivity.getMainContext());
            }
        });
        LogManager.INSTANCE.addEntry("Moving to altitude " + altmeters, LogSeverity.INFO);
        drone.sendPacket(MavLinkGuidedMode.getWaypointPacket(drone, 0, 0, altmeters));
    }

    /**
     * This method attempts to move the given drone to the given GPS coordinate.
     * The drone will move to the TARGET ALTITUDE set in the Orientation DroneAttribute
     *
     * @param drone
     * @param location location to move to
     */
    public static void goToPosition(Drone drone, final LatLng location) {
        //if (!drone.isInAir()) return;

        Orientation orien = (Orientation) drone.getDroneAttribute("Orientation");
        final float altitude = orien.getTargetAltitude();

        MainActivity.getMainContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DecimalFormat dec = new DecimalFormat("#.##");
                SnackbarManager.show(
                        Snackbar.with(MainActivity.getMainContext())
                                .text("Moving to (" + dec.format(location.latitude) + ", " +
                                        dec.format(location.longitude) + ")" + " at alt " +
                                        dec.format(altitude)),
                        MainActivity.getMainContext());
            }
        });

        LogManager.INSTANCE.addEntry("Moving to " + location +
                " at alt " + altitude, LogSeverity.INFO);
        drone.sendPacket(MavLinkGuidedMode.getChangeFlightModePacket(ApmModes.ROTOR_GUIDED, drone));
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
     * @param bearing the angle at which to go (0 is forward/north for relative true/false)
     * @param relative true if this bearing should be relative to the copters current bearing
     */
    public static void goForwardByBearing(Drone drone, float meters,
                                          float bearing, boolean relative) {
        GPSPosition gps = (GPSPosition) drone.getDroneAttribute("GPSPosition");
        Orientation orien = (Orientation) drone.getDroneAttribute("Orientation");

        LatLng currentPos = new LatLng(gps.getLatitude(), gps.getLongitude());
        float gobearing = (relative) ? orien.getTargetYaw() + bearing : bearing;

        LatLng pos = GeoTools.newCoordFromBearingAndDistance(currentPos, gobearing, meters);
        goToPosition(drone, pos);
    }

	public static void turn(Drone drone, int degrees, boolean relative) {
		Orientation orien  = (Orientation) drone.getDroneAttribute("Orientation");
        orien.setTargetYaw(relative ? orien.getTargetYaw() + degrees : degrees);

        drone.sendPacket(MavLinkGuidedMode.getChangeYawPacket(drone, orien.getTargetYaw()));
	}

	public static ArrayList<String> getStatusText(Drone drone) {
		ArrayList<String> array = new ArrayList<String>();

        DecimalFormat df = new DecimalFormat("####.#");
		HeartbeatMonitor mon = (HeartbeatMonitor) drone.getDroneAttribute("HeartbeatMonitor");
		Orientation or = (Orientation) drone.getDroneAttribute("Orientation");
        Battery bat = (Battery) drone.getDroneAttribute("Battery");
        array.add("Is armed:" + (mon.isArmed() ? "Yes" : "No"));
		array.add("Altitude:" + df.format(or.getAltitude()));
        array.add("Battery:" + df.format(bat.getVoltage()));
		array.add("Yaw:" + df.format(or.getYaw()));
		array.add("Pitch:" + df.format(or.getPitch()));
		array.add("Roll:" + df.format(or.getRoll()));
		
		return array;
	}
	
}
