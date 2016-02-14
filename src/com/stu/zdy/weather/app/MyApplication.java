package com.stu.zdy.weather.app;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

import com.stu.zdy.weather.db.DBManager;

public class MyApplication extends Application {
	public Context context;

	public SharedPreferences sharedPreferences;
	public Editor editor;
	public int runTimes;
	public static final String WEATHER_URL = "http://apis.baidu.com/heweather/weather/free";
	public static final String APIKEY = "4a3a9afa23e0c5e7bb85b37ed53ed9d3";

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		context = this;
		sharedPreferences = this.getSharedPreferences("weather_info",
				Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();
		runTimes = sharedPreferences.getInt("runtimes", 1);
		if (runTimes == 1) {
			Toast.makeText(context, "正在初始化应用...", Toast.LENGTH_SHORT).show();
			DBManager.copyDataBaseFromRaw(context);
			editor.putBoolean("moreColor", true);
			editor.putInt("refreshTime", 28800000);
			editor.putBoolean("naviBar", false);
			editor.putBoolean("lifeAdvice", true);
			editor.putString("clockPackageName", "com.google.android.deskclock");
			editor.putInt("runtimes", 2);
			try {
				editor.putString("citylist", new JSONObject("{citylist:["
						+ "]}").toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			editor.commit();
		}
	}
}
