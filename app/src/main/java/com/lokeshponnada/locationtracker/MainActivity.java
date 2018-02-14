package com.lokeshponnada.locationtracker;

import android.Manifest;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.persistence.room.Room;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.lokeshponnada.locationtracker.database.LocationModel;
import com.lokeshponnada.locationtracker.database.TrackerDatabase;
import com.lokeshponnada.locationtracker.repository.LocationRepository;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    //Butterknife starts here


    @BindView(R.id.trackBtn)
    Button trackBtn;

    // Butterknife ends here


    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private final int LOCATION_PERMISSION_CODE = 1;
    private final int CHECK_SETTINGS_CODE = 2;
    private final int LOCATION_INTERVAL_MILLIS = 5000;


    TrackerDatabase db;
    boolean currentlyTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // bind ui
        ButterKnife.bind(this);

        // set listeners
        setupListeners();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // todo can get last location here

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case LOCATION_PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initLocationStuff();
                }else{
                    Utils.showToast(getApplicationContext(),getString(R.string.cannot_track_pemission));
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }

    }


    private boolean startLocationUpdates() {
        if(checkLocationPermission()){
            mFusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null /* Looper */);
            return true;
        }else {
            return false;
        }

    }


    private void modifyUI(boolean isTracking){
        trackBtn.setText(isTracking ? getString(R.string.stop_tracking):getString(R.string.start_tracking));
        currentlyTracking = isTracking;
    }


    private void initLocationStuff() {

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // 100m accuracy as per documentation
                .setInterval(LOCATION_INTERVAL_MILLIS)
                .setFastestInterval(LOCATION_INTERVAL_MILLIS);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());


        task.addOnSuccessListener(locationSettingsResponse -> {
            if(startLocationUpdates()){
                modifyUI(true);
            }
        });

        task.addOnFailureListener(e -> {
                // todo communicate to user
            if (e instanceof ResolvableApiException) {
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MainActivity.this,
                            CHECK_SETTINGS_CODE);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }else{
                // todo fallback to the android location manager
            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    LocationModel locationModel = new LocationModel();
                    locationModel.setLat(location.getLatitude());
                    locationModel.setLng(location.getLongitude());
                    locationModel.setSource(location.getProvider());
                    locationModel.setTime(location.getTime());
                    LocationRepository.getRepository(getApplicationContext()).processLocation(locationModel);
                    Log.d("Lokesh",location.getProvider() + "-"+location.getTime() + "-"+location.getLatitude()+"-"+location.getLongitude());
                }
            }

        };


    }

    private void setupListeners(){

        trackBtn.setOnClickListener(v -> {
            if(currentlyTracking){
                stopTracking();
            }else{
                initLocationStuff();
            }
        });

    }


    private void stopTracking(){

        if(mFusedLocationClient != null && locationCallback != null){
            mFusedLocationClient.removeLocationUpdates(locationCallback);
            modifyUI(false);
            currentlyTracking = false;
        }
    }


    private boolean checkLocationPermission(){
        int res = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(res != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_CODE);

//            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
//                    showExplanationDiaog();
//            }else{
//                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_CODE);
//            }
            return false;
        }else{
            return true;
        }
    }


    private void showExplanationDiaog(){

    }
}
