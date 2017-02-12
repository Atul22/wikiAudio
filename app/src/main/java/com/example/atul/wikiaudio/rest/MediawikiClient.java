package com.example.atul.wikiaudio.rest;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

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

    @Multipart
    @POST("./")
    Call<ResponseBody> uploadFile(
            @Part("action") RequestBody action,
            @Part("filename") RequestBody filename,
            @Part("token") RequestBody token,
            @Part MultipartBody.Part file,
            @Part("text") RequestBody text
    );
}
