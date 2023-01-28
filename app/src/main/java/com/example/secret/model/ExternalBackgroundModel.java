package com.example.secret.model;

import android.util.Log;

import com.example.secret.interfaces.Listener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ExternalBackgroundModel {
    final public static ExternalBackgroundModel instance = new ExternalBackgroundModel();

    final String BASE_URL = "https://api.unsplash.com/photos/";
    Retrofit retrofit;
    BackgroundsApi backgroundsApi;

    private ExternalBackgroundModel(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        backgroundsApi = retrofit.create(BackgroundsApi.class);
    }

    public static ExternalBackgroundModel getInstance(){
        return instance;
    }

    public void getRandomBackground(Listener<ExternalBackground> success, Listener<Void> fail){
        String TAG = "GetRandomBackground";
        backgroundsApi.getRandomBackground().enqueue(new Callback<ExternalBackground>() {
            @Override
            public void onResponse(Call<ExternalBackground> call, Response<ExternalBackground> response) {
                if (!response.isSuccessful()){
                    Log.w(TAG, "Response was not successful");
                    return;
                }
                Log.d(TAG, "Success");
                success.onComplete(response.body());
            }

            @Override
            public void onFailure(Call<ExternalBackground> call, Throwable t) {
                Log.e(TAG,"Could not get random background", t);
                fail.onComplete(null);
            }
        });
    }
}
