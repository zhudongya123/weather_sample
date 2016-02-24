package com.stu.zdy.weather.appwidget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.stu.zdy.weather.activity.MainActivity;
import com.stu.zdy.weather.db.DBManager;
import com.stu.zdy.weather.net.JsonDataAnalysisByBaidu;
import com.stu.zdy.weather.service.WidgetService;
import com.stu.zdy.weather.util.CalendarUtil;
import com.stu.zdy.weather.util.NetWorkUtils;
import com.stu.zdy.weather_sample.R;

public class BigWeatherWidget extends AppWidgetProvider {
	private static String ClockPackageName = "com.google.android.deskclock";
	private String cityName = "";
	private ArrayList<String> arrayList = new ArrayList<String>();
	private SharedPreferences sharedPreferences;
	private Context mContext;
	private String[] weeks = { "����", "��һ", "�ܶ�", "����", "����", "����", "����" };

	@Override
	public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		this.mContext = context;
		runService();
		sharedPreferences = mContext.getSharedPreferences("citys", Context.MODE_PRIVATE);
		cityName = sharedPreferences.getString("cityName", "����");
		ClockPackageName = sharedPreferences.getString("packagename", "com.google.android.deskclock");
		if (NetWorkUtils.getConnectedType(context) != -1) {
			GetInfomationFromNetInBigWidget getInfomationFromNetInWidget = new GetInfomationFromNetInBigWidget();
			getInfomationFromNetInWidget.execute(cityName);
		}
		Log.v("onUpdate", "onUpdate");
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onEnabled(Context context) {
		// TODO Auto-generated method stub
		Log.v("onEnabled", "onEnabled");
		this.mContext = context;
		runService();
		super.onEnabled(context);
	}

