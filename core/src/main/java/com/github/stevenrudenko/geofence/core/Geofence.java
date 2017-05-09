package com.github.stevenrudenko.geofence.core;

import java.util.regex.Pattern;

/**
 * Geofence model.
 */
public class Geofence {
    /**
     * Latitude/Longitude of geofence center.
     */
    private final LocationProvider.Location point;
    /**
     * Radius of geofence area.
     */
    private final int radius;
    /**
     * Geofence SSID pattern.
     */
    private final Pattern ssidPattern;

    public Geofence(LocationProvider.Location point, int radius, String ssidPattern) {
        this.point = point;
        this.radius = radius;
        this.ssidPattern = Pattern.compile(ssidPattern);
    }

    public LocationProvider.Location getPoint() {
        return point;
    }

    public int getRadius() {
        return radius;
    }

    public Pattern getSsidPattern() {
        return ssidPattern;
    }
}