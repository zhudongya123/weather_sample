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
import com.stu.zdy.weather.util.AppWidgetUtils;
import com.stu.zdy.weather.util.ApplicationUtils;
import com.stu.zdy.weather.util.CalendarUtil;
import com.stu.zdy.weather.util.NetWorkUtils;
import com.stu.zdy.weather.retrofit.OkHttpUtils;
import com.stu.zdy.weather.view.MyBaseAppWidgetProvider;
import com.stu.zdy.weather_sample.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class BigWeatherWidget extends MyBaseAppWidgetProvider {
    private static String ClockPackageName = null;
    private String cityName = "";

    private Context mContext = null;
    private String[] weeks = null;

    private Timer timer = null;
    private TimerTask task = null;

    private void initData(Context context) {
        if (weeks == null) {
            weeks = context.getResources().getStringArray(R.array.week);
        }

        cityName = SharePreferenceMananger.getSharePreferenceFromString(mContext, "weather_info", "currentCity");
        ClockPackageName = SharePreferenceMananger.getSharePreferenceFromString(mContext, "weather_info",
                "clockPackageName");
    }

    @Override
    protected void opTimerTask() {
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
        } catch (Error e) {
            e.printStackTrace();
        }
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                updateTimeView(mContext);
            }
        };
        task.run();
        Calendar nextMinute = Calendar.getInstance();
        nextMinute.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE) + 1);
        nextMinute.set(Calendar.SECOND, 0);
        timer.schedule(task, nextMinute.getTime(), 60000);
    }

    /**
     * 收到广播后执行
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        super.onReceive(context, intent);
        Log.v("BigWeatherWidget", "onReceive");
        String action = intent.getAction();
        Log.d("action", intent.getAction());
        initData(context);
        updateTimeView(mContext);
        switch (action) {
            case AppWidgetUtils.UPDATE:
                widgetOnClick(context);
                prepareHttpRequest();
                break;
            case AppWidgetUtils.PackageNameBig:
                prepareHttpRequest();
                break;
            case AppWidgetUtils.WAKE:
                widgetOnClick(context);
        }
        ApplicationUtils.runService(mContext);
    }


    private void prepareHttpRequest() {
        Log.v("BigWeatherWidget", "prepareHttpRequest");
        if (cityName.equals("")) return;
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

    /**
     * @param context
     */
    private void updateTimeView(Context context) {
        Log.v("BigWeatherWidget", "updateTimeView");
        Calendar calendar = Calendar.getInstance();
        String string = String.valueOf(calendar.get(Calendar.MINUTE));
        if (calendar.get(Calendar.MINUTE) < 10) {
            string = "0" + String.valueOf(calendar.get(Calendar.MINUTE));
        }
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_big);

        views.setTextViewText(R.id.time, String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + string);
        views.setTextViewText(R.id.date,
                String.valueOf(calendar.get(Calendar.MONTH) + 1) + context.getResources().getString(R.string.month)
                        + String.valueOf(
                        calendar.get(Calendar.DAY_OF_MONTH) + context.getResources().getString(R.string.day)
                                + "   " + weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]));
        CalendarUtil calendarUtil = new CalendarUtil();
        views.setTextViewText(R.id.date_ch,
                context.getResources().getString(R.string.lunar) + "   "
                        + calendarUtil.getChineseMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH))
                        + calendarUtil.getChineseDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH)));
        ComponentName thisWidget = new ComponentName(mContext, BigWeatherWidget.class);
        AppWidgetManager.getInstance(mContext).updateAppWidget(thisWidget, views);
    }

    /**
     * @param context
     */
    private void widgetOnClick(Context context) {
        Log.v("BigWeatherWidget", "widgetOnClick");
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_big);
        Intent intent = new Intent(context, MainActivity.class);//点击进入主程序
        PendingIntent weatherPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.weather_ic_big, weatherPendingIntent);

        try {//点击进入第三方程序
            Intent clockIntent = new Intent(Intent.ACTION_MAIN);
            clockIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName(ClockPackageName,
                    ApplicationUtils.doStartApplicationWithPackageName(context, ClockPackageName));
            clockIntent.setComponent(cn);
            clockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent clockPendingIntent = PendingIntent.getActivity(context, 0, clockIntent, 0);
            views.setOnClickPendingIntent(R.id.time, clockPendingIntent);
        } catch (Exception e) {
        }

        //点击刷新数据
        Intent refreshIntent = new Intent().setAction(AppWidgetUtils.PackageNameBig);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(mContext, 0, refreshIntent, 0);
        views.setOnClickPendingIntent(R.id.fresh_button, refreshPendingIntent);

        if (SharePreferenceMananger.getSharePreferenceFromBoolean(mContext, "weather_info", "widget_mask"))
            views.setViewVisibility(R.id.big_widget_mask, View.VISIBLE);
        else views.setViewVisibility(R.id.big_widget_mask, View.INVISIBLE);

        ComponentName thisWidget = new ComponentName(mContext, BigWeatherWidget.class);
        AppWidgetManager.getInstance(mContext).updateAppWidget(thisWidget, views);
    }

    @Override
    protected void updateWeatherView(JSONObject jsonObject) throws JSONException {
        Log.v("BigWeatherWidget", "updateWeatherView");
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_big);
        Bundle bundle = new JsonDataAnalysisByBaidu(jsonObject.toString()).getBundle();
        if (!"ok".equals(bundle.getString("status"))) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.sever_error), Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        try {
            views.setTextViewText(R.id.city_big, bundle.getStringArrayList("item1").get(0));
            views.setTextViewText(R.id.temper_big,
                    bundle.getStringArrayList("item1").get(6) + mContext.getResources().getString(R.string.degree));
            views.setTextViewText(R.id.weather_big, bundle.getStringArrayList("item1").get(3));
            views.setTextViewText(R.id.fresh_big, bundle.getStringArrayList("item1").get(2) + mContext.getString(R.string.refresh));
            changeWidgetPicture(bundle.getStringArrayList("item1").get(7));
        } catch (NullPointerException e) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.sever_error), Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        ComponentName thisWidget = new ComponentName(mContext, BigWeatherWidget.class);
        AppWidgetManager.getInstance(mContext).updateAppWidget(thisWidget, views);
        super.updateWeatherView(jsonObject);
    }

    private void changeWidgetPicture(String code) {
        Log.v("BigWeatherWidget", "changeWidgetPicture");
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_big);
        switch (Integer.valueOf(code)) {
            case 100:
            case 102:
            case 103:
                views.setImageViewResource(R.id.weather_ic_big, R.drawable.sunny_pencil);
                break;
            case 101:
                views.setImageViewResource(R.id.weather_ic_big, R.drawable.cloudy_pencil);
                break;
            case 104:
                views.setImageViewResource(R.id.weather_ic_big, R.drawable.overcast_pencil);
                break;
            case 302:
            case 303:
            case 304:
                views.setImageViewResource(R.id.weather_ic_big, R.drawable.storm_pencil);
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
                views.setImageViewResource(R.id.weather_ic_big, R.drawable.rain_pencil);
                break;
            case 400:
            case 401:
            case 402:
            case 403:
            case 404:
            case 405:
            case 406:
            case 407:
                views.setImageViewResource(R.id.weather_ic_big, R.drawable.snow_pencil);
                break;
            default:
                break;
        }
        ComponentName thisWidget = new ComponentName(mContext, BigWeatherWidget.class);
        AppWidgetManager.getInstance(mContext).updateAppWidget(thisWidget, views);
    }

}
