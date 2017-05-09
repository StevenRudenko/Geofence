package com.github.stevenrudenko.geofence;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.github.stevenrudenko.geofence.core.AndroidLocationProvider;
import com.github.stevenrudenko.geofence.core.AndroidWifiInfoProvider;
import com.github.stevenrudenko.geofence.core.GeofenceModule;
import com.github.stevenrudenko.geofence.core.LocationProvider;
import com.github.stevenrudenko.geofence.core.WifiInfoProvider;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Main acitivity.
 */
public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {
    /** Log tag. */
    private static final String TAG = MainActivity.class.getSimpleName();

    /** Request to get needed permissions. */
    public static final int REQUEST_PERMISSIONS = 1;
    /** Required permissions. */
    private final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

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
        // setup map view
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        if (!checkPermissions()) {
            return;
        }
        //noinspection MissingPermission
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMapLongClickListener(this);
        googleMap.setOnMarkerClickListener(this);
        // start geo-fence module
        start();
    }

    @Override
    public void onStart() {
        mapView.onStart();
        super.onStart();
    }

    private void start() {
        geofenceModule.start();
        compositeDisposable.add(
                geofenceModule.getInboundGeofences().subscribe(geofences -> {
                    Log.i(TAG, "Geofences: " + geofences.size());
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
        geofenceModule.stop();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_PERMISSIONS) {
            return;
        }
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                checkPermissions();
                return;
            } else {
                start();
            }
        }
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : PERMISSIONS) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(
                    new String[listPermissionsNeeded.size()]), REQUEST_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
