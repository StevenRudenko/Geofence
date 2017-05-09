package com.github.stevenrudenko.geofence.core;

import org.junit.Test;

import static org.junit.Assert.*;
import static com.github.stevenrudenko.geofence.core.GeofenceTestConstants.*;

/** Tests for {@link GeofenceUtils}. */
public class GeofenceUtilsTest {

    /** Cities matched to distances from Kyiv to . */
    private static final LocationProvider.Location[] CITIES = {
            KYIV,
            LVIV,
            KHARKIV,
            ODESSA,
            SIMFEROPOL,
    };
    /** Distances between Kyiv and other cities. */
    private static final float[] DISTANCES = {
            0f,
            470247.78f,
            410211.22f,
            441125.0f,
            667510.6f,
    };

    /**
     * Test geofence value for point outside geofence and wifi disconnected.
     */
    @Test
    public void isInsideGeofence_outPoint_wifiOff() {
        Geofence geofence = new Geofence(KYIV, GEOFENCE_RADIUS, "Test");
        final boolean result = GeofenceUtils.isInsideGeofence(geofence, LVIV, null);
        assertFalse(result);
    }

    /**
     * Test geofence value for point outside geofence and wifi connected to difference hotspot.
     */
    @Test
    public void isInsideGeofence_outPoint_wifiOn_noMatch() {
        Geofence geofence = new Geofence(KYIV, GEOFENCE_RADIUS, "Match");
        final boolean result = GeofenceUtils.isInsideGeofence(geofence, LVIV, "NoMatch");
        assertFalse(result);
    }

    /**
     * Test geofence value for point outside geofence and wifi connected to geofence hotspot.
     */
    @Test
    public void isInsideGeofence_outPoint_wifiOn_Match() {
        Geofence geofence = new Geofence(KYIV, GEOFENCE_RADIUS, "Match");
        final boolean result = GeofenceUtils.isInsideGeofence(geofence, LVIV, "Match");
        assertTrue(result);
    }

    /**
     * Test geofence value for point outside geofence and wifi disconnected.
     */
    @Test
    public void isInsideGeofence_inPoint_wifiOff() {
        Geofence geofence = new Geofence(KYIV, GEOFENCE_RADIUS, "Match");
        final boolean result = GeofenceUtils.isInsideGeofence(geofence, KYIV_AIRPORT, null);
        assertTrue(result);
    }

    /**
     * Test geofence value for point outside geofence and wifi connected to difference hotspot.
     */
    @Test
    public void isInsideGeofence_inPoint_wifiOn_noMatch() {
        Geofence geofence = new Geofence(KYIV, GEOFENCE_RADIUS, "Match");
        final boolean result = GeofenceUtils.isInsideGeofence(geofence, KYIV_AIRPORT, "NoMatch");
        assertTrue(result);
    }

    /**
     * Test geofence value for point outside geofence and wifi connected to geofence hotspot.
     */
    @Test
    public void isInsideGeofence_inPoint_wifiOn_Match() {
        Geofence geofence = new Geofence(KYIV, GEOFENCE_RADIUS, "Match");
        final boolean result = GeofenceUtils.isInsideGeofence(geofence, KYIV_AIRPORT, "Match");
        assertTrue(result);
    }

    /**
     * Test geofence value for point outside geofence and wifi connected to geofence hotspot
     * based pattern.
     */
    @Test
    public void isInsideGeofence_wifiOn_Match_Pattern() {
        Geofence geofence = new Geofence(KYIV, GEOFENCE_RADIUS, "Match_.\\d");
        final boolean result = GeofenceUtils.isInsideGeofence(geofence, LVIV, "Match_01");
        assertTrue(result);
    }

    /** Tests distances between Kyiv and some other cities of Ukraine. */
    @Test
    public void distance() {
        for (int i = 0; i < CITIES.length; ++i) {
            final LocationProvider.Location city = CITIES[i];
            final float distance = GeofenceUtils.distance(KYIV, city);
            assertEquals(DISTANCES[i], distance, 1e-2);
        }
    }

}