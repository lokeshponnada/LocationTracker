package com.lokeshponnada.locationtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.lokeshponnada.locationtracker.database.LocationModel;
import com.lokeshponnada.locationtracker.remoteconfig.AppConfig;
import com.lokeshponnada.locationtracker.repository.LocationRepository;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.lokeshponnada.locationtracker.remoteconfig.AppConfig.LOCATION_INTERVAL_MILLIS;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.trackBtn)
    Button trackBtn;


    // Play Services location
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    // Framework Location
    private LocationManager locationManager;
    private android.location.LocationListener locationListener;

    private final int LOCATION_PERMISSION_CODE = 1;
    private final int CHECK_SETTINGS_CODE = 2;


    boolean currentlyTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // bind ui
        ButterKnife.bind(this);

        // set listeners
        setupListeners();

    }

    private void setupListeners(){

        trackBtn.setOnClickListener(v -> {
            if(currentlyTracking){
                stopTracking();
                toggleUI();
            }else{
                initLocationStuff();
            }
        });

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

    private void toggleUI(){
        currentlyTracking = !currentlyTracking;
        trackBtn.setText(currentlyTracking ? getString(R.string.stop_tracking):getString(R.string.start_tracking));
    }


    @SuppressLint("MissingPermission")
    private void getLocationFromAndroidApi(){

        locationManager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);


        locationListener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                processLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager.requestLocationUpdates(AppConfig.LOCATION_PROVIDER, AppConfig.LOCATION_INTERVAL_MILLIS, AppConfig.LOCATION_PRECISION, locationListener);
        toggleUI();
    }


    @SuppressLint("MissingPermission")
    private void getLocationFromPlayServices(){

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest()
                .setPriority(AppConfig.LOCATION_PRECISION_CODE)
                .setInterval(LOCATION_INTERVAL_MILLIS)
                .setFastestInterval(LOCATION_INTERVAL_MILLIS);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    processLocation(location);
                }
            }

        };

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());


        task.addOnSuccessListener(locationSettingsResponse -> {
            mFusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null /* Looper */);
            toggleUI();
        });

        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MainActivity.this,
                            CHECK_SETTINGS_CODE);
                } catch (IntentSender.SendIntentException sendEx) {
                }
            }else{
                // fallback to framework api
                getLocationFromAndroidApi();
            }
        });



    }


    private void processLocation(@NonNull Location location){
        LocationModel locationModel = new LocationModel(location);
        LocationRepository.getRepository(getApplicationContext()).processLocation(locationModel);
        Log.d("Lokesh",location.getProvider() + "-"+location.getTime() + "-"+location.getLatitude()+"-"+location.getLongitude());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case CHECK_SETTINGS_CODE:
                if(resultCode == RESULT_OK){
                    getLocationFromPlayServices();
                }else{
                    // User denied to turn on location
                }
                break;

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initLocationStuff() {

        if(!checkLocationPermission()){
            return;
        }

        if(Utils.isGooglePlayServicesAvailable(getApplicationContext())){
            getLocationFromPlayServices();
        }else{
            getLocationFromAndroidApi();
        }

    }


    private void stopTracking(){

        if(locationManager != null){
            locationManager.removeUpdates(locationListener);
        }

        if(mFusedLocationClient != null && locationCallback != null){
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }

    }

    private boolean checkLocationPermission(){
        int res = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(res != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_CODE);
            return false;
        }else{
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        stopTracking(); // Orientation change shouldn't leak activity
        super.onDestroy();
    }
}
