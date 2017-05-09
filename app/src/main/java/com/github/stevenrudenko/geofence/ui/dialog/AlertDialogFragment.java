package com.github.stevenrudenko.geofence.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/** Alert dialog fragment. */
public class AlertDialogFragment extends BaseDialogFragment<AlertDialogFragment.OnAlertDialogListener> {
    /**
     * The identifier for the positive button.
     */
    public static final int BUTTON_POSITIVE = DialogInterface.BUTTON_POSITIVE;
    /**
     * The identifier for the neutral button.
     */
    public static final int BUTTON_NEUTRAL = DialogInterface.BUTTON_NEUTRAL;
    /**
     * The identifier for the negative button.
     */
    public static final int BUTTON_NEGATIVE = DialogInterface.BUTTON_NEGATIVE;

    /** Key for title CharSequence argument. */
    public static final String ARG_TITLE = "arg:title";
    /** Key for message CharSequence argument. */
    public static final String ARG_MESSAGE = "arg:message";
    /** Key for positive button CharSequence argument. */
    public static final String ARG_POSITIVE_BUTTON = "arg:positive";
    /** Key for negative button CharSequence argument. */
    public static final String ARG_NEGATIVE_BUTTON = "arg:negative";
    /** Key for neutral button CharSequence argument. */
    public static final String ARG_NEUTRAL_BUTTON = "arg:neutral";
    /** Flag whether dialog is cancelable. */
    public static final String ARG_IS_CANCELABLE = "arg:is_cancelable";

    public static AlertDialogFragment newInstance(CharSequence title, CharSequence message,
            CharSequence positive, CharSequence negative, boolean cancelable) {
        final Bundle args = new Bundle();
        args.putCharSequence(ARG_TITLE, title);
        args.putCharSequence(ARG_MESSAGE, message);
        args.putCharSequence(ARG_POSITIVE_BUTTON, positive);
        args.putCharSequence(ARG_NEGATIVE_BUTTON, negative);
        args.putBoolean(ARG_IS_CANCELABLE, cancelable);

        final AlertDialogFragment f = new AlertDialogFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    protected Class<OnAlertDialogListener> getListenerClass() {
        return OnAlertDialogListener.class;
    }

    @Override
    protected boolean isListenerOptional() {
        return true;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final CharSequence title = args.getCharSequence(ARG_TITLE);
        final CharSequence message = args.getCharSequence(ARG_MESSAGE);
        final CharSequence positive = args.getCharSequence(ARG_POSITIVE_BUTTON);
        final CharSequence negative = args.getCharSequence(ARG_NEGATIVE_BUTTON);
        final boolean cancelable = args.getBoolean(ARG_IS_CANCELABLE);

        setCancelable(cancelable);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setCancelable(cancelable);
        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getListener().onDialogButtonClick(AlertDialogFragment.this, which);
            }
        };
        if (positive != null) {
            dialog.setPositiveButton(positive, dialogClickListener);
        }
        if (negative != null) {
            dialog.setNegativeButton(negative, dialogClickListener);
        }
        if (cancelable) {
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(final DialogInterface dialog) {
                    getListener().onDialogCanceled(AlertDialogFragment.this);
                }
            });
        }
        return dialog.create();
    }

    /** Alert dialog listener. */
    public interface OnAlertDialogListener {

        /**
         * Notifies alert button click.
         * @param f source alert dialog.
         * @param which button ID.
         */
        void onDialogButtonClick(AlertDialogFragment f, int which);

        /**
         * Notifies that dialog has been canceled.
         * @param f source alert dialog.
         */
        void onDialogCanceled(AlertDialogFragment f);

    }

}
