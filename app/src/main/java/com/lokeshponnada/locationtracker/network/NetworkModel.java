package com.lokeshponnada.locationtracker.network;

import com.google.gson.annotations.SerializedName;

public class NetworkModel {


    public NetworkModel(double lat,double lng,float accuracy,String src,long timeStamp){
        location = new Location();
        location.lat = lat;
        location.lng = lng;
        location.accuracy = accuracy;
        location.provider = src;
        location.timestamp = timeStamp;
    }

    @SerializedName("location")
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public class Location {

        @SerializedName("lat")
        private double lat;
        @SerializedName("lng")
        private double lng;
        @SerializedName("accuracy")
        private float accuracy;
        @SerializedName("provider")
        private String provider;
        @SerializedName("timestamp")
        private long timestamp;

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

        public float getAccuracy() {
            return accuracy;
        }

        public void setAccuracy(float accuracy) {
            this.accuracy = accuracy;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

    }


}