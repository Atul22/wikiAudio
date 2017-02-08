package com.example.atul.wikiaudio.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {
    private static final String BASE_URL = "https://en.wikipedia.org/w/api.php/";

    private static Retrofit retrofit;

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    private static HttpLoggingInterceptor logging =
            new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY);

    private static Interceptor queryParamInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            HttpUrl originalHttpUrl = original.url();

            HttpUrl url = originalHttpUrl.newBuilder()
                    .addQueryParameter("format", "json")
                    .build();

            // Request customization: add request headers
            Request.Builder requestBuilder = original.newBuilder()
                    .url(url);

            Request request = requestBuilder.build();
            return chain.proceed(request);
        }
    };

    private static CookieJar cookieJar = new CookieJar() {
        private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            cookieStore.put(url, cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url);
            return cookies != null ? cookies : new ArrayList<Cookie>();
        }
    };

    private static OkHttpClient.Builder httpClient =
            new OkHttpClient.Builder().cookieJar(cookieJar);

    public static <S> S createService(
            Class<S> serviceClass) {
        boolean rebuild = false;
        if (!httpClient.interceptors().contains(logging)) {
            httpClient.addInterceptor(logging);
            rebuild = true;
        }
        if (!httpClient.interceptors().contains(queryParamInterceptor)) {
            httpClient.addInterceptor(queryParamInterceptor);
            rebuild = true;
        }

        if (rebuild) {
            builder.client(httpClient.build());
            retrofit = builder.build();
        }

        return retrofit.create(serviceClass);
    }
}
