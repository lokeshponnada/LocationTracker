package com.lokeshponnada.locationtracker;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.lokeshponnada.locationtracker.database.LocationModel;
import com.lokeshponnada.locationtracker.repository.LocationRepository;


public class LocationPostService extends Service {


    // Binder given to clients
    private final IBinder mBinder = new LocationBinder();

    LocationRepository repo;

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

    }



    public class LocationBinder extends Binder {
        LocationPostService getService() {
            return LocationPostService.this;
        }
    }


    public void processLocation(@NonNull Location location){
        LocationModel locationModel = new LocationModel(location);
        repo.processLocation(locationModel);
        Log.d("Lokesh",location.getProvider() + "-"+location.getTime() + "-"+location.getLatitude()+"-"+location.getLongitude());
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
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder ;
    }
}
