package com.stu.zdy.weather.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class WidgetService extends Service {
	private static final String PackageNameBig = "com.stu.zdy.weather.big";
	private static final String PackageNameSmall = "com.stu.zdy.weather.small";
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
				Intent intent2 = new Intent(PackageNameBig);
				intent2.putExtra("index", 1);
				mContext.sendBroadcast(new Intent(intent2));
				Intent intent3 = new Intent(PackageNameSmall);
				mContext.sendBroadcast(new Intent(intent3));
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.v("onStartCommand", "onStartCommand");
		getApplicationContext().registerReceiver(broadcastReceiver,
				new IntentFilter(Intent.ACTION_SCREEN_ON));
		mContext = getApplicationContext();
		SharedPreferences preferences = mContext.getSharedPreferences("citys",
				Context.MODE_PRIVATE);
		time = preferences.getInt("time", 14400000);
		Log.e("给我查一下间隔时间是多少", String.valueOf(time));
		thread = new Thread() {// 此线程用来联网更新数据
			public void run() {
				while (run) {
					try {
						Thread.sleep(time);// 延迟time毫秒
					} catch (Exception e) {
						// TODO: handle exception
					}
					Log.v("在服务里面发送出了两条需要联网更新的广播", "间隔时间由settingFragment指定");
					Intent intent = new Intent(PackageNameBig);
					intent.putExtra("index", 12);
					mContext.sendBroadcast(new Intent(intent));
					mContext.sendBroadcast(new Intent(PackageNameSmall));
				}
			};
		};
		thread.start();
		thread2 = new Thread() {// 此线程用来自动更新时钟
			public void run() {
				while (run) {
					try {
						Thread.sleep(60000);// 每隔一分钟发送
					} catch (Exception e) {
					}
					Log.v("发出了广播", "广播用来实时更新时钟");
					Intent intent = new Intent(PackageNameBig);
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
}
