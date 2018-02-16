package com.lokeshponnada.locationtracker.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.location.Location;

/**
 * Created by lokesh on 14/02/18.
 */


@Entity(tableName = "locations")
public class LocationModel {

    public LocationModel(){}

    public LocationModel(Location location){
        super();
        lat = location.getLatitude();
        lng = location.getLongitude();
        source = location.getProvider();
        time = location.getTime();
    }

    @PrimaryKey(autoGenerate = true)
    private long _id;

    private double lat;

    private double lng;

    private String source;

    private long time;

    private boolean posted;

    public boolean isPosted() {
        return posted;
    }

    public void setPosted(boolean posted) {
        this.posted = posted;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }



}
