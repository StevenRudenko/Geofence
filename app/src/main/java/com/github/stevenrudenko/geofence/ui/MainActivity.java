package com.github.stevenrudenko.geofence.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.github.stevenrudenko.geofence.R;
import com.github.stevenrudenko.geofence.core.AndroidLocationProvider;
import com.github.stevenrudenko.geofence.core.AndroidWifiInfoProvider;
import com.github.stevenrudenko.geofence.core.Geofence;
import com.github.stevenrudenko.geofence.core.GeofenceModule;
import com.github.stevenrudenko.geofence.core.GeofenceStorage;
import com.github.stevenrudenko.geofence.core.LocationProvider;
import com.github.stevenrudenko.geofence.core.SqliteGeofenceStorage;
import com.github.stevenrudenko.geofence.core.WifiInfoProvider;
import com.github.stevenrudenko.geofence.ui.dialog.AlertDialogFragment;
import com.github.stevenrudenko.geofence.utils.ColorUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Main activity.
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener,
        AddGeofenceDialogFragment.AddGeofenceDialogListener,
        AlertDialogFragment.OnAlertDialogListener {
    /**
     * Log tag.
     */
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Request to get needed permissions.
     */
    public static final int REQUEST_PERMISSIONS = 1;
    /**
     * Required permissions.
     */
    private final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    /** Add geofence dialog tag. */
    private static final String DIALOG_ADD_GEOFENCE_TAG = "dialog:add-geofence";
    /** Remove geofence dialog tag. */
    private static final String DIALOG_REMOVE_GEOFENCE_TAG = "dialog:remove-geofence";

    /** Default my position zoom level. */
    private static final int DEFAULT_MY_POSITION_ZOOM_LEVEL = 16;

    /** State to restore map point. */
    private static final String STATE_MAP_POSITION = "state:position";
    /** State to restore map zoom level. */
    private static final String STATE_MAP_ZOOM_LEVEL = "state:zoom";

    /** Root layout. */
    private View root;
    /** Shows count of geofences inbound. */
    @Nullable
    private Snackbar geofenceSnaclbar;
    /**
     * Map view.
     */
    private MapView mapView;
    /**
     * Used to work with map.
     */
    private GoogleMap googleMap;

    /**
     * Geofence module.
     */
    private GeofenceModule geofenceModule;
    /** Location provider. */
    private LocationProvider locationProvider;
    /**
     * Used to release subscriptions.
     */
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    /** Geofence storage. */
    private GeofenceStorage storage;
    /** Geofence marker map. */
    private Map<Marker, MarkerItem> geofenceMap = new HashMap<>();
    /** Geofence marker used to be removed. */
    private Marker toRemove = null;

    /** Map position to restore. */
    @Nullable
    private LatLng restorePosition;
    /** Zoom level to restore. */
    private float restoreZoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            restorePosition = savedInstanceState.getParcelable(STATE_MAP_POSITION);
            restoreZoom = savedInstanceState.getFloat(STATE_MAP_ZOOM_LEVEL);
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        root = findViewById(R.id.root_layout);

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        locationProvider = new AndroidLocationProvider(this);
        final WifiInfoProvider wifiInfoProvider = new AndroidWifiInfoProvider(this);
        storage = new SqliteGeofenceStorage(this);
        geofenceModule = new GeofenceModule(locationProvider, wifiInfoProvider, storage);

        findViewById(R.id.fab).setOnClickListener(view -> showMyPostions());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (googleMap != null) {
            outState.putParcelable(STATE_MAP_POSITION, googleMap.getCameraPosition().target);
            outState.putFloat(STATE_MAP_ZOOM_LEVEL, googleMap.getCameraPosition().zoom);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        // setup map view
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        // check location permissions
        if (!checkPermissions()) {
            return;
        }
        //noinspection MissingPermission
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        // add map action listeners
        googleMap.setOnMapLongClickListener(this);
        googleMap.setOnMarkerClickListener(this);
        // start geo-fence module
        start();
    }

    @Override
    public void onStart() {
        mapView.onStart();
        super.onStart();
        if (googleMap != null && checkPermissions()) {
            start();
        }
    }

    private void start() {
        geofenceModule.start();
        compositeDisposable.add(
                geofenceModule.getInboundGeofences().subscribe(geofences -> {
                    final int count = geofences.size();
                    if (count == 0) {
                        if (geofenceSnaclbar != null) {
                            geofenceSnaclbar.dismiss();
                        }
                    } else {
                        final String text = getResources().getQuantityString(
                                R.plurals.geofences_inbound, count, count);
                        geofenceSnaclbar = Snackbar.make(root, text, Snackbar.LENGTH_INDEFINITE);
                        geofenceSnaclbar.show();
                    }
                })
        );
        compositeDisposable.add(
                storage.getGeofenceUpdates()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::updateGeofenceMarkers)
        );
        if (restorePosition == null) {
            showMyPostions();
        } else {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    restorePosition, restoreZoom);
            googleMap.moveCamera(cameraUpdate);
        }
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

    /**
     * @return {@code true} if all needed permissions are granted.
     * Otherwise permission request will be executed.
     */
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
        AddGeofenceDialogFragment.newInstance(latLng.latitude, latLng.longitude)
                .show(getSupportFragmentManager(), DIALOG_ADD_GEOFENCE_TAG);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        toRemove = marker;
        resetMarker(marker, true);

        AlertDialogFragment.newInstance(
                null,
                getText(R.string.remove_geofence_message),
                getText(R.string.ok),
                getText(R.string.cancel),
                true)
                .show(getSupportFragmentManager(), DIALOG_REMOVE_GEOFENCE_TAG);
        return true;
    }

    private Disposable showMyPostions() {
        return locationProvider.getLocationUpdates()
                .firstElement()
                .subscribe(location -> {
                    LatLng myPosition = new LatLng(location.getLat(), location.getLng());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myPosition,
                            DEFAULT_MY_POSITION_ZOOM_LEVEL);
                    googleMap.animateCamera(cameraUpdate);
                });
    }

    @Override
    public void addGeofence(Geofence geofence) {
        storage.add(geofence);
    }

    @Override
    public void onDialogButtonClick(AlertDialogFragment f, int which) {
        if (toRemove == null) {
            return;
        }
        if (which == AlertDialogFragment.BUTTON_POSITIVE) {
            final MarkerItem item = geofenceMap.get(toRemove);
            final Geofence geofence = item.geofence;
            storage.remove(geofence);
        } else {
            resetMarker(toRemove, false);
        }
    }

    @Override
    public void onDialogCanceled(AlertDialogFragment f) {
        if (toRemove != null) {
            resetMarker(toRemove, false);
        }
    }

    private void updateGeofenceMarkers(List<Geofence> geofences) {
        googleMap.clear();
        geofenceMap.clear();

        final int strokeColor = ColorUtils.getColor(this, R.color.colorPrimary);
        final int fillColor = ColorUtils.getColor(this, R.color.colorPrimaryDim);
        for (Geofence geofence : geofences) {
            final LocationProvider.Location point = geofence.getPoint();
            final LatLng latLng = new LatLng(point.getLat(), point.getLng());
            CircleOptions circle = new CircleOptions()
                    .center(latLng)
                    .radius(geofence.getRadius())
                    .fillColor(fillColor)
                    .strokeWidth(2f)
                    .strokeColor(strokeColor);
            MarkerOptions marker = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_geofence_marker))
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .position(latLng);
            final Circle c = googleMap.addCircle(circle);
            final Marker m = googleMap.addMarker(marker);
            geofenceMap.put(m, new MarkerItem(geofence, c));
        }
    }

    private void resetMarker(Marker marker, boolean selected) {
        marker.setIcon(BitmapDescriptorFactory.fromResource(
                selected ? R.drawable.ic_geofence_marker_selected : R.drawable.ic_geofence_marker));
        final MarkerItem item = geofenceMap.get(toRemove);
        final Circle circle = item.circle;
        final int strokeColor = ColorUtils.getColor(this,
                selected ? R.color.colorAccent : R.color.colorPrimary);
        final int fillColor = ColorUtils.getColor(this,
                selected ? R.color.colorAccentDim : R.color.colorPrimaryDim);
        circle.setStrokeColor(strokeColor);
        circle.setFillColor(fillColor);
    }

    /** Used to link geofence and circle by marker. */
    private static class MarkerItem {
        final Geofence geofence;
        final Circle circle;

        private MarkerItem(Geofence geofence, Circle circle) {
            this.geofence = geofence;
            this.circle = circle;
        }
    }

}
