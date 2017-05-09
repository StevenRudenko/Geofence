package com.github.stevenrudenko.geofence.core;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Geofence module. Used to notify whether device is inside any geofence.
 */
public class GeofenceModule {
    /**
     * Used to get location updates from.
     */
    private final LocationProvider locationProvider;
    /**
     * Used to get Wifi state updates from.
     */
    private final WifiInfoProvider wifiInfoProvider;
    /**
     * Available geofences collection.
     */
    private final GeofenceStorage storage;
    /**
     * Geofence calculation subscription.
     */
    private final CompositeDisposable combinedSubscription = new CompositeDisposable();
    /**
     * Sets work on scheduler.
     */
    private final Scheduler workOn;
    /**
     * Inbounds geofence observable.
     */
    private final BehaviorSubject<ArrayList<Geofence>> inbouncGeofences = BehaviorSubject.create();
    /**
     * Check geofences data queue.
     */
    private final BehaviorSubject<GeofenceCheck> checkQueue = BehaviorSubject.create();

    public GeofenceModule(LocationProvider locationProvider,
                          WifiInfoProvider wifiInfoProvider,
                          GeofenceStorage storage) {
        this(locationProvider, wifiInfoProvider, storage, Schedulers.computation());
    }

    /**
     * For testing purposes.
     */
    protected GeofenceModule(LocationProvider locationProvider,
                             WifiInfoProvider wifiInfoProvider,
                             GeofenceStorage storage,
                             Scheduler workOn) {
        this.locationProvider = locationProvider;
        this.wifiInfoProvider = wifiInfoProvider;
        this.storage = storage;
        this.workOn = workOn;
    }

    public Observable<ArrayList<Geofence>> getInboundGeofences() {
        if (combinedSubscription.size() == 0) {
            throw new IllegalStateException(GeofenceModule.class.getSimpleName() + " should be started first");
        }
        return inbouncGeofences;
    }

    public void start() {
        locationProvider.start();
        wifiInfoProvider.start();

        Observable.combineLatest(
                locationProvider.getLocationUpdates(),
                wifiInfoProvider.getWiFiInfoUpdates(),
                GeofenceCheck::new)
                .subscribeOn(workOn)
                .subscribe(checkQueue);

        combinedSubscription.add(checkQueue.map(this::check).subscribe());
        combinedSubscription.add(
                storage.getGeofenceUpdates().subscribe(geofences -> check(checkQueue.getValue()))
        );
    }

    public void stop() {
        if (combinedSubscription != null) {
            combinedSubscription.clear();
        }
        locationProvider.stop();
        wifiInfoProvider.stop();
    }

    private ArrayList<Geofence> check(GeofenceCheck check) {
        final List<Geofence> geofences = storage.getGeofences();
        final ArrayList<Geofence> result = new ArrayList<>();
        for (Geofence geofence : geofences) {
            final boolean inbound = GeofenceUtils.isInsideGeofence(
                    geofence, check.location, check.wifiInfo.getSsid());
            if (inbound) {
                result.add(geofence);
            }
        }
        inbouncGeofences.onNext(result);
        return result;
    }

    /**
     * Model used to check geofences.
     */
    private static class GeofenceCheck {
        /**
         * Last known location.
         */
        final LocationProvider.Location location;
        /**
         * Last known WiFi state.
         */
        final WifiInfoProvider.WifiInfo wifiInfo;

        private GeofenceCheck(LocationProvider.Location location,
                              WifiInfoProvider.WifiInfo wifiInfo) {
            this.location = location;
            this.wifiInfo = wifiInfo;
        }
    }

    /**
     * Model used for geofences result.
     */
    private static class GeofenceResult {
        /**
         * Geofence.
         */
        final Geofence geofence;
        /**
         * Indicates whether device is inbounds of {@link #geofence}.
         */
        final boolean inbound;

        private GeofenceResult(Geofence geofence, boolean inbound) {
            this.geofence = geofence;
            this.inbound = inbound;
        }
    }

}
