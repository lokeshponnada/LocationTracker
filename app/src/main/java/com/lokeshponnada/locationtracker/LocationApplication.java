package com.lokeshponnada.locationtracker;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by lokesh on 15/02/18.
 */

public class LocationApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
