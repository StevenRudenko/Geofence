package com.github.stevenrudenko.geofence.core;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/** Memory implementation for {@link GeofenceStorage}. Used in tests. */
public class MemoryGeofenceStorage implements GeofenceStorage {
    /** Memory storage. */
    private final List<Geofence> geofences = new ArrayList<>();

    /** Used to proxy geo state updates. */
    private final BehaviorSubject<List<Geofence>> geofencesSubject = BehaviorSubject.create();

    @Override
    public Observable<List<Geofence>> getGeofenceUpdates() {
        return geofencesSubject;
    }

    @Override
    public List<Geofence> getGeofences() {
        return new ArrayList<>(geofences);
    }

    @Override
    public void add(Geofence geofence) {
        geofences.add(geofence);
        geofencesSubject.onNext(geofences);
    }

    @Override
    public void remove(Geofence geofence) {
        geofences.remove(geofence);
        geofencesSubject.onNext(geofences);
    }
}
