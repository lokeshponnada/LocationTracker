package com.lokeshponnada.locationtracker;

import android.content.Context;
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

    public boolean isGooglePlayServicesAvailable(Context context){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

}
