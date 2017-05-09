package com.github.stevenrudenko.geofence.core;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Geofence model.
 */
public class Geofence {
    /**
     * UUID. Used to store it at storage.
     */
    private final String uuid;
    /**
     * Latitude/Longitude of geofence center.
     */
    private final LocationProvider.Location point;
    /**
     * Radius of geofence area.
     */
    private final int radius;
    /**
     * Geofence SSID.
     */
    private final String ssid;
    /**
     * Gefence SSID pattern.
     */
    private final Pattern pattern;

    public Geofence(LocationProvider.Location point, int radius, String ssid) {
        this(UUID.randomUUID().toString(), point, radius, ssid);
    }

    public Geofence(String uuid, LocationProvider.Location point, int radius, String ssid) {
        this.uuid = uuid;
        this.point = point;
        this.radius = radius;
        this.ssid = ssid;
        this.pattern = Pattern.compile(ssid);
    }

    public String getUuid() {
        return uuid;
    }

    public LocationProvider.Location getPoint() {
        return point;
    }

    public int getRadius() {
        return radius;
    }

    public String getSsid() {
        return ssid;
    }

    public Pattern getSsidPattern() {
        return pattern;
    }
}