package com.github.stevenrudenko.geofence.core;

import org.junit.Test;

import java.util.List;

import io.reactivex.observers.TestObserver;

import static com.github.stevenrudenko.geofence.core.GeofenceTestConstants.GEOFENCE_RADIUS;
import static com.github.stevenrudenko.geofence.core.GeofenceTestConstants.KYIV;
import static org.junit.Assert.assertEquals;

/** Test for {@link GeofenceStorage}. */
public class MemoryGeofenceStorageTest {
    private MemoryGeofenceStorage target = new MemoryGeofenceStorage();

    @Test
    public void getGeofenceUpdates_zero() throws Exception {
        final TestObserver<List<Geofence>> observer = new TestObserver<>();
        target.getGeofenceUpdates().subscribe(observer);
        observer.assertNoErrors();
        observer.assertValueCount(0);

        Geofence kyiv = new Geofence(KYIV, GEOFENCE_RADIUS, "Kyiv");
        target.add(kyiv);
        observer.assertValueCount(1);
        final List<Geofence> list = observer.values().get(0);
        assertEquals(list.size(), 1);
    }

    @Test
    public void getGeofenceUpdates_add() throws Exception {
        Geofence kyiv = new Geofence(KYIV, GEOFENCE_RADIUS, "Kyiv");

        final TestObserver<List<Geofence>> observer = new TestObserver<>();
        target.getGeofenceUpdates().subscribe(observer);
        observer.assertNoErrors();
        observer.assertValueCount(0);

        target.add(kyiv);
        observer.assertNoErrors();
        observer.assertValueCount(1);
        assertEquals(observer.values().get(0).size(), 1);
    }

    @Test
    public void getGeofenceUpdates_remove() throws Exception {
        Geofence kyiv = new Geofence(KYIV, GEOFENCE_RADIUS, "Kyiv");
        target.add(kyiv);

        final TestObserver<List<Geofence>> observer = new TestObserver<>();
        target.getGeofenceUpdates().subscribe(observer);

        target.remove(kyiv);
        observer.assertNoErrors();
        observer.assertValueCount(2);
        assertEquals(observer.values().get(1).size(), 0);
    }

    @Test
    public void add() throws Exception {
        assertEquals(target.getGeofences().size(), 0);
        target.add(new Geofence(KYIV, GEOFENCE_RADIUS, "Kyiv"));
        assertEquals(target.getGeofences().size(), 1);
    }

    @Test
    public void remove() throws Exception {
        assertEquals(target.getGeofences().size(), 0);
        Geofence kyiv = new Geofence(KYIV, GEOFENCE_RADIUS, "Kyiv");
        target.add(kyiv);
        assertEquals(target.getGeofences().size(), 1);
        target.remove(kyiv);
        assertEquals(target.getGeofences().size(), 0);
    }

}