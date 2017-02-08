package com.example.atul.wikiaudio.rest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface MediawikiClient {
    @FormUrlEncoded
    @POST("./")
    Call<ResponseBody> getToken(
            @Field("action") String action,
            @Field("meta") String meta,
            @Field("type") String type
    );

    @FormUrlEncoded
    @POST("./")
    Call<ResponseBody> login(
            @Field("action") String action,
            @Field("lgname") String username,
            @Field("lgpassword") String password,
            @Field("lgtoken") String token
    );
}
