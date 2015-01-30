package com.flightcontroller.utils;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Nicholas on 1/19/15.
 */
public class GeoTools {

    private static final double RADIUS_OF_EARTH = 6378137.0;

    public static LatLng newCoordFromBearingAndDistance(LatLng origin, double bearing,
                                                         double distance) {
        double lat = origin.latitude;
        double lon = origin.longitude;
        double lat1 = Math.toRadians(lat);
        double lon1 = Math.toRadians(lon);
        double brng = Math.toRadians(bearing);
        double dr = distance / RADIUS_OF_EARTH;

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dr) + Math.cos(lat1) * Math.sin(dr)
                * Math.cos(brng));
        double lon2 = lon1
                + Math.atan2(Math.sin(brng) * Math.sin(dr) * Math.cos(lat1),
                Math.cos(dr) - Math.sin(lat1) * Math.sin(lat2));

        return (new LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2)));
    }

    public static float warpToPositiveAngle(float degree) {
        if (degree >= 0) {
            return degree;
        } else {
            return 360 + degree;
        }
    }

}
