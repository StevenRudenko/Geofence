package com.github.stevenrudenko.geofence.core;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/** Android implementation of {@link LocationProvider} */
public class AndroidLocationProvider implements LocationProvider, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /** Used to proxy location updates. */
    private final BehaviorSubject<Location> locationSubject = BehaviorSubject.create();

    /** Used to work with fused location service from Google. */
    private GoogleApiClient googleApiClient;

    public AndroidLocationProvider(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context.getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }

    @Override
    public Observable<Location> getLocationUpdates() {
        return locationSubject;
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        locationSubject.onNext(new Location(location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //TODO: handle failed cases
    }
}
