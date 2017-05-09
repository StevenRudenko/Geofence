package com.github.stevenrudenko.geofence;

import com.github.stevenrudenko.geofence.core.Geofence;
import com.github.stevenrudenko.geofence.core.GeofenceStorage;

import java.util.ArrayList;
import java.util.List;

/** Memory implementation for {@link GeofenceStorage}. Used in tests. */
public class MemoryGeofenceStorage implements GeofenceStorage {
    /** Memory storage. */
    private final List<Geofence> geofences = new ArrayList<>();

    @Override
    public List<Geofence> getGeofences() {
        return new ArrayList<>(geofences);
    }

    @Override
    public void add(Geofence geofence) {
        geofences.add(geofence);
    }

    @Override
    public void remove(Geofence geofence) {
        geofences.remove(geofence);
    }
}
