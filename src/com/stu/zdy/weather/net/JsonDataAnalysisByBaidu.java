package com.stu.zdy.weather.net;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

/**
 * �����ͷ��������ص�Json����
 * 
 * @author Zdy
 * 
 */
public class JsonDataAnalysisByBaidu {

	private JSONObject jsonObject;
	private Bundle bundle;

	public JsonDataAnalysisByBaidu(String result) {
		try {
			this.jsonObject = new JSONObject(result);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			bundle = new Bundle();
			bundle.putString("status", "error");
			return;
		}
		try {
			if (jsonObject.getJSONArray("HeWeather5").getJSONObject(0).getString("status")
					.equals("ok")) {
				analysisData();
			} else {
				bundle = new Bundle();
				bundle.putString("status", "error");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bundle = new Bundle();
			bundle.putString("status", "error");
		}
	}

	/**
	 * @author Zdy item1包括城市名，城市Id，更新时间，
	 * @throws JSONException
	 */
	private void analysisData() throws JSONException {

		JSONObject basic = jsonObject.getJSONArray("HeWeather5").getJSONObject(0)
				.getJSONObject("basic");

		ArrayList<String> item1 = new ArrayList<String>();
		item1.add(basic.getString("city"));// 城市名
		item1.add(basic.getString("id"));// 城市id
		item1.add(basic.getJSONObject("update").getString("loc").substring(11));// 更新时间
		JSONObject now = jsonObject.getJSONArray("HeWeather5").getJSONObject(0).getJSONObject("now");// 实况天气
		item1.add(now.getJSONObject("cond").getString("txt"));// 当前天气描述
		item1.add(now.getString("hum"));// 湿度
		item1.add(now.getJSONObject("wind").getString("dir") + now.getJSONObject("wind").getString("sc") + "级");// 当前风力风向
		item1.add(now.getString("tmp"));// 当前温度
		item1.add(now.getJSONObject("cond").getString("code"));// 当前天气状况代码
		item1.add(jsonObject.getJSONArray("HeWeather5").getJSONObject(0).getJSONObject("suggestion")
				.getJSONObject("uv").getString("brf"));// 紫外线强度

		JSONArray daily_forcast = jsonObject.getJSONArray("HeWeather5").getJSONObject(0)
				.getJSONArray("daily_forecast");// 未来天气预报

		ArrayList<String> item2 = new ArrayList<String>();
		for (int i = 0; i < 5; i++) {
			item2.add(i, daily_forcast.getJSONObject(i).getJSONObject("tmp").getString("min"));// 未来五天低温
			item2.add(daily_forcast.getJSONObject(i).getJSONObject("tmp").getString("max"));// 未来五天高温
		}
		for (int i = 0; i < 5; i++) {
			item2.add(daily_forcast.getJSONObject(i).getJSONObject("cond").getString("txt_d"));// 未来五天白天天气描述
			item2.add(daily_forcast.getJSONObject(i).getJSONObject("cond").getString("txt_n"));// 未来五天夜晚天气描述
		}
		for (int i = 0; i < 5; i++) {
			item2.add(10 + i, daily_forcast.getJSONObject(i).getJSONObject("cond").getString("code_d"));// 未来五天白天天气状况代码
		}

		JSONArray hourly_forecast = jsonObject.getJSONArray("HeWeather5").getJSONObject(0)
				.getJSONArray("hourly_forecast");// 未来三小时天气预报

		ArrayList<String> item3 = new ArrayList<String>();
		for (int i = 0; i < hourly_forecast.length(); i++) {
			item3.add(i, hourly_forecast.getJSONObject(i).getString("date"));// 更新时间
			item3.add(hourly_forecast.getJSONObject(i).getString("tmp"));// 温度
		}

		JSONObject suggestion = jsonObject.getJSONArray("HeWeather5").getJSONObject(0)
				.getJSONObject("suggestion");
		ArrayList<String> item4 = new ArrayList<String>();
		item4.add(suggestion.getJSONObject("comf").getString("txt"));// 舒适度描述
		item4.add(suggestion.getJSONObject("cw").getString("brf"));// 洗车指数
		item4.add(suggestion.getJSONObject("sport").getString("brf"));// 运动指数
		item4.add(suggestion.getJSONObject("trav").getString("brf"));// 出行指数

		try {
			JSONObject aqi = jsonObject.getJSONArray("HeWeather5").getJSONObject(0)
					.getJSONObject("aqi");
			ArrayList<String> item5 = new ArrayList<String>();
			item5.add(aqi.getJSONObject("city").getString("aqi"));
			item5.add(aqi.getJSONObject("city").getString("co"));
			item5.add(aqi.getJSONObject("city").getString("no2"));
			item5.add(aqi.getJSONObject("city").getString("o3"));
			item5.add(aqi.getJSONObject("city").getString("pm10"));
			item5.add(aqi.getJSONObject("city").getString("pm25"));
			item5.add(aqi.getJSONObject("city").getString("qlty"));
			item5.add(aqi.getJSONObject("city").getString("so2"));
			bundle.putStringArrayList("item5", item5);
		} catch (Exception e) {
			// TODO: handle exception
		}

		bundle = new Bundle();
		bundle.putString("status", "ok");
		bundle.putStringArrayList("item1", item1);
		bundle.putStringArrayList("item2", item2);
		bundle.putStringArrayList("item3", item3);
		bundle.putStringArrayList("item4", item4);
	}

	public Bundle getBundle() {
		return bundle;
	}

	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}

}
