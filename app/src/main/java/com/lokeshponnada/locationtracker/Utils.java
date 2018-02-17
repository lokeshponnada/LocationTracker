package com.lokeshponnada.locationtracker;

import android.Manifest;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by lokesh on 14/02/18.
 */

public class Utils {


    public static void showToast(Context ctx,String msg){
        Toast.makeText(ctx,msg,Toast.LENGTH_LONG).show();
    }

    public static boolean isGooglePlayServicesAvailable(Context context){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
//        return resultCode == ConnectionResult.SUCCESS;
        return false;
    }

    public static boolean isProviderEnabled(@NonNull LocationManager manager, @NonNull String provider){
        return manager.isProviderEnabled(provider);
    }

    // Can be false positive in case of limited connectivity
    public static boolean isConnectedToNetwork(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }


    public static String getPermission(int precision){
        return Manifest.permission.ACCESS_FINE_LOCATION;
    }

}
