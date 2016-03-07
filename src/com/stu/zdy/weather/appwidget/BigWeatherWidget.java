package com.stu.zdy.weather.appwidget;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.stu.zdy.weather.ui.MainActivity;
import com.stu.zdy.weather.app.MyApplication;
import com.stu.zdy.weather.interfaces.WeatherCallBack;
import com.stu.zdy.weather.mananger.SharePreferenceMananger;
import com.stu.zdy.weather.net.JsonDataAnalysisByBaidu;
import com.stu.zdy.weather.util.ApplicationUtils;
import com.stu.zdy.weather.util.CalendarUtil;
import com.stu.zdy.weather.util.NetWorkUtils;
import com.stu.zdy.weather.util.OkHttpUtils;
import com.stu.zdy.weather_sample.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class BigWeatherWidget extends AppWidgetProvider {
	private static String ClockPackageName = null;
	private String cityName = null;

	private Context mContext = null;
	private String[] weeks = null;
	private RemoteViews views = null;

	private Timer timer = null;
	private TimerTask task = null;

	private void initData(Context context) {
		mContext = context.getApplicationContext();
		if (weeks == null) {
			weeks = context.getResources().getStringArray(R.array.week);
		}
		if (views == null) {
			views = new RemoteViews(mContext.getPackageName(), R.layout.bigwidget);
		}
		cityName = SharePreferenceMananger.getSharePreferenceFromString(mContext, "weather_info", "currentCity");
		ClockPackageName = SharePreferenceMananger.getSharePreferenceFromString(mContext, "weather_info",
				"packagename");
	}

	private void initView(Context context) {
		runTimerTask(1);
		prepareHttpRequest();
		widgetOnClick(context);
	}

	private void runTimerTask(int type) {
		Log.v("BigWeatherWidget", "runTimerTask");
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
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		Log.v("BigWeatherWidget", "onReceive");
		initData(context);
		initView(context);

		ApplicationUtils.runService(mContext);
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		Log.v("BigWeatherWidget", "onDisabled");
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		Log.v("BigWeatherWidget", "onDeleted");
		runTimerTask(0);
	}

	private void prepareHttpRequest() {
		Log.v("BigWeatherWidget", "prepareHttpRequest");
		if (NetWorkUtils.getConnectedType(mContext) != -1) {
			OkHttpUtils okHttpUtils = new OkHttpUtils(new WeatherCallBack() {

				@Override
				public void onUpdate(String result) {
					// TODO Auto-generated method stub
					try {
						updateWeatherView(new JSONObject(result));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			okHttpUtils.run(new Handler(), cityName);
		}
	}

	/**
	 * @param context
	 * @param views
	 * 
	 * 
	 */
	private void updateTimeView(Context context) {
		Log.v("BigWeatherWidget", "updateTimeView");
		Calendar calendar = Calendar.getInstance();
		String string = String.valueOf(calendar.get(Calendar.MINUTE));
		if (calendar.get(Calendar.MINUTE) < 10) {
			string = "0" + String.valueOf(calendar.get(Calendar.MINUTE));
		}
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
	 * 
	 * 
	 * @param context
	 * @param views
	 */
	private void widgetOnClick(Context context) {
		Log.v("BigWeatherWidget", "widgetOnClick");
		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.weather_ic_big, pendingIntent);
		try {
			Intent clockIntent = new Intent(Intent.ACTION_MAIN);
			clockIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			ComponentName cn = new ComponentName(ClockPackageName,
					ApplicationUtils.doStartApplicationWithPackageName(context, ClockPackageName));
			clockIntent.setComponent(cn);
			pendingIntent = PendingIntent.getActivity(context, 0, clockIntent, 0);
			views.setOnClickPendingIntent(R.id.time, pendingIntent);
		} catch (Exception e) {
			// TODO: handle exception
		}
		Intent refreshIntent = new Intent().setAction(MyApplication.PackageNameBig);
		pendingIntent = PendingIntent.getBroadcast(mContext, 0, refreshIntent, 0);
		views.setOnClickPendingIntent(R.id.fresh_button, pendingIntent);
		ComponentName thisWidget = new ComponentName(mContext, BigWeatherWidget.class);
		AppWidgetManager.getInstance(mContext).updateAppWidget(thisWidget, views);
	}

	private void updateWeatherView(JSONObject jsonObject) {
		Log.v("BigWeatherWidget", "updateWeatherView");
		Bundle bundle = new JsonDataAnalysisByBaidu(jsonObject.toString()).getBundle();
		if (!"ok".equals(bundle.getString("status"))) {
			Toast.makeText(mContext, mContext.getResources().getString(R.string.sever_error), Toast.LENGTH_SHORT)
					.show();
			return;
		}
		views.setTextViewText(R.id.city_big, bundle.getStringArrayList("item1").get(0));
		views.setTextViewText(R.id.temper_big,
				bundle.getStringArrayList("item1").get(6) + mContext.getResources().getString(R.string.degree));
		views.setTextViewText(R.id.weather_big, bundle.getStringArrayList("item1").get(3));
		views.setTextViewText(R.id.fresh_big, bundle.getStringArrayList("item1").get(2));

		changeWidgetPicture(bundle.getStringArrayList("item1").get(7));
		ComponentName thisWidget = new ComponentName(mContext, BigWeatherWidget.class);
		AppWidgetManager.getInstance(mContext).updateAppWidget(thisWidget, views);
	}

	private void changeWidgetPicture(String code) {
		Log.v("BigWeatherWidget", "changeWidgetPicture");
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