	private boolean isMyServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.stu.zdy.weather.service.WidgetService".equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.mContext = context;
		runService();
		Log.v("onReceive", "onReceive");
		sharedPreferences = mContext.getSharedPreferences("citys", Context.MODE_PRIVATE);
		cityName = sharedPreferences.getString("cityName", "");
		Log.v("cityName", cityName);
		if (1 == intent.getIntExtra("index", 0)) {
			Log.v("����ˢ��", "����ˢ��");
			bildview(0, null);
		}
		if (12 == intent.getIntExtra("index", 0)) {
			Log.v("����ˢ��", "����ˢ��");
			if (NetWorkUtils.getConnectedType(context) != -1) {
				Log.v("cityname", cityName);
				Toast.makeText(mContext, "����ˢ��", Toast.LENGTH_SHORT).show();
				GetInfomationFromNetInBigWidget getInfomationFromNetInWidget = new GetInfomationFromNetInBigWidget();
				getInfomationFromNetInWidget.execute(cityName);
			}
		}
		super.onReceive(context, intent);
	}

	/**
	 * �жϷ����Ƿ�����
	 */
	private void runService() {
		// TODO Auto-generated method stub
		if (!isMyServiceRunning(mContext)) {
			Log.v("����δ����", "��������");
			Intent intent2 = new Intent(mContext, WidgetService.class);
			mContext.startService(intent2);
		}
	}

	@Override
	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
		super.onDisabled(context);
		Log.v("onDisabled", "onDisabled");
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		super.onDeleted(context, appWidgetIds);
		Log.v("onDeleted", "onDeleted");
	}

	/**
	 * @param context
	 * @param views
	 *            ˢ��ʱ��
	 * 
	 */
	private void FreshTime(Context context, RemoteViews views) {
		Calendar calendar = Calendar.getInstance();
		String string = String.valueOf(calendar.get(Calendar.MINUTE));
		if (calendar.get(Calendar.MINUTE) < 10) {
			string = "0" + String.valueOf(calendar.get(Calendar.MINUTE));
		}
		views.setTextViewText(R.id.time, String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + string);
		views.setTextViewText(R.id.date, String.valueOf(calendar.get(Calendar.MONTH) + 1) + "��" + String.valueOf(
				calendar.get(Calendar.DAY_OF_MONTH) + "��   " + weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]));
		CalendarUtil calendarUtil = new CalendarUtil();
		views.setTextViewText(R.id.date_ch,
				"ũ�� " + calendarUtil.getChineseMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
						calendar.get(Calendar.DAY_OF_MONTH))
				+ calendarUtil.getChineseDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
						calendar.get(Calendar.DAY_OF_MONTH)));
	}

	/**
	 * ����¼����� �������˹㲥
	 * 
	 * @param context
	 * @param views
	 */
	private void widgetOnClick(Context context, RemoteViews views) {
		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.weather_ic_big, pendingIntent);
		try {
			Intent intent2 = new Intent(Intent.ACTION_MAIN);
			intent2.addCategory(Intent.CATEGORY_LAUNCHER);
			ComponentName cn = new ComponentName(ClockPackageName, doStartApplicationWithPackageName(ClockPackageName));
			intent2.setComponent(cn);
			pendingIntent = PendingIntent.getActivity(context, 0, intent2, 0);
			views.setOnClickPendingIntent(R.id.time, pendingIntent);
		} catch (Exception e) {
			// TODO: handle exception
		}
		Intent intent3 = new Intent().setAction("com.stu.zdy.weather.big");
		intent3.putExtra("index", 12);
		pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent3, 0);
		views.setOnClickPendingIntent(R.id.fresh_button, pendingIntent);
	}

	/**
	 * ˢ�����ݣ� 1.����ˢ��Ϊһ����һ�Σ�ˢ��ʱ�䣬���Ӵ洢��ȡ������ˢ��ʱ��������ݡ�
	 * 2.����ˢ��Ϊ�����ť���߼�����£���ˢ��ʱ�䣬�ڴ������ȡ����ˢ�¡�
	 * 
	 * @param kind
	 * @param jsonObject
	 */
	private void bildview(int kind, JSONObject jsonObject) {
		// TODO Auto-generated method stub
		RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.bigwidget);
		sharedPreferences = mContext.getSharedPreferences("weather_info", Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		if (kind == 1) {

			// ����ˢ��
			// �߼�����Ҫ����ˢ��ʱ���ӷ�������ȡ���ݺ󣬽����������ݴ洢����ѡ��֮��
			// if֮��Ĵ�������������������������������
			// ��������ʱ��ֱ�Ӵ���ѡ���л�ȡ���ݣ�������ʱ������Ϊ�ոս����ݱ��������磬
			// �ٴ���ѡ���ȡ�����ݣ����������ͬ
			Bundle bundle = new JsonDataAnalysisByBaidu(jsonObject.toString()).getBundle();// ��������
			Log.v("���յ���ʲô����", jsonObject.toString());
			if (bundle.getString("status").equals("ok")) {// ������������������
				editor.putString(
						// �洢������
						"widget",
						bundle.getStringArrayList("item1").get(0) + "," + bundle.getStringArrayList("item1").get(6)
								+ "," + bundle.getStringArrayList("item1").get(3) + ","
								+ bundle.getStringArrayList("item1").get(2).substring(11, 16) + "����" + ","
								+ bundle.getStringArrayList("item1").get(7));
				editor.commit();
			}
		}
		arrayList.clear();
		// �ָ����ݣ�������ȥ�����Ÿ���ArrayList
		String widgetdata = sharedPreferences.getString("widget", "");
		Log.v("ȡ�����ݣ�", widgetdata);
		int j = 0;
		for (int i = 0; i < widgetdata.length(); i++) {
			if (widgetdata.substring(i, i + 1).equals(",")) {
				arrayList.add(widgetdata.substring(j, i));
				j = i + 1;
			}
		}
		arrayList.add(widgetdata.substring(j, widgetdata.length()));
		views.setTextViewText(R.id.city_big, arrayList.get(0));
		views.setTextViewText(R.id.temper_big, arrayList.get(1) + "��");
		views.setTextViewText(R.id.weather_big, arrayList.get(2));
		views.setTextViewText(R.id.fresh_big, arrayList.get(3));
		FreshTime(mContext, views);// ˢ��ʱ���Ƕ�Ҫ����
		widgetOnClick(mContext, views);// �����¼�Ҳ�Ƕ�Ҫ����
		changeWidgetPicture(views);// ˢ��ͼƬҲ�Ƕ�Ҫ����
		ComponentName thisWidget = new ComponentName(mContext, BigWeatherWidget.class);
		AppWidgetManager.getInstance(mContext).updateAppWidget(thisWidget, views);
	}

	/**
	 * ����������icon
	 * 
	 * @param views
	 */
	private void changeWidgetPicture(RemoteViews views) {
		switch (Integer.valueOf(arrayList.get(4))) {

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
	}

	/**
	 * ͨ��������ȡӦ�ó����Ĭ��Activity
	 * 
	 * @param packagename
	 * @return
	 */
	private String doStartApplicationWithPackageName(String packagename) {

		// ͨ��������ȡ��APP��ϸ��Ϣ������Activities��services��versioncode��name�ȵ�
		PackageInfo packageinfo = null;
		try {
			packageinfo = mContext.getPackageManager().getPackageInfo(packagename, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packageinfo == null) {
			return "";
		}
		// ����һ�����ΪCATEGORY_LAUNCHER�ĸð�����Intent
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(packageinfo.packageName);

		// ͨ��getPackageManager()��queryIntentActivities��������
		List<ResolveInfo> resolveinfoList = mContext.getPackageManager().queryIntentActivities(resolveIntent, 0);

		ResolveInfo resolveinfo = resolveinfoList.iterator().next();
		if (resolveinfo != null) {
			String packageName = resolveinfo.activityInfo.packageName;
			String className = resolveinfo.activityInfo.name;
			return className;
		}
		return "";
	}

	/**
	 * �첽�࣬����������ȡ����
	 * 
	 * @author Zdy
	 * 
	 */
	class GetInfomationFromNetInBigWidget extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			Log.d("widget+doInBackground", "widget+doInBackground");

			String httpUrl = "https://api.heweather.com/x3/weather?cityid=" + DBManager.getIdByCityName(params[0])
					+ "&key=57efa20515e94db68ae042319463dba4";
			String jsonResult = NetWorkUtils.request(httpUrl);
			return jsonResult;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			Log.d("widget+onPostExecute", "widget+onPostExecute");
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(result);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			super.onPostExecute(result);
			bildview(1, jsonObject);
		}

	}
}
