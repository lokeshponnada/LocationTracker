package com.lokeshponnada.locationtracker;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.lokeshponnada.locationtracker.database.LocationModel;
import com.lokeshponnada.locationtracker.remoteconfig.AppConfig;
import com.lokeshponnada.locationtracker.repository.LocationRepository;

import static com.lokeshponnada.locationtracker.remoteconfig.AppConfig.LOCATION_SYNC_MILLIS;
import static com.lokeshponnada.locationtracker.remoteconfig.AppConfig.LOCATION_TIME_THRESHOLD;


public class LocationPostService extends Service {

    private final IBinder mBinder = new LocationBinder();

    LocationRepository repo;

    LocationModel locationModel = new LocationModel();

    Runnable periodicUpdate;
    Handler handler;


    @Override
    public void onCreate() {
        super.onCreate();

        repo = LocationRepository.getRepository(getApplicationContext());

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);


        String CHANNEL_ID = "location_tracking";

        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.tracking_now))
                .setContentIntent(pendingIntent).build();

        startForeground(1, notification);

        Log.d("Lokesh","Service Created");

        handler = new Handler();

       periodicUpdate = new Runnable() {
           @Override
           public void run() {
               handler.postDelayed(periodicUpdate, LOCATION_SYNC_MILLIS);
               if(locationModel != null){
                   processLocation(LocationModel.getCopy(locationModel));
               }else{
                   // wait for 2 more mins
               }
           }
       };


    }


    public class LocationBinder extends Binder {
        LocationPostService getService() {
            return LocationPostService.this;
        }
    }

    public void processLocation(LocationModel model){
        repo.processLocation(model);
        Log.d("Lokesh",model.getSource() + "-"+model.getTime() + "-"+model.getLat()+"-"+model.getLng());
    }

    public void updateLocation(@Nullable Location location){

        if(Utils.distanceInMetresBetweenEarthCoordinates
                (location.getLatitude(),location.getLongitude(),locationModel.getLat(),locationModel.getLng()) >=
                AppConfig.LOCATION_DISTANCE_THRESHOLD){
            handler.removeCallbacksAndMessages(null);
            locationModel = new LocationModel(location);
            processLocation(LocationModel.getCopy(locationModel));
            handler.postDelayed(periodicUpdate,LOCATION_SYNC_MILLIS);
            Log.d("Lokesh","Update due to distance threshold");
        }else{
            locationModel = new LocationModel(location);
        }
    }


    @Override
    public boolean stopService(Intent name) {
        Log.d("Lokesh","Stopping Service");
        stopSelf();
        return false;
    }

    @Override
    public void onDestroy() {
        Log.d("Lokesh","Service Destroyed");
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder ;
    }
}
