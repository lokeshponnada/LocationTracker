package com.lokeshponnada.locationtracker.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by lokesh on 14/02/18.
 */


@Database(entities = {LocationModel.class},version = 1)
public abstract class TrackerDatabase extends RoomDatabase {
    public abstract LocationDao locationDao();
}
