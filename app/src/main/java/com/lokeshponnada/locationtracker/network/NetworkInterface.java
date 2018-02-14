package com.lokeshponnada.locationtracker.network;

import com.lokeshponnada.locationtracker.database.LocationModel;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by lokesh on 14/02/18.
 */

public interface NetworkInterface {

    @Headers("Content-Type: application/json")
    @POST("client/test/user/candidate/location")
    Call<Void> postLocation(@Body NetworkModel networkModel);

}
