package com.stu.zdy.weather.net;

import java.util.ArrayList;

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
public class JsonDataAnalysisByHe {

	private JSONObject jsonObject;
	public ArrayList<String> item1 = new ArrayList<String>();// ״̬��Ϣ��ʵʱ����
	public ArrayList<String> item2 = new ArrayList<String>();// δ�����������
	public ArrayList<String> item3 = new ArrayList<String>();// ��Сʱ����
	public ArrayList<String> item4 = new ArrayList<String>();// �����
	private Bundle bundle;

	public JsonDataAnalysisByHe(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
		try {
			Log.v("��ǰ������״̬", jsonObject.getJSONArray("HeWeather data service 3.0")
					.getJSONObject(0).getString("status"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			if (jsonObject.getJSONArray("HeWeather data service 3.0")
					.getJSONObject(0).getString("status").equals("ok")) {
				analysisData();
			} else {
				bundle = new Bundle();
				bundle.putString("status", "error");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @author Zdy
	 *         item1���±�0��ʼ�ֱ�Ϊ�����У�����ID������ʱ�䣬������ʪ�ȣ��������¶ȣ���
	 *         ������,������ǿ�� item2���±�0��4Ϊδ�����յĵ��£�5��9Ϊδ�����յĸ���
	 *         ��10��14�ֱ�Ϊδ�����յ�����������룬15��24ÿ����Ϊ���յ��������
	 *         item3ΪΪ��Сʱ����Ԥ�������Ȳ�����������ʾ�������ʱ����ǰ���¶��ں�
	 *         item4Ϊ�����
	 * @throws JSONException
	 */
	private void analysisData() throws JSONException {
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
				.getJSONArray("hourly_forecast").length(); i++) {
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
