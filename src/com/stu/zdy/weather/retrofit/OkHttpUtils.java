package com.stu.zdy.weather.retrofit;

import android.os.Handler;
import android.util.Log;

import com.google.gson.JsonObject;
import com.stu.zdy.weather.app.MyApplication;
import com.stu.zdy.weather.data.DBManager;
import com.stu.zdy.weather.interfaces.WeatherCallBack;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class OkHttpUtils {

    public static OkHttpClient mOkHttpClient = null;
    public WeatherCallBack callBack;

    public OkHttpUtils(WeatherCallBack weatherCallBack) {
        this.callBack = weatherCallBack;
    }


    public void run(final Handler handler, String city) {
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient();
        }
        Request request = new Request.Builder()
                .url(MyApplication.WEATHER_URL + "?cityid=" + DBManager.getIdByCityName(city))
                .header("apikey", MyApplication.APIKEY).build();
    }

    public static void runRxjava(String city, final WeatherCallBack callback) {
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient.Builder().addNetworkInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    return chain.proceed(request);
                }
            }).build();
        }


        ApiDemo.getWeatherInfo(mOkHttpClient)
                .info(DBManager.getIdByCityName(city), MyApplication.APIKEY)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<JsonObject>() {
                    @Override
                    public void call(JsonObject s) {
                        Log.v("result", "result" + s.toString());
                        callback.onUpdate(s.toString());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.v("Error", throwable.getMessage());
                    }
                });


    }

}
