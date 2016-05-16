package com.stu.zdy.weather.util;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Zdy on 2016/5/2.
 */
public class JSONUtils {
    public static JSONObject getJSONObject(String raw) {
        JSONObject object = null;
        try {
            object = new JSONObject(raw);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }
}
