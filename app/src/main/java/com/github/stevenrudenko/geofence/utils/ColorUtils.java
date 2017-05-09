package com.github.stevenrudenko.geofence.utils;

import android.content.Context;
import android.support.annotation.ColorRes;

/** Color utilities. */
public final class ColorUtils {

    public static int getColor(Context context, @ColorRes int res) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return context.getResources().getColor(res, context.getTheme());
        } else {
            //noinspection deprecation
            return context.getResources().getColor(res);
        }
    }

}
