package com.github.stevenrudenko.geofence.core;

import java.util.List;

/** Storage to save geofence records. */
public interface GeofenceStorage {

    List<Geofence> getGeofences();

    void add(Geofence geofence);

    void remove(Geofence geofence);

}
