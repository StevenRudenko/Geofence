package com.github.stevenrudenko.geofence.core;

import io.reactivex.Observable;

/** Provides information about WiFi connection. */
public interface WifiInfoProvider {
    void start();

    void stop();

    /**
     * @return provides access to {@link WifiInfo} updates.
     */
    Observable<WifiInfo> getWiFiInfoUpdates();

    /** WiFi information. */
    class WifiInfo {
        /** Indicates whether WiFi connection alive. */
        private boolean connected;
        /** WiFi SSID connected to. */
        private String ssid;

        public WifiInfo(boolean connected) {
            this(connected, null);
        }

        public WifiInfo(boolean connected, String ssid) {
            this.connected = connected;
            this.ssid = ssid;
        }

        public boolean isConnected() {
            return connected;
        }

        public String getSsid() {
            return ssid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WifiInfo wifiInfo = (WifiInfo) o;

            if (connected != wifiInfo.connected) return false;
            return ssid != null ? ssid.equals(wifiInfo.ssid) : wifiInfo.ssid == null;
        }

        @Override
        public int hashCode() {
            int result = (connected ? 1 : 0);
            result = 31 * result + (ssid != null ? ssid.hashCode() : 0);
            return result;
        }
    }
}
