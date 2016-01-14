package com.stu.zdy.weather.net;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;

import net.sf.json.JSONObject;

/**
 * 解析和风天气返回的Json数据
 * 
 * @author Zdy
 * 
 */
public class JsonDataAnalysisByHe {

	private JSONObject jsonObject;
	public ArrayList<String> item1 = new ArrayList<String>();// 状态信息和实时天气
	public ArrayList<String> item2 = new ArrayList<String>();// 未来五天的天气
	public ArrayList<String> item3 = new ArrayList<String>();// 三小时天气
	public ArrayList<String> item4 = new ArrayList<String>();// 生活建议
	private Bundle bundle;

	public JsonDataAnalysisByHe(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
		Log.v("当前服务器状态", jsonObject.getJSONArray("HeWeather data service 3.0")
				.getJSONObject(0).getString("status"));
		if (jsonObject.getJSONArray("HeWeather data service 3.0")
				.getJSONObject(0).getString("status").equals("ok")) {
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
		JSONObject jsonitem = jsonObject
				.getJSONArray("HeWeather data service 3.0").getJSONObject(0)
				.getJSONObject("basic");
		item1.add(jsonitem.getString("city"));
		item1.add(jsonitem.getString("id"));
		item1.add(jsonitem.getJSONObject("update").getString("loc"));
		jsonitem = jsonObject.getJSONArray("HeWeather data service 3.0")
				.getJSONObject(0).getJSONObject("now");
		item1.add(jsonitem.getJSONObject("cond").getString("txt"));
		item1.add(jsonitem.getString("hum"));
		item1.add(jsonitem.getJSONObject("wind").getString("dir")
				+ jsonitem.getJSONObject("wind").getString("sc") + "级");
		item1.add(jsonitem.getString("tmp"));
		item1.add(jsonitem.getJSONObject("cond").getString("code"));
		item1.add(jsonObject.getJSONArray("HeWeather data service 3.0")
				.getJSONObject(0).getJSONObject("suggestion")
				.getJSONObject("uv").getString("brf"));
		for (int i = 0; i < 5; i++) {
			jsonitem = jsonObject.getJSONArray("HeWeather data service 3.0")
					.getJSONObject(0).getJSONArray("daily_forecast")
					.getJSONObject(i);
			item2.add(i, jsonitem.getJSONObject("tmp").getString("min"));
			item2.add(jsonitem.getJSONObject("tmp").getString("max"));
		}
		for (int i = 0; i < 5; i++) {
			jsonitem = jsonObject.getJSONArray("HeWeather data service 3.0")
					.getJSONObject(0).getJSONArray("daily_forecast")
					.getJSONObject(i);
			item2.add(jsonitem.getJSONObject("cond").getString("txt_d"));
			item2.add(jsonitem.getJSONObject("cond").getString("txt_n"));
			item2.add(10 + i, jsonitem.getJSONObject("cond")
					.getString("code_d"));
		}
		for (int i = 0; i < jsonObject
				.getJSONArray("HeWeather data service 3.0").getJSONObject(0)
				.getJSONArray("hourly_forecast").size(); i++) {
			jsonitem = jsonObject.getJSONArray("HeWeather data service 3.0")
					.getJSONObject(0).getJSONArray("hourly_forecast")
					.getJSONObject(i);
			item3.add(i, jsonitem.getString("date"));
			item3.add(jsonitem.getString("tmp"));
		}
		jsonitem = jsonObject.getJSONArray("HeWeather data service 3.0")
				.getJSONObject(0).getJSONObject("suggestion");
		item4.add(jsonitem.getJSONObject("comf").getString("txt"));
		item4.add(jsonitem.getJSONObject("cw").getString("brf"));
		item4.add(jsonitem.getJSONObject("sport").getString("brf"));
		item4.add(jsonitem.getJSONObject("trav").getString("brf"));

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
