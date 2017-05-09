package com.github.stevenrudenko.geofence.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.github.stevenrudenko.geofence.R;
import com.github.stevenrudenko.geofence.core.Geofence;
import com.github.stevenrudenko.geofence.core.LocationProvider;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/** Add geofence dialog fragment. */
public class AddGeofenceDialogFragment extends DialogFragment {
    /** Radius pattern. */
    private static final Pattern RADIUS_PATTERN = Pattern.compile("\\d+");

    /** Double. Geofence location latitude. */
    public static final String ARG_LATITUDE = "arg:latitude";
    /** Double. Geofence location longitude. */
    public static final String ARG_LONGITUDE = "arg:longitude";

    /** Radius input. */
    private EditText radiusInput;
    /** SSID input. */
    private EditText ssidInput;
    /** Used to release input validation. */
    private Disposable inputValidationDisposable;

    /** Add geofence listener. */
    private AddGeofenceDialogListener listener;

    public static AddGeofenceDialogFragment newInstance(double lat, double lng) {
        final Bundle args = new Bundle(2);
        args.putDouble(ARG_LATITUDE, lat);
        args.putDouble(ARG_LONGITUDE, lng);

        final AddGeofenceDialogFragment f = new AddGeofenceDialogFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() != null
                && getParentFragment() instanceof AddGeofenceDialogListener) {
            listener = (AddGeofenceDialogListener) getParentFragment();
        } else if (getActivity() instanceof AddGeofenceDialogListener) {
            listener = (AddGeofenceDialogListener) getActivity();
        } else {
            throw new IllegalStateException("Dialog parent should inplement "
                    + AddGeofenceDialogListener.class.getSimpleName());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();
        final View v = LayoutInflater.from(context).inflate(R.layout.dialog_add_geofence, null, false);
        radiusInput = (EditText) v.findViewById(R.id.add_geofence_radius);
        ssidInput = (EditText) v.findViewById(R.id.add_geofence_ssid);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.add_geofence_title)
                .setView(v)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    final Bundle args = getArguments();
                    final double lat = args.getDouble(ARG_LATITUDE);
                    final double lng = args.getDouble(ARG_LONGITUDE);

                    final LocationProvider.Location location =
                            new LocationProvider.Location(lat, lng);
                    final int radius = Integer.parseInt(radiusInput.getText().toString());
                    final String ssid = ssidInput.getText().toString();
                    listener.addGeofence(new Geofence(location, radius, ssid));
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        inputValidationDisposable = Observable.combineLatest(
                RxTextView.textChanges(radiusInput), RxTextView.textChanges(ssidInput),
                (radius, ssid) -> RADIUS_PATTERN.matcher(radius).matches() && !TextUtils.isEmpty(ssid))
                .subscribe(correct -> {
                    final AlertDialog dialog = (AlertDialog) getDialog();
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(correct);
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (inputValidationDisposable != null) {
            inputValidationDisposable.dispose();
            inputValidationDisposable = null;
        }
    }

    /** Add geofence dialog listener. */
    public interface AddGeofenceDialogListener {

        void addGeofence(Geofence geofence);
    }

}
