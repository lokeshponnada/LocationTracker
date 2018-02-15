package com.lokeshponnada.locationtracker.repository;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.graphics.Bitmap;
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
    private static TrackerDatabase db;
    private static Context ctx;

    private LocationRepository(Context context){
        ctx = context;
        getDb(context);
    }

    public TrackerDatabase getDb(Context context){
        if(db == null){
            db = Room.databaseBuilder(context.getApplicationContext(),
                    TrackerDatabase.class, "location_db").build();
        }
        return db;
    }

    public static synchronized LocationRepository getRepository(@NonNull Context context){
        if(locationRepository == null){
            locationRepository = new LocationRepository(context);
        }
        return locationRepository;
    }


    public void processLocation(LocationModel locationModel){

        // todo replace with intent service ?
        new DBAsynkTask().execute(new LocationModel[]{locationModel});
    }


    // todo make this non static , so that ctx is not abused
    public static void postLocation(LocationModel locationModel){

        if(!Utils.isConnectedToNetwork(ctx)){
            return;
        }

        NetworkModel networkModel = new NetworkModel(locationModel.getLat(),locationModel.getLng(),0.0f,locationModel.getSource(),locationModel.getTime());
        Call<Void> postCall = RetroSingleton.getNetworkService().postLocation(networkModel);
        postCall.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                locationModel.setPosted(true);
                // todo must change later
                Log.d("Lokesh_JOB","Posted : " + locationModel.get_id());
                if(locationModel.get_id() != -1){
                    new BackgroundTask().doInBackground(locationModel);
                }else{
                    // location failed to save in db but was reported , lost from history ?
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                //todo location already saved in db. Job Schedular will take care
            }
        });
    }


    private static class DBAsynkTask extends AsyncTask<LocationModel,Void,Void>{

        @Override
        protected Void doInBackground(LocationModel... locationModels) {
            long index = db.locationDao().addLocation(locationModels[0]);
            locationModels[0].set_id(index);
            postLocation(locationModels[0]);
            return  null;
        }

    }


    private static class BackgroundTask extends AsyncTask<LocationModel, Void, Void> {

        @Override
        protected Void doInBackground(LocationModel... locationModels) {
            try{
                db.locationDao().updateLocation(locationModels[0]);
            }catch (Exception e){
                // update failed
            }
            return null;
        }
    }

}
