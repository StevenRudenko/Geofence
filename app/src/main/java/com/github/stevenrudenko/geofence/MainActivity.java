package com.github.stevenrudenko.geofence;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.github.stevenrudenko.geofence.core.AndroidLocationProvider;
import com.github.stevenrudenko.geofence.core.AndroidWifiInfoProvider;
import com.github.stevenrudenko.geofence.core.GeofenceModule;
import com.github.stevenrudenko.geofence.core.LocationProvider;
import com.github.stevenrudenko.geofence.core.WifiInfoProvider;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

/**
 * Main acitivity.
 */
public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    /**
     * Map view.
     */
    private MapView mapView;

    /**
     * Used to work with map.
     */
    private GoogleMap googleMap;

    /**
     * Location provider.
     */
    private LocationProvider locationProvider;

    /** Geofence module. */
    private GeofenceModule geofenceModule;

    /**
     * Used to release subscriptions.
     */
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        locationProvider = new AndroidLocationProvider(this);
        final WifiInfoProvider wifiInfoProvider = new AndroidWifiInfoProvider(this);
        geofenceModule = new GeofenceModule(locationProvider, wifiInfoProvider, new MemoryGeofenceStorage());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;


        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        this.googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onStart() {
        mapView.onStart();
        super.onStart();
        compositeDisposable.add(
                locationProvider.getLocationUpdates().subscribe(location -> {
                    final LatLng userPosition = new LatLng(location.getLat(), location.getLng());
                    final MarkerOptions markerOptions = new MarkerOptions()
                            .position(userPosition);
                    this.googleMap.addMarker(markerOptions);
                    this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(userPosition));
                })
        );

    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        compositeDisposable.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}
