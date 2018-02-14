package com.lokeshponnada.locationtracker.network;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by lokesh on 14/02/18.
 */

public class RetroSingleton {

    static  OkHttpClient okHttpClient = new OkHttpClient().newBuilder().addInterceptor(new Interceptor() {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();

            Request.Builder builder = originalRequest.newBuilder().header("Authorization",
                    Credentials.basic("test/candidate", "c00e-4764"));

            Request newRequest = builder.build();
            return chain.proceed(newRequest);
        }
    }).build();

    static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.locus.sh/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    static NetworkInterface networkInterface;

    public static NetworkInterface getNetworkService(){
        if(networkInterface == null){
            networkInterface = retrofit.create(NetworkInterface.class);
        }
        return networkInterface;
    }




}
