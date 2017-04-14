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
    @GET("v5/weather")
    Observable<JsonObject> info(@Query("city") String id, @Query("key") String key);
}
