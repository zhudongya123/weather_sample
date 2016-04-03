package com.stu.zdy.weather.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.stu.zdy.weather.mananger.SharePreferenceMananger;
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
                    mContext.sendBroadcast(new Intent(AppWidgetUtils.PackageNameBig));
                    mContext.sendBroadcast(new Intent(AppWidgetUtils.PackageNameSmall));
                    mContext.sendBroadcast(new Intent(AppWidgetUtils.PackageNameHuge));
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


    private void sendBroadCastForRefresh(int type) {
        if (type == 1) {
            refreshTimer = new Timer();
            refreshTimerTask = new TimerTask() {

                @Override
                public void run() {
                    mContext.sendBroadcast(new Intent(AppWidgetUtils.REFRESH));
                }
            };
            Calendar nextMinute = Calendar.getInstance();
            nextMinute.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE) + 1);
            nextMinute.set(Calendar.SECOND, 0);
            refreshTimer.schedule(refreshTimerTask, nextMinute.getTime(), 60000);
            mContext.sendBroadcast(new Intent(AppWidgetUtils.REFRESH));
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
        sendBroadCastForRefresh(1);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        sendBroadCastForWeather(0);
        sendBroadCastForRefresh(0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
