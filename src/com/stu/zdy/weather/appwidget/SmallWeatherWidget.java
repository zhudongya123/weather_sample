package com.stu.zdy.weather.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.stu.zdy.weather.interfaces.WeatherCallBack;
import com.stu.zdy.weather.mananger.SharePreferenceMananger;
import com.stu.zdy.weather.net.JsonDataAnalysisByBaidu;
import com.stu.zdy.weather.ui.MainActivity;
import com.stu.zdy.weather.util.ApplicationUtils;
import com.stu.zdy.weather.util.NetWorkUtils;
import com.stu.zdy.weather.retrofit.OkHttpUtils;
import com.stu.zdy.weather.view.MyBaseAppWidgetProvider;
import com.stu.zdy.weather_sample.R;

import org.json.JSONException;
import org.json.JSONObject;

public class SmallWeatherWidget extends MyBaseAppWidgetProvider {
    private static final String PackageName = "com.stu.zdy.weather.small";

    private Context mContext;
    private String cityName;


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.v("SmallWeatherWidget", "onUpdate");
        mContext = context;
        cityName = SharePreferenceMananger.getSharePreferenceFromString(mContext, "weather_info", "currentCity");
        prepareHttpRequest();
        ApplicationUtils.runService(mContext);
    }

    private void prepareHttpRequest() {
        Log.v("SmallWeatherWidget", "prepareHttpRequest");
        if (NetWorkUtils.getConnectedType(mContext) != -1) {
            OkHttpUtils okHttpUtils = new OkHttpUtils(new WeatherCallBack() {

                @Override
                public void onUpdate(String result) {
                    try {
                        updateWeatherView(new JSONObject(result));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            okHttpUtils.run(new Handler(), cityName);
        }
    }

    @Override
    protected void updateWeatherView(JSONObject jsonObject) throws JSONException {
        Log.v("SmallWeatherWidget", "updateWeatherView");
        Bundle bundle = new JsonDataAnalysisByBaidu(jsonObject.toString()).getBundle();
        if (!"ok".equals(bundle.getString("status"))) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.sever_error), Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        RemoteViews views = new RemoteViews(mContext.getPackageName(),
                R.layout.widget_small);
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
                intent, 0);
        views.setOnClickPendingIntent(R.id.small_root, pendingIntent);
        try {
            views.setTextViewText(R.id.small_city, bundle.getStringArrayList("item1").get(0));
            views.setTextViewText(R.id.small_temper,
                    bundle.getStringArrayList("item1").get(6) + mContext.getResources().getString(R.string.degree));
            views.setTextViewText(R.id.small_weather, bundle.getStringArrayList("item1").get(3));
            views.setTextViewText(R.id.small_fresh, bundle.getStringArrayList("item1").get(2) + mContext.getString(R.string.refresh));

        } catch (NullPointerException e) {

        }
        if (SharePreferenceMananger.getSharePreferenceFromBoolean(mContext, "weather_info", "widget_mask"))
            views.setViewVisibility(R.id.small_widget_mask, View.VISIBLE);
        else views.setViewVisibility(R.id.small_widget_mask, View.INVISIBLE);

        int code = Integer.parseInt(bundle.getStringArrayList("item1").get(7));
        switch (code) {
            case 100:
            case 102:
            case 103:
                views.setImageViewResource(R.id.weather_ic,
                        R.drawable.sunny_pencil);
                break;
            case 101:
                views.setImageViewResource(R.id.weather_ic,
                        R.drawable.cloudy_pencil);
                break;
            case 104:
                views.setImageViewResource(R.id.weather_ic,
                        R.drawable.overcast_pencil);
                break;
            case 302:
            case 303:
            case 304:
                views.setImageViewResource(R.id.weather_ic,
                        R.drawable.storm_pencil);
                break;
            case 301:
            case 305:
            case 306:
            case 307:
            case 308:
            case 309:
            case 310:
            case 311:
            case 312:
            case 313:
                views.setImageViewResource(R.id.weather_ic,
                        R.drawable.rain_pencil);
                break;
            case 400:
            case 401:
            case 402:
            case 403:
            case 404:
            case 405:
            case 406:
            case 407:
                views.setImageViewResource(R.id.weather_ic,
                        R.drawable.snow_pencil);
                break;
        }
        ComponentName thisWidget = new ComponentName(mContext, SmallWeatherWidget.class);
        AppWidgetManager.getInstance(mContext).updateAppWidget(thisWidget, views);
        super.updateWeatherView(jsonObject);
    }
}
