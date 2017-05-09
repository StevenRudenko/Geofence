package com.github.stevenrudenko.geofence.core;

import io.reactivex.Observable;

/** Provides location updates.  */
public interface LocationProvider {

    void start();

    void stop();

    /**
     * @return location updates observable.
     */
    Observable<Location> getLocationUpdates();

    /** Location instance. */
    class Location {
        /** Decimal latitude/longitude location values. */
        private final double lat, lng;

        public Location(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }
    }
}
