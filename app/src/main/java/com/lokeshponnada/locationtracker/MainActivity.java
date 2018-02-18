package com.lokeshponnada.locationtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.lokeshponnada.locationtracker.remoteconfig.AppConfig;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.lokeshponnada.locationtracker.remoteconfig.AppConfig.LOCATION_TIME_THRESHOLD;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.trackBtn)
    Button trackBtn;

    private Dialog dialog;
    boolean currentlyTracking;


    // Play Services location
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    // Framework Location
    private LocationManager locationManager;
    private android.location.LocationListener locationListener;

    // Service related
    LocationPostService locationService;
    boolean mBound = false;


    private final int LOCATION_PERMISSION_CODE = 1;
    private final int CHECK_SETTINGS_CODE = 2;
    private final int GPS_REQUEST_CODE = 3;

    Location lastKnownLocation;


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

        if(!locationManager.isProviderEnabled(AppConfig.LOCATION_PROVIDER)){
            turnOnProvider(getString(R.string.enable_provider));
            return;
        }

        setFrameworkLocationListener();

        locationManager.requestLocationUpdates(AppConfig.LOCATION_PROVIDER, 0, AppConfig.LOCATION_DISTANCE_THRESHOLD, locationListener);
        startLocationService();
        toggleUI();

        lastKnownLocation = locationManager.getLastKnownLocation(AppConfig.LOCATION_PROVIDER);
    }

    private void setPlayServicesLocationListener(){

        if(locationCallback == null){

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        postLocation(location);
                    }
                }

            };
        }
    }

    private void setFrameworkLocationListener(){

        if(locationListener == null){

            locationListener = new android.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    postLocation(location);
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
        }
    }

    private void startLocationService(){

        Thread t = new Thread(){
            @Override
            public void run(){
                Intent locationService = new Intent(MainActivity.this, LocationPostService.class);
                bindService(locationService,mConnection,Context.BIND_AUTO_CREATE);
            }
        };
        t.start();

    }

    private void postLocation(@NonNull Location location){
        if (mBound) {
            locationService.updateLocation(location);
        }
    }


    @SuppressLint("MissingPermission")
    private void getLocationFromPlayServices(){

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest()
                .setPriority(AppConfig.LOCATION_PRECISION_CODE)
                .setInterval(LOCATION_TIME_THRESHOLD);

        setPlayServicesLocationListener();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());


        task.addOnSuccessListener(locationSettingsResponse -> {
            mFusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null /* Looper */);
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    lastKnownLocation = location;
                }
            });

            startLocationService();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case CHECK_SETTINGS_CODE:
                if(resultCode == RESULT_OK){
                    getLocationFromPlayServices();
                }else{
                    Utils.showToast(getApplicationContext(),getString(R.string.cannot_track_turnon));
                }
                break;
            case GPS_REQUEST_CODE:
                if(locationManager.isProviderEnabled(AppConfig.LOCATION_PROVIDER)){
                    getLocationFromAndroidApi();
                }else {
                    Utils.showToast(getApplicationContext(),getString(R.string.cannot_track_turnon));
                }
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

    private void stopService(){
        unbindService(mConnection);
        mBound = false;
        locationService.stopSelf();
    }

    private void stopTracking(){

        if(locationManager != null){
            locationManager.removeUpdates(locationListener);
        }

        if(mFusedLocationClient != null && locationCallback != null){
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }

        stopService();

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


    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            LocationPostService.LocationBinder binder = (LocationPostService.LocationBinder) service;
            locationService = binder.getService();
            mBound = true;
            if(lastKnownLocation != null){
                postLocation(lastKnownLocation);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    private void turnOnProvider(String msg){

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),GPS_REQUEST_CODE))
                .setNegativeButton(getString(R.string.no), (dialog, id) -> dialog.cancel());
        dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        stopTracking();
        super.onDestroy();
    }
}
