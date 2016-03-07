package com.stu.zdy.weather.util;

import java.util.List;

import com.stu.zdy.weather.service.WidgetService;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class ApplicationUtils {

	public static boolean isMyServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.stu.zdy.weather.service.WidgetService".equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param packagename
	 * @return
	 */
	public static String doStartApplicationWithPackageName(Context context, String packagename) {

		PackageInfo packageinfo = null;
		try {
			packageinfo = context.getPackageManager().getPackageInfo(packagename, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packageinfo == null) {
			return "";
		}
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(packageinfo.packageName);

		List<ResolveInfo> resolveinfoList = context.getPackageManager().queryIntentActivities(resolveIntent, 0);

		ResolveInfo resolveinfo = resolveinfoList.iterator().next();
		if (resolveinfo != null) {
			String packageName = resolveinfo.activityInfo.packageName;
			String className = resolveinfo.activityInfo.name;
			return className;
		}
		return "";
	}

	/**
	 * 运行服务，内部包含检测是否运行
	 * 
	 * @param context
	 */
	public static void runService(Context context) {
		// TODO Auto-generated method stub
		if (!isMyServiceRunning(context)) {
			Intent intent2 = new Intent(context, WidgetService.class);
			context.startService(intent2);
		}
	}
}
