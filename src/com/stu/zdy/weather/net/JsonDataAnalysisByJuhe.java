package com.stu.zdy.weather.net;

import java.util.ArrayList;
import java.util.Calendar;

import net.sf.json.JSONObject;
import android.os.Bundle;
import android.util.Log;

public class JsonDataAnalysisByJuhe {
	private JSONObject jsonObject;
	public ArrayList<String> item1 = new ArrayList<String>();// 状态信息和实时天气
	public ArrayList<String> item2 = new ArrayList<String>();// 未来五天的天气
	public ArrayList<String> item3 = new ArrayList<String>();// 三小时天气
	public ArrayList<String> item4 = new ArrayList<String>();// 生活建议
	private Bundle bundle;
	private JSONObject jsonitem;

	public JsonDataAnalysisByJuhe(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
		Log.v("当前服务器状态", jsonObject.getString("resultcode"));
		if (jsonObject.getString("status").equals("200")) {
			analysisData();
		} else {
			bundle = new Bundle();
			bundle.putString("status", "error");
		}
	}

	/**
	 * @author Zdy item1从下标0开始分别为：城市，城市ID，更新时间，天气，湿度，风力，温度，天气代码,紫外线强度
	 *         item2从下标0到4为未来五日的低温，5到9为未来五日的高温
	 *         ，10到14分别为未来五日的天气情况代码，15到24每两个为单日的天气情况
	 *         item3为为三小时天气预报，长度不定，根据显示的情况，时间在前，温度在后。 item4为生活建议
	 */
	private void analysisData() {
		jsonitem = jsonObject.getJSONObject("result");
		item1.add(jsonitem.getJSONObject("today").getString("city"));
		item1.add("empty");
		item1.add(jsonitem.getJSONObject("sk").getString("time"));
		item1.add(jsonitem.getJSONObject("today").getString("weather"));
		item1.add(jsonitem.getJSONObject("sk").getString("humidity"));
		item1.add(jsonitem.getJSONObject("sk").getString("wind_direction")
				+ jsonitem.getJSONObject("sk").getString("wind_strength"));
		item1.add(jsonitem.getJSONObject("sk").getString("temp"));
		item1.add(jsonitem.getJSONObject("today").getJSONObject("weather_id")
				.getString("fa"));
		item1.add(jsonitem.getJSONObject("today").getString("uv_index"));
		for (int i = 0; i < 5; i++) {// item2，0到9位置
			getDateString(jsonitem, i);
			String temperature = jsonitem.getString("temperature");
			// 切字符串，取出无关符号
			for (int j = 0; i < temperature.length(); j++) {
				int min = 0, max = 0;
				if (temperature.substring(j, j + 1).equals("℃")) {
					min = Integer.valueOf(temperature.substring(0, j));
					max = Integer.valueOf(temperature.substring(j + 2,
							temperature.length() - 1));
					item2.add(i, String.valueOf(min));
					item2.add(String.valueOf(max));
					break;
				}
			}
		}
		for (int i = 0; i < 5; i++) {// item2，10到14位置
			getDateString(jsonitem, i);
			String weather_id = jsonitem.getJSONObject("weather_id").getString(
					"fa");
			item2.add(weather_id);
		}
		for (int i = 0; i < 5; i++) {// item2，15-24位置
			getDateString(jsonitem, i);
			String weather = jsonitem.getString("weather");
			// 切字符串
			for (int j = 0; j < weather.length(); j++) {
				if (weather.substring(j, j + 1).equals("转")) {
					item2.add(weather.substring(0, j));
					item2.add(weather.substring(j + 1, weather.length()));
				} else {
					if (j == weather.length() - 1) {
						item2.add(weather);
						item2.add(weather);
					}
				}
			}
		}

		jsonitem = jsonObject.getJSONObject("result");
		item4.add(jsonitem.getJSONObject("today").getString("dressing_advice"));
		item4.add(jsonitem.getJSONObject("today").getString("wash_index"));
		item4.add(jsonitem.getJSONObject("today").getString("exercise_index"));
		item4.add(jsonitem.getJSONObject("today").getString("travel_index"));

		bundle = new Bundle();
		bundle.putString("status", "ok");
		bundle.putStringArrayList("item1", item1);
		bundle.putStringArrayList("item2", item2);
		bundle.putStringArrayList("item3", item3);
		bundle.putStringArrayList("item4", item4);
	}

	private void getDateString(JSONObject jsonitem, int i) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_YEAR,
				Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + i);
		calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
		// get未来日期节点，规格="day_"+年月日
		jsonitem.getJSONObject("future").getJSONObject(
				"day_"
						+ String.valueOf(calendar.get(Calendar.YEAR))
						+ String.valueOf(Integer.valueOf(calendar
								.get(Calendar.MONTH)) > 8 ? calendar
								.get(Calendar.MONTH) + 1
								: String.valueOf("0"
										+ Integer.valueOf(calendar
												.get(Calendar.MONTH) + 1)))
						+ String.valueOf(calendar.get(Calendar.DATE)));
	}

	public Bundle getBundle() {
		return bundle;
	}

	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}
}
