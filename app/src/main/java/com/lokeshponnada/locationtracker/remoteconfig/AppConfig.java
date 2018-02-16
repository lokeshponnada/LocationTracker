package com.lokeshponnada.locationtracker.remoteconfig;

import com.google.android.gms.location.LocationRequest;

/**
 * Created by lokesh on 15/02/18.
 */

public  class AppConfig {

    /* Constants for precision defined here :
            https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest.html */
    public static int LOCATION_PRECISION_CODE = LocationRequest.PRIORITY_HIGH_ACCURACY;

    /*  This is precision value in metres, to be used to determine provider
            if the device DOES NOT have play services installed   */
    public static int LOCATION_PRECISION = 100;
    public static String LOCATION_PROVIDER = "gps";


    public static int LOCATION_INTERVAL_MILLIS = 1000; // in sec


    /* Used by job schedular */
    public static int SYNC_TIME = 15; // sync time in mins
    public static int SYNC_GRACE_TIME = 15; // refer doc

}
