package com.stu.zdy.weather.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class FileUtils {


    public static boolean saveCityList(Context context, String input) throws JSONException {
        SharedPreferences sharedPreferences = context.getSharedPreferences("weather_info", Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();

        String output = sharedPreferences.getString("citylist", new JSONObject().toString());
        JSONObject temp = null;
        try {
            temp = new JSONObject(output);
            if (temp.getJSONArray("citylist").length() == 0) {
                editor.putString("currentCity", input);
            }
            temp.getJSONArray("citylist").put(input);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        editor.putString("citylist", temp.toString());
        editor.commit();
        return true;
    }

    public static JSONObject getCityList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("weather_info", Context.MODE_PRIVATE);
        String output = sharedPreferences.getString("citylist", new JSONObject().toString());
        try {
            return new JSONObject(output);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
        return new JSONObject();
    }

    public static String getCityFromJsonArray(Context context, int index) {
        JSONObject jsonObject = getCityList(context);
        try {
            return (String) jsonObject.getJSONArray("citylist").get(index);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    @SuppressLint("NewApi")
    public static boolean removeCityFromJsonArray(Context context, int index) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("weather_info", Context.MODE_PRIVATE);
        JSONObject jsonObject = getCityList(context);
        try {
            jsonObject.getJSONArray("citylist").remove(index);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        Editor editor = sharedPreferences.edit();
        editor.putString("citylist", jsonObject.toString());
        editor.commit();
        return true;
    }

    public static void write(Context context, String cityName, String data) {
        // TODO Auto-generated method stub
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(cityName, Context.MODE_PRIVATE);
            PrintStream printStream = new PrintStream(fileOutputStream);
            printStream.print(data);
            printStream.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String read(Context context, String cityName) {
        // TODO Auto-generated method stub
        try {
            FileInputStream fileInputStream = context.openFileInput(cityName);
            byte[] bs = new byte[3072];
            int hasRead = 0;
            StringBuilder stringBuilder = new StringBuilder("");
            while ((hasRead = fileInputStream.read(bs)) > 0) {
                stringBuilder.append(new String(bs, 0, hasRead));
            }
            fileInputStream.close();
            return stringBuilder.toString();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteDirectory(String filePath) {
        boolean flag = false;
        // 如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        // 遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                // 删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            } else {
                // 删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        return flag;
    }

    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }
}
