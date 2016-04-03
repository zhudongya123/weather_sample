package com.stu.zdy.weather.view;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.stu.zdy.weather_sample.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by Zdy on 2016/3/27.
 */
public class MyBaseAppWidgetProvider extends AppWidgetProvider {
    protected Context mContext;

    protected void opTimerTask() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        mContext = context;
    }

    protected void updateWeatherView(JSONObject jsonObject) throws JSONException {
        Calendar calendar = Calendar.getInstance();
        Toast.makeText(mContext, mContext.getString(R.string.loading_time) + calendar.getTime().toLocaleString(), Toast.LENGTH_SHORT).show();
    }

}
