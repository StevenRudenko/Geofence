package com.github.stevenrudenko.geofence.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.support.v7.app.AppCompatDialogFragment;

/**
 * Base dialog fragment.
 * @param <Listener> processorType of listener
 */
public abstract class BaseDialogFragment<Listener> extends AppCompatDialogFragment {

    /** Dialog listener instance. */
    private Listener listener;

    /**
     * Provides listener class to cast listener.
     * @return listener class.
     */
    protected Class<Listener> getListenerClass() {
        return null;
    }

    public Listener getListener() {
        return listener;
    }

    /**
     * Indicates whether listener is optional.
     * @return true if listener is optional.
     */
    protected boolean isListenerOptional() {
        return getListenerClass() == null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        final Class<Listener> clazz = getListenerClass();
        if (clazz == null) {
            listener = null;
        } else if (getParentFragment() != null
                && clazz.isAssignableFrom(getParentFragment().getClass())) {
            listener = (Listener) getParentFragment();
        } else if (clazz.isAssignableFrom(activity.getClass())) {
            listener = (Listener) activity;
        } else {
            listener = null;
        }
        if (listener == null && !isListenerOptional()) {
            throw new ClassCastException(activity.getClass().getName() + " must implement listener");
        }
    }

    public boolean isShowing() {
        final Dialog dialog = getDialog();
        return dialog != null && dialog.isShowing();
    }

    // Hack for android issue 17423 in the compatibility library
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
