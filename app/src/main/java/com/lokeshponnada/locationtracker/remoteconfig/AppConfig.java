package com.lokeshponnada.locationtracker.remoteconfig;

import com.google.android.gms.location.LocationRequest;

/**
 * Created by lokesh on 15/02/18.
 */

public  class AppConfig {

    public static final String DB_NAME = "location_db";

    /* Constants for precision defined here :
            https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest.html */
    public static int LOCATION_PRECISION_CODE = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

    /*  This is precision value in metres, to be used to determine provider
            if the device DOES NOT have play services installed   */
    public static int LOCATION_DISTANCE_THRESHOLD = 100;
    public static String LOCATION_PROVIDER = "network";
    public static String LOCATION_PERMISSION = "android.permission.ACCESS_COARSE_LOCATION"; // needed when using framework api


    public static int LOCATION_TIME_THRESHOLD = 2*60*1000; // in sec

    public static int LOCATION_SYNC_MILLIS = 2*60*1000; // Location sync time with server


    /* Used by job schedular */
    public static int SYNC_TIME = 15; // sync time in mins
    public static int SYNC_GRACE_TIME = 15; // refer doc

}
