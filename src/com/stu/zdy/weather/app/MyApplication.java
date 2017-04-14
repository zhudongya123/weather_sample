package com.stu.zdy.weather.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

import com.stu.zdy.weather.data.DBManager;
import com.stu.zdy.weather_sample.R;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONException;
import org.json.JSONObject;

public class MyApplication extends Application {
    public Context context;

    public SharedPreferences sharedPreferences;
    public Editor editor;
    public int runTimes;

    public static final String WEATHER_URL = "https://free-api.heweather.com/";
    public static final String APIKEY = "57efa20515e94db68ae042319463dba4";

    public static final String WEATHER_ICON_URL = "http://mr-zdy-shanghai.oss-cn-shanghai.aliyuncs.com/weather_icon_white/";

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        context = this;
        sharedPreferences = this.getSharedPreferences("weather_info", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        runTimes = sharedPreferences.getInt("runtimes", 1);
        if (runTimes == 1) {
            Toast.makeText(context, this.getResources().getString(R.string.loading_app), Toast.LENGTH_SHORT).show();
            DBManager.copyDataBaseFromRaw(context);
            editor.putBoolean("moreColor", true);
            editor.putInt("refreshTime", 28800000);
            editor.putBoolean("naviBar", false);
            editor.putBoolean("lifeAdvice", true);
            editor.putString("clockPackageName", "com.google.android.deskclock");
            editor.putBoolean("widget_mask", true);
            editor.putInt("runtimes", 2);
            editor.putString("currentCity", getResources().getString(R.string.currentcity));
            try {
                editor.putString("citylist", new JSONObject("{citylist:[" + "]}").toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            editor.commit();
        }
        CrashReport.initCrashReport(getApplicationContext(), "900022729", false);


    }

}
