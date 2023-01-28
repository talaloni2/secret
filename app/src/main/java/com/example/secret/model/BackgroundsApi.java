package com.example.secret.model;

import retrofit2.Call;
import retrofit2.http.GET;

public interface BackgroundsApi {

    @GET("random?client_id=R-OCMeu2YkZNPSv4qgBYXnDhcY2gdsb0y_ZNksTgAbo")
    Call<ExternalBackground> getRandomBackground();
}
