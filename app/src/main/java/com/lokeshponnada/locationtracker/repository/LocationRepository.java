package com.lokeshponnada.locationtracker.repository;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.lokeshponnada.locationtracker.Utils;
import com.lokeshponnada.locationtracker.database.LocationModel;
import com.lokeshponnada.locationtracker.database.TrackerDatabase;
import com.lokeshponnada.locationtracker.network.NetworkInterface;
import com.lokeshponnada.locationtracker.network.NetworkModel;
import com.lokeshponnada.locationtracker.network.RetroSingleton;

import javax.security.auth.callback.Callback;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by lokesh on 14/02/18.
 */

public class LocationRepository {

    private static LocationRepository locationRepository;
    private TrackerDatabase db;
    private static Context ctx;

    private LocationRepository(Context context){
        ctx = context;
        if(db == null){
            db = Room.databaseBuilder(context.getApplicationContext(),
                    TrackerDatabase.class, "location_db").build();
        }
    }

    public static synchronized LocationRepository getRepository(@NonNull Context context){
        if(locationRepository == null){
            locationRepository = new LocationRepository(context);
        }
        return locationRepository;
    }



    public void processLocation(LocationModel locationModel){

        // Add to db


        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                 db.locationDao().addLocation(locationModel);
                 return null;
            }

            @Override
            protected void onPostExecute(Void agentsCount) {
                Log.d("Lokesh","Added Entry ");
            }
        }.execute();

        if(Utils.isConnectedToNetwork(ctx)){
            postLocation(locationModel);
        }else {
            // Already saved , nothing to do
        }

    }

    public void postLocation(LocationModel locationModel){
        NetworkModel networkModel = new NetworkModel(locationModel.getLat(),locationModel.getLng(),0.0f,locationModel.getSource(),locationModel.getTime());
        Call<Void> postCall = RetroSingleton.getNetworkService().postLocation(networkModel);
        postCall.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(ctx,"This is response code : "+response.code(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ctx,"Error ",Toast.LENGTH_LONG).show();
            }
        });
    }

}
