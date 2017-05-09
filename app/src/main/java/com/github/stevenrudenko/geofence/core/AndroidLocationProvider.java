package com.github.stevenrudenko.geofence.core;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/** Android implementation of {@link LocationProvider} */
public class AndroidLocationProvider implements LocationProvider, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    /** Log tag. */
    private static final String TAG = AndroidLocationProvider.class.getSimpleName();

    /** Used to proxy location updates. */
    private final BehaviorSubject<Location> locationSubject = BehaviorSubject.create();

    /** Used to request location updates. */
    private final Context context;
    /** Used to work with fused location service from Google. */
    private GoogleApiClient googleApiClient;

    public AndroidLocationProvider(Context context) {
        this.context = context.getApplicationContext();
        googleApiClient = new GoogleApiClient.Builder(this.context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public boolean isAvailable() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void start() {
        if (!isAvailable()) {
            throw new IllegalStateException("Can't get location without permissions");
        }

        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        } else {
            requestLocationUpdates();
        }
    }

    @SuppressWarnings("MissingPermission")
    private void requestLocationUpdates() {
        final LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(60000L);
        onLocationChanged(LocationServices.FusedLocationApi.getLastLocation(googleApiClient));
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, request, this);
    }

    @Override
    public void stop() {
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    @Override
    public Observable<Location> getLocationUpdates() {
        return locationSubject;
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        Log.d(TAG, "onLocationChanged: " + location);
        if (location != null) {
            locationSubject.onNext(new Location(location.getLatitude(), location.getLongitude()));
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: " + bundle);
        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //TODO: handle failed cases
        Log.w(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    }
}
