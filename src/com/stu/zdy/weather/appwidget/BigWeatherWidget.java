package com.stu.zdy.weather.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.stu.zdy.weather.app.MyApplication;
import com.stu.zdy.weather.mananger.SharePreferenceMananger;
import com.stu.zdy.weather.net.JsonDataAnalysisByBaidu;
import com.stu.zdy.weather.ui.MainActivity;
import com.stu.zdy.weather.util.AppWidgetUtils;
import com.stu.zdy.weather.util.ApplicationUtils;
import com.stu.zdy.weather.util.CalendarUtil;
import com.stu.zdy.weather.view.MyBaseAppWidgetProvider;
import com.stu.zdy.weather_sample.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class BigWeatherWidget extends MyBaseAppWidgetProvider {

    private Timer timer = null;
    private TimerTask task = null;

    @Override
    protected void opTimerTask(final Context context) {
        Log.v("BigWeatherWidget", "opTimerTask");
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (task != null) {
                task.cancel();
                task = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                updateTimeView(context);
            }
        };
        task.run();
        Calendar nextMinute = Calendar.getInstance();
        nextMinute.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE) + 1);
        nextMinute.set(Calendar.SECOND, 0);
        timer.schedule(task, nextMinute.getTime(), 60000);
        super.opTimerTask(context);
    }

    /**
     * 收到广播后执行
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.v("BigWeatherWidget", "onReceive");
        Log.d("action", intent.getAction());
        switch (intent.getAction()) {
            case AppWidgetUtils.BroadCast_ReFresh_Weather://更新天气
                try {
                    updateWeatherView(context, new JSONObject(intent.getStringExtra("result")));
                    opTimerTask(context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case AppWidgetUtils.BroadCast_ReFresh_Time://更新时钟
                break;
            case AppWidgetUtils.WAKE://唤醒
                widgetOnClick(context);
                updateTimeView(context);
                ApplicationUtils.runService(context);
                break;
        }
    }


    @Override
    public void onEnabled(Context context) {
        Log.v("BigWeatherWidget", "onEnabled");
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.v("BigWeatherWidget", "onUpdate");
        widgetOnClick(context);
        ApplicationUtils.runService(context);
        context.sendBroadcast(new Intent(AppWidgetUtils.BroadCast_ReFresh_Notify_Service));
        opTimerTask(context);
    }


    private void updateTimeView(Context context) {
        Log.v("BigWeatherWidget", "updateTimeView");
        Calendar calendar = Calendar.getInstance();
        String string = String.valueOf(calendar.get(Calendar.MINUTE));
        if (calendar.get(Calendar.MINUTE) < 10) {
            string = "0" + String.valueOf(calendar.get(Calendar.MINUTE));
        }
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_big);
        views.setTextViewText(R.id.time, String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + string);

        String[] weeks = context.getResources().getStringArray(R.array.week);
        views.setTextViewText(R.id.date,
                String.valueOf(calendar.get(Calendar.MONTH) + 1) + context.getResources().getString(R.string.month)
                        + String.valueOf(
                        calendar.get(Calendar.DAY_OF_MONTH) + context.getResources().getString(R.string.day)
                                + "   " + weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]));
        CalendarUtil calendarUtil = new CalendarUtil();
        views.setTextViewText(R.id.date_ch, context.getResources().getString(R.string.lunar) + "   " + calendarUtil.getChineseMonth(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)) + calendarUtil.getChineseDay(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
        ComponentName thisWidget = new ComponentName(context, BigWeatherWidget.class);
        AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, views);
    }


    private void widgetOnClick(Context context) {
        Log.v("BigWeatherWidget", "widgetOnClick");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_big);
        Intent intent = new Intent(context, MainActivity.class);//点击进入主程序
        PendingIntent weatherPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.weather_ic_big, weatherPendingIntent);

        try {//点击进入第三方程序
            Intent clockIntent = new Intent(Intent.ACTION_MAIN);
            clockIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            String ClockPackageName = SharePreferenceMananger.getSharePreferenceFromString(context, "weather_info", "clockPackageName");
            ComponentName cn = new ComponentName(ClockPackageName,
                    ApplicationUtils.doStartApplicationWithPackageName(context, ClockPackageName));
            clockIntent.setComponent(cn);
            clockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent clockPendingIntent = PendingIntent.getActivity(context, 0, clockIntent, 0);
            views.setOnClickPendingIntent(R.id.time, clockPendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //点击刷新数据
        Intent refreshIntent = new Intent().setAction(AppWidgetUtils.BroadCast_ReFresh_Notify_Service);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, 0);
        views.setOnClickPendingIntent(R.id.fresh_button, refreshPendingIntent);

        ComponentName thisWidget = new ComponentName(context, BigWeatherWidget.class);
        AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, views);
    }

    @Override
    protected void updateWeatherView(Context context, JSONObject jsonObject) throws JSONException {
        Log.v("BigWeatherWidget", "updateWeatherView");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_big);
        Bundle bundle = new JsonDataAnalysisByBaidu(jsonObject.toString()).getBundle();
        if (!"ok".equals(bundle.getString("status"))) {
            Toast.makeText(context, context.getResources().getString(R.string.sever_error), Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        try {
            views.setTextViewText(R.id.city_big, bundle.getStringArrayList("item1").get(0));
            views.setTextViewText(R.id.temper_big,
                    bundle.getStringArrayList("item1").get(6) + context.getResources().getString(R.string.degree));
            views.setTextViewText(R.id.weather_big, bundle.getStringArrayList("item1").get(3));
            views.setTextViewText(R.id.fresh_big, bundle.getStringArrayList("item1").get(2) + context.getString(R.string.refresh));
            changeWidgetPicture(context, bundle.getStringArrayList("item1").get(7));
        } catch (NullPointerException e) {
            Toast.makeText(context, context.getResources().getString(R.string.sever_error), Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        if (SharePreferenceMananger.getSharePreferenceFromBoolean(context, "weather_info", "widget_mask"))
            views.setViewVisibility(R.id.big_widget_mask, View.VISIBLE);
        else views.setViewVisibility(R.id.big_widget_mask, View.INVISIBLE);

        ComponentName thisWidget = new ComponentName(context, BigWeatherWidget.class);
        AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, views);
        super.updateWeatherView(context, jsonObject);
    }

    private void changeWidgetPicture(final Context context, String code) {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_big);
        Glide.with(context).load(MyApplication.WEATHER_ICON_URL + code + ".png").asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                views.setImageViewBitmap(R.id.weather_ic_big, resource);
                ComponentName thisWidget = new ComponentName(context, BigWeatherWidget.class);
                AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, views);
            }
        });

    }

}
