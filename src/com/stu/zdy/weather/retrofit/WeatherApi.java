package com.stu.zdy.weather.retrofit;

import com.google.gson.JsonObject;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Zdy on 2016/4/18.
 */
public interface WeatherApi {
    @Headers("apikey: 4a3a9afa23e0c5e7bb85b37ed53ed9d3")
    @GET("free")
    Observable<JsonObject> info(@Query("cityid") String id);
}
