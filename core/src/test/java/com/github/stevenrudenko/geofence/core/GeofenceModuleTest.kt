package com.github.stevenrudenko.geofence.core

import com.github.stevenrudenko.geofence.core.GeofenceTestConstants.*
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.ArrayList

/** Tests for [GeofenceModule].  */
class GeofenceModuleTest {
    private val kyivGeofence = Geofence(KYIV, GEOFENCE_RADIUS, "Kyiv")
    private val vyshneveGeofence = Geofence(VYSHNEVE, GEOFENCE_RADIUS, "Vyshneve")
    private val kbpGeofence = Geofence(KBP_AIRPORT, GEOFENCE_RADIUS, "KBP")

    private var locations = PublishSubject.create<LocationProvider.Location>()
    private var locationProviderMock = mock<LocationProvider> {
        on { locationUpdates } doReturn locations
    }
    private var wifiInfos = PublishSubject.create<WifiInfoProvider.WifiInfo>()
    private var wifiInfoProviderMock = mock<WifiInfoProvider> {
        on { wiFiInfoUpdates } doReturn wifiInfos
    }
    private val geofenceStorage = MemoryGeofenceStorage()

    private lateinit var target: GeofenceModule

    @Before
    fun setUp() {
        target = GeofenceModule(locationProviderMock, wifiInfoProviderMock, geofenceStorage,
                Schedulers.trampoline())
        // add some geofences
        geofenceStorage.add(kyivGeofence)
        geofenceStorage.add(vyshneveGeofence)
        geofenceStorage.add(kbpGeofence)
    }

    /**
     * [GeofenceModule.inboundGeofences] should throw exception is module has
     * not be started before subscribing for updates.
     */
    @Test(expected = IllegalStateException::class)
    fun getInboundGeofences_exception() {
        target.inboundGeofences
                .subscribeOn(Schedulers.trampoline())
                .subscribe()
    }

    @Test
    fun getInboundGeofences_outBounds() {
        val observer = TestObserver<ArrayList<Geofence>>()
        target.start()
        target.inboundGeofences
                .observeOn(Schedulers.trampoline())
                .subscribe(observer)
        // send init locations
        locations.onNext(LVIV)
        wifiInfos.onNext(WifiInfoProvider.WifiInfo(false))

        observer.assertNoErrors()
        observer.assertValueCount(1)
        val list = observer.values()[0]
        assertEquals(list.size, 0)
    }

    @Test
    fun getInboundGeofences_inBounds_location_noWifi() {
        val observer = TestObserver<ArrayList<Geofence>>()
        target.start()
        target.inboundGeofences
                .observeOn(Schedulers.trampoline())
                .subscribe(observer)
        // send init locations
        locations.onNext(KBP_AIRPORT)
        wifiInfos.onNext(WifiInfoProvider.WifiInfo(false))

        observer.assertNoErrors()
        observer.assertValueCount(1)
        val list = observer.values()[0]
        assertEquals(list.size, 1)
        assertEquals(list[0], kbpGeofence)
    }

    @Test
    fun getInboundGeofences_inBounds_noLocation_wifi() {
        val observer = TestObserver<ArrayList<Geofence>>()
        target.start()
        target.inboundGeofences
                .observeOn(Schedulers.trampoline())
                .subscribe(observer)
        // send init locations
        locations.onNext(LVIV)
        wifiInfos.onNext(WifiInfoProvider.WifiInfo(true, "Kyiv"))

        observer.assertNoErrors()
        observer.assertValueCount(1)
        val list = observer.values()[0]
        assertEquals(list.size, 1)
        assertEquals(list[0], kyivGeofence)
    }

    @Test
    fun getInboundGeofences_inBounds_multiple_geofences() {
        val observer = TestObserver<ArrayList<Geofence>>()
        target.start()
        target.inboundGeofences
                .observeOn(Schedulers.trampoline())
                .subscribe(observer)
        // send init locations
        locations.onNext(KYIV_AIRPORT)
        wifiInfos.onNext(WifiInfoProvider.WifiInfo(true, "Kyiv"))

        observer.assertNoErrors()
        observer.assertValueCount(1)
        val list = observer.values()[0]
        assertEquals(list.size, 2)
        assertTrue(list.contains(kyivGeofence))
        assertTrue(list.contains(vyshneveGeofence))
    }

    @Test
    fun getInboundGeofences_path_to_geofence() {
        val observer = TestObserver<ArrayList<Geofence>>()
        target.start()
        target.inboundGeofences
                .observeOn(Schedulers.trampoline())
                .subscribe(observer)
        // send init locations
        wifiInfos.onNext(WifiInfoProvider.WifiInfo(false))
        locations.onNext(KHARKIV)
        locations.onNext(KBP_AIRPORT)
        locations.onNext(KYIV_AIRPORT)

        observer.assertNoErrors()

        observer.assertValueCount(3)
        val list = observer.values()[2]
        assertEquals(list.size, 2)
        assertTrue(list.contains(kyivGeofence))
        assertTrue(list.contains(vyshneveGeofence))
    }

    @Test
    fun start() {
        target.start()
        verify(locationProviderMock).start()
        verify(wifiInfoProviderMock).start()
    }

    @Test
    fun stop() {
        target.stop()
        verify(locationProviderMock).stop()
        verify(wifiInfoProviderMock).stop()
    }

}