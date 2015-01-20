package com.flightcontroller.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flightcontroller.MainActivity;
import com.flightcontroller.R;
import com.flightcontroller.model.DroneActions;
import com.flightcontroller.model.DroneImp;
import com.flightcontroller.model.attributes.core.GPSPosition;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Timer;
import java.util.TimerTask;

public class MiniMapFragment extends MapFragment implements GoogleMap.OnMapClickListener {

    private static final String SUPPORT_MAP_BUNDLE_KEY = "MapOptions";

    private Timer copterUpdator_;

    private Marker copterMarker_;
    private Location currentLocation_;
    private GoogleMap googleMap_;

    public static MiniMapFragment newInstance() {
        return new MiniMapFragment();
    }

    public static MiniMapFragment newInstance(GoogleMapOptions options) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(SUPPORT_MAP_BUNDLE_KEY, options);

        MiniMapFragment fragment = new MiniMapFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        googleMap_ = this.getMap();
        googleMap_.setMyLocationEnabled(true);
        googleMap_.setOnMapClickListener(this);

        final LocationManager locationManager = (LocationManager) MainActivity.getMainContext()
                .getSystemService(Activity.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                currentLocation_ = location;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(provider, 20000L, 0.0f, locationListener);

        copterUpdator_ = new Timer();
        copterUpdator_.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                drawCopter();
            }
        }, 0, 250);

        return view;
    }

    @Override
    public void onMapClick(LatLng point) {
        final LatLng location = point;
        Handler mainHandler = new Handler(MainActivity.getMainContext().getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                final Marker gohere = googleMap_.addMarker(new MarkerOptions()
                        .position(location)
                        .title("Go Here")
                        .icon(BitmapDescriptorFactory.fromBitmap(
                                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))));
                googleMap_.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        if (marker.equals(gohere)) {
                            DroneActions.moveToPosition(DroneImp.INSTANCE, marker.getPosition());
                            marker.hideInfoWindow();
                        }
                    }
                });
                gohere.showInfoWindow();
            }
        });
    }

    private void drawCopter() {
        final GPSPosition gps = (GPSPosition) DroneImp.INSTANCE.getDroneAttribute("GPSPosition");
        final LatLng location = new LatLng(gps.getLatitude(), gps.getLongitude());

        Handler mainHandler = new Handler(MainActivity.getMainContext().getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (copterMarker_ != null)
                    copterMarker_.remove();

                copterMarker_ = googleMap_.addMarker(new MarkerOptions()
                        .position(location)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.copter_icon)));
            }
        });
    }
}