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
import com.stu.zdy.weather.util.OkHttpUtils;
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

    private static String ClockPackageName = null;
    private String cityName = "";

    private Context mContext = null;
    private String[] weeks = null;

    private Timer timer = null;
    private TimerTask task = null;
    private int[][] ids;


    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        super.onReceive(context, intent);
        Log.v("HugeWeatherWidget", "onReceive");
        String action = intent.getAction();
        Log.v("action", action);
        initData(mContext);
        switch (action) {
            case AppWidgetUtils.UPDATE:
                opTimerTask(1);
                widgetOnClick(context);
                prepareHttpRequest();
                break;
            case AppWidgetUtils.PackageNameHuge:
                prepareHttpRequest();
                updateTimeView(mContext);
                break;
        }
    }

    private void initData(Context context) {
        Log.v("HugeWeatherWidget", "initData");
        if (weeks == null) {
            weeks = context.getResources().getStringArray(R.array.week);
        }
        cityName = SharePreferenceMananger.getSharePreferenceFromString(mContext, "weather_info", "currentCity");
        ClockPackageName = SharePreferenceMananger.getSharePreferenceFromString(mContext, "weather_info",
                "clockPackageName");
        if (ids == null) {
            ids = new int[][]{{R.id.huge_future_weather_day1_date, R.id.huge_future_weather_day1_icon, R.id.huge_future_weather_day1_weather,
                    R.id.huge_future_weather_day1_temper, R.id.huge_future_weather_day1_high_change, R.id.huge_future_weather_day1_low_change}
                    , {R.id.huge_future_weather_day2_date, R.id.huge_future_weather_day2_icon, R.id.huge_future_weather_day2_weather,
                    R.id.huge_future_weather_day2_temper, R.id.huge_future_weather_day2_high_change, R.id.huge_future_weather_day2_low_change},
                    {R.id.huge_future_weather_day3_date, R.id.huge_future_weather_day3_icon, R.id.huge_future_weather_day3_weather,
                            R.id.huge_future_weather_day3_temper, R.id.huge_future_weather_day3_high_change, R.id.huge_future_weather_day3_low_change}};
        }
    }

    private void prepareHttpRequest() {
        Log.v("HugeWeatherWidget", "prepareHttpRequest");
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

    @Override
    protected void opTimerTask(int type) {
        Log.v("HugeWeatherWidget", "opTimerTask");
        if (type == 1) {
            timer = new Timer();
            task = new TimerTask() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    updateTimeView(mContext);
                }
            };
            task.run();
            timer.schedule(task, Calendar.getInstance().getTime(), 60000);

        } else {
            timer.cancel();
            task.cancel();
            timer = null;
            task = null;
        }
    }

    @Override
    protected void updateWeatherView(JSONObject jsonObject) throws JSONException {
        Bundle bundle = new JsonDataAnalysisByBaidu(jsonObject.toString()).getBundle();
        if (!"ok".equals(bundle.getString("status"))) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.sever_error), Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        RemoteViews rootView = new RemoteViews(mContext.getPackageName(), R.layout.widget_huge);
        try {
            rootView.setTextViewText(R.id.huge_city, bundle.getStringArrayList("item1").get(0));
            rootView.setTextViewText(R.id.huge_temper,
                    bundle.getStringArrayList("item1").get(6) + mContext.getResources().getString(R.string.degree));
            rootView.setTextViewText(R.id.huge_weather, bundle.getStringArrayList("item1").get(3));
            rootView.setTextViewText(R.id.huge_refresh, bundle.getStringArrayList("item1").get(2) + mContext.getString(R.string.refresh));
            changeWidgetPicture(bundle.getStringArrayList("item1").get(7), R.id.huge_weather_icon);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 3; i++) {
            ArrayList<String> array = bundle.getStringArrayList("item2");
            rootView.setTextViewText(ids[i][2], array.get(15 + i * 2) + (array.get(17 + i * 2).equals(array.get(17 + i * 2 + 1)) ? "" : "转" + array.get(17 + i * 2 + 1)));

            rootView.setTextViewText(ids[i][3], array.get(i + 1) + mContext.getString(R.string.degree) +
                    "~" + array.get(i + 6) + mContext.getString(R.string.degree));
            rootView.setTextViewText(ids[i][4], mContext.getString(R.string.high_temper) + "   " + convertStringResult(array.get(i + 5), array.get(6 + i)) + mContext.getString(R.string.degree));
            rootView.setTextViewText(ids[i][5], mContext.getString(R.string.low_temper) + "   " + convertStringResult(array.get(i), array.get(1 + i)) + mContext.getString(R.string.degree));
            changeWidgetPicture(array.get(i + 10), ids[i][1]);
        }
        ComponentName thisWidget = new ComponentName(mContext, HugeWeatherWidget.class);
        AppWidgetManager.getInstance(mContext).updateAppWidget(thisWidget, rootView);
        super.updateWeatherView(jsonObject);
    }


    private void updateTimeView(Context mContext) {
        Log.v("HugeWeatherWidget", "updateTimeView");
        Calendar calendar = Calendar.getInstance();
        String string = String.valueOf(calendar.get(Calendar.MINUTE));
        if (calendar.get(Calendar.MINUTE) < 10) {
            string = "0" + String.valueOf(calendar.get(Calendar.MINUTE));
        }
        RemoteViews rootView = new RemoteViews(mContext.getPackageName(), R.layout.widget_huge);
        rootView.setTextViewText(R.id.huge_time, String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + string);
        rootView.setTextViewText(R.id.huge_date,
                String.valueOf(calendar.get(Calendar.MONTH) + 1) + mContext.getResources().getString(R.string.month)
                        + String.valueOf(
                        calendar.get(Calendar.DAY_OF_MONTH) + mContext.getResources().getString(R.string.day)
                                + "   " + weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]));
        for (int i = 0; i < 3; i++) {
            if (CalendarUtil.monthDays[calendar.get(Calendar.MONTH)] < calendar.get(Calendar.DAY_OF_MONTH) + i + 1) {
                rootView.setTextViewText(ids[i][0], (calendar.get(Calendar.MONTH) + 2) + mContext.getString(R.string.month) +
                        (calendar.get(Calendar.DAY_OF_MONTH) + 1 + i - CalendarUtil.monthDays[calendar.get(Calendar.MONTH)]) + mContext.getString(R.string.day));
            } else {
                rootView.setTextViewText(ids[i][0], (calendar.get(Calendar.MONTH) + 1) + mContext.getString(R.string.month) +
                        (calendar.get(Calendar.DAY_OF_MONTH) + 1 + i) + mContext.getString(R.string.day));
            }
        }
        CalendarUtil calendarUtil = new CalendarUtil();
        rootView.setTextViewText(R.id.huge_ch,
                mContext.getResources().getString(R.string.lunar) + "   "
                        + calendarUtil.getChineseMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH))
                        + calendarUtil.getChineseDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH)));
        ComponentName thisWidget = new ComponentName(mContext, HugeWeatherWidget.class);
        AppWidgetManager.getInstance(mContext).updateAppWidget(thisWidget, rootView);
    }

    private void widgetOnClick(Context context) {
        Log.v("HugeWeatherWidget", "widgetOnClick");
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_huge);
        Intent intent = new Intent(context, MainActivity.class);//点击进入主程序
        PendingIntent weatherPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.huge_weather_icon, weatherPendingIntent);

        try {//点击进入第三方程序
            Intent clockIntent = new Intent(Intent.ACTION_MAIN);
            clockIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName(ClockPackageName,
                    ApplicationUtils.doStartApplicationWithPackageName(context, ClockPackageName));
            clockIntent.setComponent(cn);
            clockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent clockPendingIntent = PendingIntent.getActivity(context, 0, clockIntent, 0);
            views.setOnClickPendingIntent(R.id.huge_time, clockPendingIntent);
        } catch (Exception e) {
        }

        //点击刷新数据
        Intent refreshIntent = new Intent().setAction(AppWidgetUtils.PackageNameHuge);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(mContext, 0, refreshIntent, 0);
        views.setOnClickPendingIntent(R.id.huge_refresh_button, refreshPendingIntent);

        if (SharePreferenceMananger.getSharePreferenceFromBoolean(mContext, "weather_info", "widget_mask"))
            views.setViewVisibility(R.id.huge_widget_mask, View.VISIBLE);
        else views.setViewVisibility(R.id.huge_widget_mask, View.INVISIBLE);

        ComponentName thisWidget = new ComponentName(mContext, HugeWeatherWidget.class);
        AppWidgetManager.getInstance(mContext).updateAppWidget(thisWidget, views);
    }


    private void changeWidgetPicture(String code, int id) {
        Log.v("hugeWeatherWidget", "changeWidgetPicture");
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_huge);
        switch (Integer.valueOf(code)) {
            case 100:
            case 102:
            case 103:
                views.setImageViewResource(id, R.drawable.sunny_pencil);
                break;
            case 101:
                views.setImageViewResource(id, R.drawable.cloudy_pencil);
                break;
            case 104:
                views.setImageViewResource(id, R.drawable.overcast_pencil);
                break;
            case 302:
            case 303:
            case 304:
                views.setImageViewResource(id, R.drawable.storm_pencil);
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
                views.setImageViewResource(id, R.drawable.rain_pencil);
                break;
            case 400:
            case 401:
            case 402:
            case 403:
            case 404:
            case 405:
            case 406:
            case 407:
                views.setImageViewResource(id, R.drawable.snow_pencil);
                break;
            default:
                break;
        }
        ComponentName thisWidget = new ComponentName(mContext, HugeWeatherWidget.class);
        AppWidgetManager.getInstance(mContext).updateAppWidget(thisWidget, views);
    }

    private String convertStringResult(String a, String b) {
        int result = Integer.valueOf(b) - Integer.valueOf(a);
        if (result > 0) {
            return "+" + result;
        }
        return result + "";
    }
}
