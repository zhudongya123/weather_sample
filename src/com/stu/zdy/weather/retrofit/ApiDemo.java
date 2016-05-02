package com.stu.zdy.weather.retrofit;

import com.stu.zdy.weather.app.MyApplication;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Zdy on 2016/4/18.
 */
public class ApiDemo {
    public static WeatherApi getWeatherInfo(OkHttpClient okHttpClient) {
        WeatherApi weatherApi;
        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(MyApplication.WEATHER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        weatherApi = retrofit.create(WeatherApi.class);
        return weatherApi;
    }
}
