package com.stu.zdy.weather.util;

import android.os.Handler;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.stu.zdy.weather.app.MyApplication;
import com.stu.zdy.weather.data.DBManager;
import com.stu.zdy.weather.interfaces.WeatherCallBack;

import java.io.IOException;

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

		mOkHttpClient.newCall(request).enqueue(new Callback() {

			@Override
			public void onResponse(final Response response) throws IOException {
				// TODO Auto-generated method stub
				final String res = response.body().string();
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Log.v("receiveData", res);
						callBack.onUpdate(res);
					}
				});
			}

			@Override
			public void onFailure(Request arg0, IOException arg1) {
				// TODO Auto-generated method stub

			}
		});
	}

}
