package com.stu.zdy.weather.util;

import java.io.IOException;

import android.content.Context;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.stu.zdy.weather.app.MyApplication;
import com.stu.zdy.weather.db.DBManager;
import com.stu.zdy.weather.interfaces.WeatherCallBack;

public class OkHttpUtils {

	public static OkHttpClient mOkHttpClient = null;
	public WeatherCallBack callBack;

	public OkHttpUtils(WeatherCallBack weatherCallBack) {
		this.callBack = weatherCallBack;
	}

	public void run(Context context, String city) {
		if (mOkHttpClient == null) {
			mOkHttpClient = new OkHttpClient();
		}

		Request request = new Request.Builder()
				.url(MyApplication.WEATHER_URL + "?cityid="
						+ DBManager.getIdByCityName(city))
				.header("apikey", MyApplication.WEATHER_URL).build();

		Call call = mOkHttpClient.newCall(request);
		try {
			call.execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		call.enqueue(new Callback() {

			@Override
			public void onResponse(Response response) throws IOException {
				// TODO Auto-generated method stub
				callBack.onUpdate();
				final String res = response.body().string();
			}

			@Override
			public void onFailure(Request arg0, IOException arg1) {
				// TODO Auto-generated method stub

			}
		});
	}

}


