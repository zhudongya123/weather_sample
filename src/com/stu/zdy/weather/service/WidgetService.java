package com.stu.zdy.weather.service;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.stu.zdy.weather.app.MyApplication;
import com.stu.zdy.weather.mananger.SharePreferenceMananger;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class WidgetService extends Service {
	private Context mContext;
	private int time = 14400000;
	private Timer timer = null;
	private TimerTask timerTask = null;

	private void sendBroadCastTimer(int type) {
		if (type == 1) {
			timer = new Timer();
			timerTask = new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mContext.sendBroadcast(new Intent(MyApplication.PackageNameBig));
					mContext.sendBroadcast(new Intent(MyApplication.PackageNameSmall));
				}
			};
			timer.schedule(timerTask, Calendar.getInstance().getTime(), time);
		} else {
			timer.cancel();
			timerTask.cancel();
			timer = null;
			timerTask = null;
		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.v("onStartCommand", "onStartCommand");
		mContext = getApplicationContext();
		time = SharePreferenceMananger.getSharePreferenceFromInteger(this, "weather_info", "refreshTime");
		sendBroadCastTimer(1);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		sendBroadCastTimer(0);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
