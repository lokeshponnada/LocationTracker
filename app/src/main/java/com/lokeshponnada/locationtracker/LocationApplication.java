package com.lokeshponnada.locationtracker;

import android.app.Application;

import com.evernote.android.job.JobManager;
import com.facebook.stetho.Stetho;
import com.lokeshponnada.locationtracker.background.LocationJobCreator;

/**
 * Created by lokesh on 15/02/18.
 */

public class LocationApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        // todo remove in release
        Stetho.initializeWithDefaults(this);

        JobManager.create(this).addJobCreator(new LocationJobCreator());
    }
}
