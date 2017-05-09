package com.github.stevenrudenko.geofence.core;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
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
     * Inbounds geofence observable.
     */
    private BehaviorSubject<ArrayList<Geofence>> inbouncGeofences = BehaviorSubject.create();
    /**
     * Geofence calculation subscription.
     */
    private Disposable updatesSubscription;

    /**
     * Sets work on scheduler.
     */
    private final Scheduler workOn;

    public GeofenceModule(LocationProvider locationProvider,
                          WifiInfoProvider wifiInfoProvider,
                          GeofenceStorage storage) {
        this(locationProvider, wifiInfoProvider, storage, Schedulers.computation());
    }

    /** For testing purposes. */
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
        if (updatesSubscription == null) {
            throw new IllegalStateException(GeofenceModule.class.getSimpleName() + " should be started first");
        }
        return inbouncGeofences;
    }

    public void start() {
        locationProvider.start();
        wifiInfoProvider.start();

        updatesSubscription = Observable.combineLatest(
                locationProvider.getLocationUpdates(),
                wifiInfoProvider.getWiFiInfoUpdates(),
                GeofenceCheck::new)
                .subscribeOn(workOn)
                .map(geofenceCheck -> {
                    final List<Geofence> geofences = storage.getGeofences();
                    final ArrayList<GeofenceResult> result = new ArrayList<>();
                    for (Geofence geofence : geofences) {
                        final boolean inbound = GeofenceUtils.isInsideGeofence(
                                geofence, geofenceCheck.location, geofenceCheck.wifiInfo.getSsid());
                        result.add(new GeofenceResult(geofence, inbound));
                    }
                    return result;
                })
                .doOnNext(results -> {
                    final ArrayList<Geofence> list = new ArrayList<>();
                    for (GeofenceResult result : results) {
                        if (result.inbound) {
                            list.add(result.geofence);
                        }
                    }
                    inbouncGeofences.onNext(list);
                })
                .subscribe();
    }

    public void stop() {
        if (updatesSubscription != null) {
            updatesSubscription.dispose();
            updatesSubscription = null;
        }

        locationProvider.stop();
        wifiInfoProvider.stop();
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
