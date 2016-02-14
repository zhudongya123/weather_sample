package com.stu.zdy.weather.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class FileUtils {
	public enum DataOperate {
		SAVE, ADD, // GET,
	}

	public static boolean saveCityList(Context context, DataOperate operate,
			String input) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"weather_info", Context.MODE_PRIVATE);
		if (operate == FileUtils.DataOperate.SAVE) {
			Editor editor = sharedPreferences.edit();
			editor.putString("citylist", input);
			editor.commit();
			return true;
		}
		if (operate == FileUtils.DataOperate.ADD) {
			String output = sharedPreferences.getString("citylist",
					new JSONObject().toString());
			JSONObject temp = null;
			try {
				temp = new JSONObject(output);
				temp.getJSONArray("citylist").put(input);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			Editor editor = sharedPreferences.edit();
			editor.putString("citylist", temp.toString());
			editor.commit();
			return true;
		}
		return false;
	}

	public static JSONObject getCityList(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"weather_info", Context.MODE_PRIVATE);
		String output = sharedPreferences.getString("citylist",
				new JSONObject().toString());
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
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"weather_info", Context.MODE_PRIVATE);
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
			FileOutputStream fileOutputStream = context.openFileOutput(
					cityName, Context.MODE_PRIVATE);
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
}
