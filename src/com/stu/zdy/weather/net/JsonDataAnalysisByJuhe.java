package com.stu.zdy.weather.net;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

public class JsonDataAnalysisByJuhe {
	private JSONObject jsonObject;
	public ArrayList<String> item1 = new ArrayList<String>();// ״̬��Ϣ��ʵʱ����
	public ArrayList<String> item2 = new ArrayList<String>();// δ�����������
	public ArrayList<String> item3 = new ArrayList<String>();// ��Сʱ����
	public ArrayList<String> item4 = new ArrayList<String>();// �����
	private Bundle bundle;
	private JSONObject jsonitem;

	public JsonDataAnalysisByJuhe(JSONObject jsonObject) throws JSONException {
		this.jsonObject = jsonObject;
		Log.v("��ǰ������״̬", jsonObject.getString("resultcode"));
		if (jsonObject.getString("status").equals("200")) {
			analysisData();
		} else {
			bundle = new Bundle();
			bundle.putString("status", "error");
		}
	}

	/**
	 * @author Zdy item1���±�0��ʼ�ֱ�Ϊ�����У�����ID������ʱ�䣬������ʪ�ȣ��������¶ȣ���������,������ǿ��
	 *         item2���±�0��4Ϊδ�����յĵ��£�5��9Ϊδ�����յĸ���
	 *         ��10��14�ֱ�Ϊδ�����յ�����������룬15��24ÿ����Ϊ���յ��������
	 *         item3ΪΪ��Сʱ����Ԥ�������Ȳ�����������ʾ�������ʱ����ǰ���¶��ں� item4Ϊ�����
	 * @throws JSONException 
	 */
	private void analysisData() throws JSONException {
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
		for (int i = 0; i < 5; i++) {// item2��0��9λ��
			getDateString(jsonitem, i);
			String temperature = jsonitem.getString("temperature");
			// ���ַ�����ȡ���޹ط���
			for (int j = 0; i < temperature.length(); j++) {
				int min = 0, max = 0;
				if (temperature.substring(j, j + 1).equals("��")) {
					min = Integer.valueOf(temperature.substring(0, j));
					max = Integer.valueOf(temperature.substring(j + 2,
							temperature.length() - 1));
					item2.add(i, String.valueOf(min));
					item2.add(String.valueOf(max));
					break;
				}
			}
		}
		for (int i = 0; i < 5; i++) {// item2��10��14λ��
			getDateString(jsonitem, i);
			String weather_id = jsonitem.getJSONObject("weather_id").getString(
					"fa");
			item2.add(weather_id);
		}
		for (int i = 0; i < 5; i++) {// item2��15-24λ��
			getDateString(jsonitem, i);
			String weather = jsonitem.getString("weather");
			// ���ַ���
			for (int j = 0; j < weather.length(); j++) {
				if (weather.substring(j, j + 1).equals("ת")) {
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

	private void getDateString(JSONObject jsonitem, int i) throws JSONException {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_YEAR,
				Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + i);
		calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
		// getδ�����ڽڵ㣬���="day_"+������
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
