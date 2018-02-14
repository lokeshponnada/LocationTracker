package com.lokeshponnada.locationtracker.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by lokesh on 14/02/18.
 */

@Dao
public interface LocationDao {


    @Query("SELECT * FROM locations")
    List<LocationModel> getAllLocations();

//    @Query("SELECT * FROM locations WHERE time  > reqTime")
//    List<LocationModel> getLocations(long reqTime);

    @Insert
    void addLocation(LocationModel location);

    @Insert
    void  addMultipleLocations(LocationModel... locations);

}
