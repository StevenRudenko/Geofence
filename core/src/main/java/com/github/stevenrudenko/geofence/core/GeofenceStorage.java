package com.github.stevenrudenko.geofence.core;

import java.util.List;

import io.reactivex.Observable;

/** Storage to save geofence records. */
public interface GeofenceStorage {

    Observable<List<Geofence>> getGeofenceUpdates();

    List<Geofence> getGeofences();

    void add(Geofence geofence);

    void remove(Geofence geofence);

}
