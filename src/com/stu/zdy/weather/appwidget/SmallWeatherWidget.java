package com.stu.zdy.weather.appwidget;

import net.sf.json.JSONObject;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import com.stu.zdy.weather.activity.MainActivity;
import com.stu.zdy.weather.db.DBmanager;
import com.stu.zdy.weather.net.GetInternetInfo;
import com.stu.zdy.weather.service.WidgetService;
import com.stu.zdy.weather_sample.R;

public class SmallWeatherWidget extends AppWidgetProvider {
	private static final String PackageName = "com.stu.zdy.weather.small";

	private Context mContext;
	private String cityName = "";

	@Override
	public void onUpdate(final Context context,
			AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		mContext = context;
		runService();
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"citys", Context.MODE_PRIVATE);
		cityName = sharedPreferences.getString("cityName", "北京");
		Log.v("当前城市为", cityName);
		if (GetInternetInfo.getConnectedType(context) != -1) {
			GetInfomationFromNetInSmallWidget getInfomationFromNetInSmallWidget = new GetInfomationFromNetInSmallWidget();
			getInfomationFromNetInSmallWidget.execute(cityName);
		}
		Log.v("执行了update方法", "执行了update方法");
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		mContext = context;
		runService();
		Log.d("执行了receive方法", "执行了receive方法");
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"citys", Context.MODE_PRIVATE);
		cityName = sharedPreferences.getString("cityName", "");
		String action = intent.getAction();
		Log.d("getaction", action);
		if (PackageName.equals(action)) {
			Log.v("收到一条广播", "收到一条广播");
			if (GetInternetInfo.getConnectedType(context) != -1) {
				GetInfomationFromNetInSmallWidget getInfomationFromNetInSmallWidget = new GetInfomationFromNetInSmallWidget();
				getInfomationFromNetInSmallWidget.execute(cityName);
			}
		}
		super.onReceive(context, intent);
	}

	@Override
	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
		super.onDisabled(context);
		Log.v("onDisabled", "onDisabled");
	}

	@Override
	public void onEnabled(Context context) {
		// TODO Auto-generated method stub
		Log.v("onEnabled", "onEnabled");
		this.mContext = context;
		runService();
		super.onEnabled(context);
	}

	private void runService() {
		if (!isMyServiceRunning(mContext)) {
			Log.v("服务未启动", "正在启动服务");
			Intent intent = new Intent(mContext, WidgetService.class);
			mContext.startService(intent);
		}
	}

	private boolean isMyServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.stu.zdy.weather.service.WidgetService"
					.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private void bildview(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		RemoteViews views = new RemoteViews(mContext.getPackageName(),
				R.layout.smallwidget);
		Intent intent = new Intent(mContext, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
				intent, 0);
		views.setOnClickPendingIntent(R.id.small_root, pendingIntent);
		views.setTextViewText(
				R.id.city,
				jsonObject.getJSONArray("HeWeather data service 3.0")
						.getJSONObject(0).getJSONObject("basic")
						.getString("city"));
		views.setTextViewText(R.id.temper,
				jsonObject.getJSONArray("HeWeather data service 3.0")
						.getJSONObject(0).getJSONObject("now").getString("tmp")
						+ "°");
		views.setTextViewText(
				R.id.weather,
				jsonObject.getJSONArray("HeWeather data service 3.0")
						.getJSONObject(0).getJSONObject("now")
						.getJSONObject("cond").getString("txt"));
		views.setTextViewText(
				R.id.fresh,
				jsonObject.getJSONArray("HeWeather data service 3.0")
						.getJSONObject(0).getJSONObject("basic")
						.getJSONObject("update").getString("loc")
						.substring(11, 16)
						+ "更新");
		switch (Integer.valueOf(jsonObject
				.getJSONArray("HeWeather data service 3.0").getJSONObject(0)
				.getJSONObject("now").getJSONObject("cond").getString("code"))) {
		case 100:
		case 102:
		case 103:
			views.setImageViewResource(R.id.weather_ic, R.drawable.sunny_pencil);
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
			views.setImageViewResource(R.id.weather_ic, R.drawable.storm_pencil);
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
			views.setImageViewResource(R.id.weather_ic, R.drawable.rain_pencil);
			break;
		case 400:
		case 401:
		case 402:
		case 403:
		case 404:
		case 405:
		case 406:
		case 407:
			views.setImageViewResource(R.id.weather_ic, R.drawable.snow_pencil);
			break;
		}
		ComponentName thisWidget = new ComponentName(mContext,
				SmallWeatherWidget.class);
		AppWidgetManager.getInstance(mContext).updateAppWidget(thisWidget,
				views);
	}

	class GetInfomationFromNetInSmallWidget extends
			AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			Log.d("widget+doInBackground", "widget+doInBackground");
			DBmanager dbHelper = new DBmanager(mContext);
			dbHelper.openDatabase();
			dbHelper.closeDatabase();
			String httpUrl = "https://api.heweather.com/x3/weather?cityid="
					+ dbHelper.getIdByCityName(params[0])
					+ "&key=57efa20515e94db68ae042319463dba4";
			String jsonResult = GetInternetInfo.request(httpUrl);
			return jsonResult;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			JSONObject jsonObject = JSONObject.fromObject(result);
			super.onPostExecute(result);
			bildview(jsonObject);
		}

	}

}
