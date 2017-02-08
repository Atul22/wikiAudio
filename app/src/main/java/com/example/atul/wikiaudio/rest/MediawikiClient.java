package com.example.atul.wikiaudio.rest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MediawikiClient {
    @POST("/?action=login&format=json")
    Call<ResponseBody> login(
            @Query("lgname") String username,
            @Query("lgpassword") String password,
            @Query("lgtoken") String token
    );
}
