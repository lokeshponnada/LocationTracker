package com.lokeshponnada.locationtracker.sync;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by lokesh on 15/02/18.
 */

public class LocationJobCreator implements JobCreator{
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case LocationSyncJob.TAG:
                return new LocationSyncJob();
            default:
                return null;
        }
    }
}
