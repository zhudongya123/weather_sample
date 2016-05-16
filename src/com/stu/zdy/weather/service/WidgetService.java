package com.stu.zdy.weather.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.stu.zdy.weather.interfaces.WeatherCallBack;
import com.stu.zdy.weather.mananger.SharePreferenceMananger;
import com.stu.zdy.weather.retrofit.OkHttpUtils;
import com.stu.zdy.weather.util.AppWidgetUtils;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class WidgetService extends Service {
    private Context mContext;
    private int time = 14400000;
    private Timer weatherTimer = null;
    private TimerTask weatherTimerTask = null;

    private Timer refreshTimer = null;
    private TimerTask refreshTimerTask = null;


    private void sendBroadCastForWeather(int type) {
        if (type == 1) {
            weatherTimer = new Timer();
            weatherTimerTask = new TimerTask() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    String result = SharePreferenceMananger.getSharePreferenceFromString(mContext, "weather_info", "currentCity");
                    prepareHttpRequest(result);
                }
            };
            weatherTimer.schedule(weatherTimerTask, Calendar.getInstance().getTime(), time);
        } else {
            try {
                weatherTimer.cancel();
                weatherTimerTask.cancel();
                weatherTimer = null;
                weatherTimerTask = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void prepareHttpRequest(String city) {
        OkHttpUtils.runRxjava(city, new WeatherCallBack() {
            @Override
            public void onUpdate(String result) {
                Intent intent = new Intent();
                intent.putExtra("result", result);
                intent.setAction(AppWidgetUtils.BroadCast_ReFresh_Weather);
                mContext.sendBroadcast(intent);
            }
        });
    }

    private void sendBroadCastForRefreshTimes(int type) {
        if (type == 1) {
            refreshTimer = new Timer();
            refreshTimerTask = new TimerTask() {

                @Override
                public void run() {
                    mContext.sendBroadcast(new Intent(AppWidgetUtils.BroadCast_ReFresh_Time));
                }
            };
            Calendar nextMinute = Calendar.getInstance();
            nextMinute.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE) + 1);
            nextMinute.set(Calendar.SECOND, 0);
            refreshTimer.schedule(refreshTimerTask, nextMinute.getTime(), 60000);
            mContext.sendBroadcast(new Intent(AppWidgetUtils.BroadCast_ReFresh_Time));
        } else {
            try {
                refreshTimer.cancel();
                refreshTimerTask.cancel();
                refreshTimer = null;
                refreshTimerTask = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.v("onStartCommand", "onStartCommand");
        mContext = getApplicationContext();
        time = SharePreferenceMananger.getSharePreferenceFromInteger(this, "weather_info", "refreshTime");
        sendBroadCastForWeather(1);
        sendBroadCastForRefreshTimes(1);
        IntentFilter filter = new IntentFilter(AppWidgetUtils.BroadCast_ReFresh_Notify_Service);
        MyBroadCastReceive broadCastReceive = new MyBroadCastReceive();
        registerReceiver(broadCastReceive, filter);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        sendBroadCastForWeather(0);
        sendBroadCastForRefreshTimes(0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    class MyBroadCastReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(AppWidgetUtils.BroadCast_ReFresh_Notify_Service)) {
                String result = SharePreferenceMananger.getSharePreferenceFromString(mContext, "weather_info", "currentCity");
                prepareHttpRequest(result);
            }
        }
    }
}
