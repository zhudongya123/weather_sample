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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Zdy on 2016/3/26.
 */
public class HugeWeatherWidget extends MyBaseAppWidgetProvider {

    private int[][] ids = new int[][]{{R.id.huge_future_weather_day1_date, R.id.huge_future_weather_day1_icon, R.id.huge_future_weather_day1_weather,
            R.id.huge_future_weather_day1_temper, R.id.huge_future_weather_day1_high_change, R.id.huge_future_weather_day1_low_change}
            , {R.id.huge_future_weather_day2_date, R.id.huge_future_weather_day2_icon, R.id.huge_future_weather_day2_weather,
            R.id.huge_future_weather_day2_temper, R.id.huge_future_weather_day2_high_change, R.id.huge_future_weather_day2_low_change},
            {R.id.huge_future_weather_day3_date, R.id.huge_future_weather_day3_icon, R.id.huge_future_weather_day3_weather,
                    R.id.huge_future_weather_day3_temper, R.id.huge_future_weather_day3_high_change, R.id.huge_future_weather_day3_low_change}};

    private int[] emojis = new int[]{R.id.huge_weather_icon_emoji_1, R.id.huge_weather_icon_emoji_2, R.id.huge_weather_icon_emoji_3};
    private Timer timer = null;
    private TimerTask task = null;


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.v("HugeWeatherWidget", "onReceive");
        String action = intent.getAction();
        switch (action) {
            case AppWidgetUtils.BroadCast_ReFresh_Weather:
                try {
                    updateWeatherView(context, new JSONObject(intent.getStringExtra("result")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                opTimerTask(context);
                break;
            case AppWidgetUtils.BroadCast_ReFresh_Time:
                break;
            case AppWidgetUtils.WAKE://唤醒
                updateTimeView(context);
                widgetOnClick(context);
                ApplicationUtils.runService(context);
                break;
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v("HugeWeatherWidget", "onUpdate");
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        updateTimeView(context);
        widgetOnClick(context);
        ApplicationUtils.runService(context);
        context.sendBroadcast(new Intent(AppWidgetUtils.BroadCast_ReFresh_Notify_Service));
        opTimerTask(context);
    }

    @Override
    public void onEnabled(Context context) {
        Log.v("HugeWeatherWidget", "onEnabled");
        super.onEnabled(context);
    }


    @Override
    protected void opTimerTask(final Context context) {
        Log.v("HugeWeatherWidget", "opTimerTask");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
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
        updateTimeView(context);
    }

    protected void updateWeatherView(Context context, JSONObject jsonObject) throws JSONException {
        Bundle bundle = new JsonDataAnalysisByBaidu(jsonObject.toString()).getBundle();
        if (!"ok".equals(bundle.getString("status"))) {
            Toast.makeText(context, context.getResources().getString(R.string.sever_error), Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        RemoteViews rootView = new RemoteViews(context.getPackageName(), R.layout.widget_huge);
        try {
            rootView.setTextViewText(R.id.huge_city, bundle.getStringArrayList("item1").get(0));
            rootView.setTextViewText(R.id.huge_temper,
                    bundle.getStringArrayList("item1").get(6) + context.getResources().getString(R.string.degree));
            rootView.setTextViewText(R.id.huge_weather, bundle.getStringArrayList("item1").get(3));
            rootView.setTextViewText(R.id.huge_refresh, bundle.getStringArrayList("item1").get(2) + context.getString(R.string.refresh));
            changeWidgetPicture(context, bundle.getStringArrayList("item1").get(7), R.id.huge_weather_icon);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 3; i++) {
            ArrayList<String> array = bundle.getStringArrayList("item2");
            rootView.setTextViewText(ids[i][2], array.get(15 + i * 2) + (array.get(17 + i * 2).equals(array.get(17 + i * 2 + 1)) ? "" : "~" + array.get(17 + i * 2 + 1)));

            rootView.setTextViewText(ids[i][3], array.get(i + 1) + context.getString(R.string.degree) +
                    "~" + array.get(i + 6) + context.getString(R.string.degree));
            rootView.setTextViewText(ids[i][4], context.getString(R.string.high_temper) + "   " + convertStringResult(array.get(i + 5), array.get(6 + i)) + context.getString(R.string.degree));
            rootView.setTextViewText(ids[i][5], context.getString(R.string.low_temper) + "   " + convertStringResult(array.get(i), array.get(1 + i)) + context.getString(R.string.degree));
            changeWidgetPicture(context, array.get(i + 10), ids[i][1]);
            int result = Math.abs(Integer.valueOf(array.get(i + 5)) - Integer.valueOf(array.get(6 + i))) + Math.abs(Integer.valueOf(array.get(i)) - Integer.valueOf(array.get(1 + i)));
            changeWidgetEmoji(rootView, emojis[i], result);
        }

        if (SharePreferenceMananger.getSharePreferenceFromBoolean(context, "weather_info", "widget_mask"))
            rootView.setViewVisibility(R.id.huge_widget_mask, View.VISIBLE);
        else rootView.setViewVisibility(R.id.huge_widget_mask, View.INVISIBLE);

        ComponentName thisWidget = new ComponentName(context, HugeWeatherWidget.class);
        AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, rootView);
    }

    private void changeWidgetEmoji(RemoteViews rootView, int emoji, int result) {
        if (result < 6)
            rootView.setImageViewResource(emoji, R.drawable.temper_emoji_2);
        else if (result < 8 && result >= 6)
            rootView.setImageViewResource(emoji, R.drawable.temper_emoji_3);
        else if (result < 11 && result >= 8)
            rootView.setImageViewResource(emoji, R.drawable.temper_emoji_5);
        else if (result < 14 && result >= 11)
            rootView.setImageViewResource(emoji, R.drawable.temper_emoji_1);
        else if (result > 14)
            rootView.setImageViewResource(emoji, R.drawable.temper_emoji_6);
    }


    private void updateTimeView(Context context) {
        Log.v("HugeWeatherWidget", "updateTimeView");
        Calendar calendar = Calendar.getInstance();
        String string = String.valueOf(calendar.get(Calendar.MINUTE));
        if (calendar.get(Calendar.MINUTE) < 10) {
            string = "0" + String.valueOf(calendar.get(Calendar.MINUTE));
        }
        RemoteViews rootView = new RemoteViews(context.getPackageName(), R.layout.widget_huge);
        rootView.setTextViewText(R.id.huge_time, String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + string);
        String[] weeks = context.getResources().getStringArray(R.array.week);
        rootView.setTextViewText(R.id.huge_date,
                String.valueOf(calendar.get(Calendar.MONTH) + 1) + context.getResources().getString(R.string.month)
                        + String.valueOf(
                        calendar.get(Calendar.DAY_OF_MONTH) + context.getResources().getString(R.string.day)
                                + " " + weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]));
        for (int i = 0; i < 3; i++) {
            if (CalendarUtil.monthDays[calendar.get(Calendar.MONTH)] < calendar.get(Calendar.DAY_OF_MONTH) + i + 1) {
                rootView.setTextViewText(ids[i][0], (calendar.get(Calendar.MONTH) + 2) + context.getString(R.string.month) +
                        (calendar.get(Calendar.DAY_OF_MONTH) + 1 + i - CalendarUtil.monthDays[calendar.get(Calendar.MONTH)]) + context.getString(R.string.day));
            } else {
                rootView.setTextViewText(ids[i][0], (calendar.get(Calendar.MONTH) + 1) + context.getString(R.string.month) +
                        (calendar.get(Calendar.DAY_OF_MONTH) + 1 + i) + context.getString(R.string.day));
            }
        }
        CalendarUtil calendarUtil = new CalendarUtil();
        rootView.setTextViewText(R.id.huge_ch,
                context.getResources().getString(R.string.lunar) + "   "
                        + calendarUtil.getChineseMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH))
                        + calendarUtil.getChineseDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH)));
        ComponentName thisWidget = new ComponentName(context, HugeWeatherWidget.class);
        AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, rootView);
    }

    private void widgetOnClick(Context context) {
        Log.v("HugeWeatherWidget", "widgetOnClick");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_huge);
        Intent intent = new Intent(context, MainActivity.class);//点击进入主程序
        PendingIntent weatherPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.huge_weather_icon, weatherPendingIntent);

        try {//点击进入第三方程序
            Intent clockIntent = new Intent(Intent.ACTION_MAIN);
            clockIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            String clockPackageName = SharePreferenceMananger.getSharePreferenceFromString(context, "weather_info",
                    "clockPackageName");
            ComponentName cn = new ComponentName(clockPackageName,
                    ApplicationUtils.doStartApplicationWithPackageName(context, clockPackageName));
            clockIntent.setComponent(cn);
            clockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent clockPendingIntent = PendingIntent.getActivity(context, 0, clockIntent, 0);
            views.setOnClickPendingIntent(R.id.huge_time, clockPendingIntent);
        } catch (Exception e) {
        }

        //点击刷新数据
        Intent refreshIntent = new Intent().setAction(AppWidgetUtils.BroadCast_ReFresh_Notify_Service);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, 0);
        views.setOnClickPendingIntent(R.id.huge_refresh_button, refreshPendingIntent);


        ComponentName thisWidget = new ComponentName(context, HugeWeatherWidget.class);
        AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, views);
    }


    private void changeWidgetPicture(final Context context, String code, final int id) {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_huge);
        Glide.with(context).load(MyApplication.WEATHER_ICON_URL + code + ".png").asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                views.setImageViewBitmap(id, resource);
                ComponentName thisWidget = new ComponentName(context, HugeWeatherWidget.class);
                AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, views);
            }
        });


    }

    private String convertStringResult(String a, String b) {
        int result = Integer.valueOf(b) - Integer.valueOf(a);
        if (result > 0) {
            return "+" + result;
        }
        return result + "";
    }
}
