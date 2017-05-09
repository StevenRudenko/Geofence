package com.github.stevenrudenko.geofence.core;

/** Test constants. */
public final class GeofenceTestConstants {
    /** Geofence radius in meters. */
    public static final int GEOFENCE_RADIUS = 10000;

    public static final LocationProvider.Location KYIV = new LocationProvider.Location(50.45, 30.523333);
    public static final LocationProvider.Location LVIV = new LocationProvider.Location(49.83, 24.014167);
    public static final LocationProvider.Location KHARKIV = new LocationProvider.Location(50.004444, 36.231389);
    public static final LocationProvider.Location ODESSA = new LocationProvider.Location(46.485722, 30.743444);
    public static final LocationProvider.Location SIMFEROPOL = new LocationProvider.Location(44.951944, 34.102222);

    public static final LocationProvider.Location KYIV_AIRPORT = new LocationProvider.Location(50.411164, 30.444748);
    public static final LocationProvider.Location KBP_AIRPORT = new LocationProvider.Location(50.337936, 30.896075);
    public static final LocationProvider.Location VYSHNEVE = new LocationProvider.Location(50.384291, 30.369959);

    private GeofenceTestConstants() {
        // hide
    }
}
