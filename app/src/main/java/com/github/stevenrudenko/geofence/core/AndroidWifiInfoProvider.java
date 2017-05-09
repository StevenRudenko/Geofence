package com.github.stevenrudenko.geofence.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/** Android implementation of {@link WifiInfoProvider}. */
public class AndroidWifiInfoProvider implements WifiInfoProvider {
    /** Used to register Wifi state reciever. */
    private final Context context;
    /** Used to lantern for system events about Wifi state changes. */
    private final WifiStateBroadcastReceiver receiver = new WifiStateBroadcastReceiver();

    /** Used to proxy WIfi state updates. */
    private final BehaviorSubject<WifiInfo> wifiInfoSubject = BehaviorSubject.create();

    /** Indicates whether provider is started already. */
    private boolean isStarted;

    public AndroidWifiInfoProvider(Context context) {
        this.context = context;
    }

    @Override
    public void start() {
        if (isStarted) {
            return;
        }
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(receiver, filter);
        isStarted = true;
    }

    @Override
    public void stop() {
        if (isStarted) {
            context.unregisterReceiver(receiver);
        }
        isStarted = false;
    }

    @Override
    public Observable<WifiInfo> getWiFiInfoUpdates() {
        return wifiInfoSubject;
    }

    private WifiInfo readWifiInfo() {
        final ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = connManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(
                    Context.WIFI_SERVICE);
            android.net.wifi.WifiInfo info = wifiManager.getConnectionInfo();
            String ssid  = info.getSSID().replaceAll("\"", "");
            return new WifiInfo(true, ssid);
        } else {
            return new WifiInfo(false);
        }
    }

    /** Used to listen for system events about Wifi state changes. */
    private class WifiStateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final WifiInfo lastWifiInfo = wifiInfoSubject.getValue();
            final WifiInfo newWifiInfo = readWifiInfo();
            if (lastWifiInfo == null || !lastWifiInfo.equals(newWifiInfo)) {
                wifiInfoSubject.onNext(newWifiInfo);
            }
        }
    }

}
