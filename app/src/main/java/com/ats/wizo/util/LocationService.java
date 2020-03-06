package com.ats.wizo.util;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;


/**
 * Created by eis-05 on 20/1/16.
 */
public class LocationService extends Service implements LocationListener {

    private LocationManager locationManager;

    private Location location;
    private Context context;


    public LocationService(Context context) {
        this.context = context;
        locationManager = (LocationManager) context
                .getSystemService(LOCATION_SERVICE);
    }



    public Location getLocation(String provider) {
        if (locationManager.isProviderEnabled(provider)) {

            int result = context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (result == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(provider,
                        2000, 10, this);
            }
            if (result == PackageManager.PERMISSION_GRANTED) {
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(provider);
                    return location;
                }
            }
        }
        return null;
    }

//




    @Override
    public void onProviderEnabled(String provider) {

        /******** Called when User on Gps  *********/

        Toast.makeText(getApplicationContext(), "Gps turned on ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }












}