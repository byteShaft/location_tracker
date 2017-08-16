package com.byteshaft.locationupdate;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by s9iper1 on 5/21/17.
 */

public class AppGlobals extends Application {

    public static int LOCATION_ENABLE = 12;
    private static Context sContext;
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_ENTERPRISE_ID = "enterprise_id";
    public static final String KEY_TRACKING = "tracking";
    public static boolean isInternetPresent = false;
    public static boolean checkingInternet = false;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    public static Context getContext() {
        return sContext;
    }

    public static void saveDeviceId(String deviceId) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().putString(KEY_DEVICE_ID, deviceId).apply();
    }

    public static String getDeviceId() {
        SharedPreferences sharedPreferences = getPreferenceManager();
        return sharedPreferences.getString(KEY_DEVICE_ID, "");
    }

    public static void saveEnterpriseId(String enterpriseId) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().putString(KEY_ENTERPRISE_ID, enterpriseId).apply();
    }

    public static String getEnterPriseId() {
        SharedPreferences sharedPreferences = getPreferenceManager();
        return sharedPreferences.getString(KEY_ENTERPRISE_ID, "");
    }

    public static void saveTackingState(boolean state) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().putBoolean(KEY_TRACKING, state).apply();
    }

    public static boolean isTracking() {
        SharedPreferences sharedPreferences = getPreferenceManager();
        return sharedPreferences.getBoolean(KEY_TRACKING, false);
    }

    public static SharedPreferences getPreferenceManager() {
        return getContext().getSharedPreferences("shared_prefs", MODE_PRIVATE);
    }
}
