package com.stu.zdy.weather.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;

import com.stu.zdy.weather.service.WidgetService;

import java.util.List;

/**
 * 2016年3月19日17:19:05
 * 重新整合代码
 */

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
    public static boolean runService(Context context) {
        // TODO Auto-generated method stub
        if (!isMyServiceRunning(context)) {
            Intent start = new Intent(context, WidgetService.class);
            context.startService(start);
            return true;
        }
        return false;
    }

    public static boolean stopService(Context context) {
        // TODO Auto-generated method stub
        if (isMyServiceRunning(context)) {
            Intent start = new Intent(context, WidgetService.class);
            context.stopService(start);
            return true;
        }
        return false;
    }

    public static boolean startAPP(Context context, String appPackageName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
