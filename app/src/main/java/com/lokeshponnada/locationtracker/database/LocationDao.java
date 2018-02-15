package com.lokeshponnada.locationtracker.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by lokesh on 14/02/18.
 */

@Dao
public interface LocationDao {


    @Query("SELECT * FROM locations")
    List<LocationModel> getAllLocations();

    @Query("SELECT * FROM locations WHERE posted = 'false'")
    List<LocationModel> getNonPostedLocations();

    @Insert
    long addLocation(LocationModel location);

    @Insert
    long[]  addMultipleLocations(LocationModel... locations);

    @Update(onConflict = REPLACE)
    void updateLocation(LocationModel model);


}
