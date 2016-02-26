package com.stu.zdy.weather.service;

import com.stu.zdy.weather.data.FinalField;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class WidgetService extends Service {
	private boolean run = true;
	private Context mContext;
	private int time = 14400000;
	private Thread thread;
	private Thread thread2;
	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				Intent intent2 = new Intent(FinalField.PackageNameBig);
				intent2.putExtra("index", 1);
				mContext.sendBroadcast(new Intent(intent2));
				Intent intent3 = new Intent(FinalField.PackageNameSmall);
				mContext.sendBroadcast(new Intent(intent3));
			}
		}
	};

	public void onStart(Intent intent, int startId) {
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.v("onStartCommand", "onStartCommand");
		getApplicationContext().registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		mContext = getApplicationContext();
		SharedPreferences preferences = mContext.getSharedPreferences("citys", Context.MODE_PRIVATE);
		time = preferences.getInt("time", 14400000);
		thread = new Thread() {
			public void run() {
				while (run) {
					try {
						Thread.sleep(time);
					} catch (Exception e) {
						// TODO: handle exception
					}
					Intent intent = new Intent(FinalField.PackageNameBig);
					intent.putExtra("index", 12);
					mContext.sendBroadcast(new Intent(intent));
					mContext.sendBroadcast(new Intent(FinalField.PackageNameSmall));
				}
			};
		};
		thread.start();
		thread2 = new Thread() {
			public void run() {
				while (run) {
					try {
						Thread.sleep(60000);
					} catch (Exception e) {
					}
					Intent intent = new Intent(FinalField.PackageNameBig);
					intent.putExtra("index", 1);
					mContext.sendBroadcast(new Intent(intent));
				}
			};
		};
		thread2.start();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
