package com.stu.zdy.weather.ui.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.stu.zdy.weather.interfaces.FragmentCallBack;
import com.stu.zdy.weather.mananger.SharePreferenceMananger;
import com.stu.zdy.weather.open_source.MaterialDialog;
import com.stu.zdy.weather.ui.MainActivity;
import com.stu.zdy.weather.util.ApplicationUtils;
import com.stu.zdy.weather.util.ScreenUtils;
import com.stu.zdy.weather.view.MyListView;
import com.stu.zdy.weather_sample.R;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SettingFragment extends Fragment {

    public final static String TAG = "SettingFragment";
    private FragmentCallBack fragmentCallBack = null;
    private SharedPreferences sharedPreferences;
    private LayoutInflater mLayoutInflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view = inflater.inflate(R.layout.fragment_setting, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        mLayoutInflater = android.view.LayoutInflater.from(getActivity());
        sharedPreferences = getActivity().getSharedPreferences("weather_info", Context.MODE_PRIVATE);
        MyListView listView = (MyListView) getActivity().findViewById(R.id.setting_listView);
        ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();

        String[] titles = getResources().getStringArray(R.array.setting_title);
        String[] subtitles = getResources().getStringArray(R.array.setting_subtitle);

        for (int i = 0; i < titles.length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("describe", titles[i]);
            map.put("info", subtitles[i]);
            arrayList.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(getActivity(), arrayList, R.layout.item_listview_setting,
                new String[]{"describe", "info"}, new int[]{R.id.setting_switch_describe, R.id.setting_switch_detail}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View item = super.getView(position, convertView, parent);
                CheckBox box = (CheckBox) item.findViewById(R.id.setting_switch_checkbox);
                switch (position) {
                    case 0:
                    case 4:
                    case 5:
                        box.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        box.setChecked(sharedPreferences.getBoolean("lifeAdvice", true));
                        break;
                    case 2:
                        box.setChecked(sharedPreferences.getBoolean("naviBar", false));
                        break;
                    case 3:
                        box.setChecked(sharedPreferences.getBoolean("moreColor", true));
                        break;
                    case 6:
                        box.setChecked(sharedPreferences.getBoolean("widget_mask", true));
                        break;
                }
                return item;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                // TODO Auto-generated method stub
                CheckBox box = (CheckBox) view.findViewById(R.id.setting_switch_checkbox);
                box.setClickable(false);
                switch (position) {
                    case 0:
                        initMaterialDialog(1);
                        break;
                    case 1:
                        box.setChecked(!box.isChecked());
                        SharePreferenceMananger.saveSharePreferenceFromBoolean(getActivity(), "weather_info", "lifeAdvice", box.isChecked());
                        break;
                    case 2:
                        box.setChecked(!box.isChecked());
                        SharePreferenceMananger.saveSharePreferenceFromBoolean(getActivity(), "weather_info", "naviBar", box.isChecked());
                        break;
                    case 3:
                        box.setChecked(!box.isChecked());
                        SharePreferenceMananger.saveSharePreferenceFromBoolean(getActivity(), "weather_info", "moreColor", box.isChecked());
                        break;
                    case 4:
                        initMaterialDialog(2);
                        break;
                    case 5:
                        initMaterialDialog(3);
                        break;
                    case 6:
                        box.setChecked(!box.isChecked());
                        SharePreferenceMananger.saveSharePreferenceFromBoolean(getActivity(), "weather_info", "widget_mask", box.isChecked());
                        break;
                    default:
                        break;
                }

            }
        });

    }

    private void initMaterialDialog(int type) {
        final MaterialDialog dialog = new MaterialDialog(getActivity());
        View mRadioLayout;
        RadioGroup group;
        switch (type) {
            case 1:
                mRadioLayout = mLayoutInflater.inflate(R.layout.dialog_radio, null);
                group = (RadioGroup) mRadioLayout.findViewById(R.id.setting_radio_radioGroup);
                String[] rbStrings = getResources().getStringArray(R.array.refresh_time);
                for (int i = 0; i < 4; i++) {
                    RadioButton rb = new RadioButton(getActivity());
                    rb.setChecked(false);
                    RadioGroup.LayoutParams rbParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                    rbParams.setMargins(0, ScreenUtils.dip2px(getActivity(), 4), 0, ScreenUtils.dip2px(getActivity(), 4));
                    rb.setText(rbStrings[i]);
                    rb.setLayoutParams(rbParams);
                    group.addView(rb);
                }
                int refreshTime = SharePreferenceMananger.getSharePreferenceFromInteger(getActivity(), "weather_info", "refreshTime");
                switch (refreshTime) {
                    case 7200000:
                        ((RadioButton) group.getChildAt(0)).setChecked(true);
                        break;
                    case 14400000:
                        ((RadioButton) group.getChildAt(1)).setChecked(true);
                        break;
                    case 28800000:
                        ((RadioButton) group.getChildAt(2)).setChecked(true);
                        break;
                    case 57600000:
                        ((RadioButton) group.getChildAt(3)).setChecked(true);
                        break;
                }
                group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (group.getChildAt(0).getId() == checkedId) {
                            SharePreferenceMananger.saveSharePreferenceFromInteger(getActivity(), "weather_info", "refreshTime", 7200000);
                        } else if (group.getChildAt(1).getId() == checkedId) {
                            SharePreferenceMananger.saveSharePreferenceFromInteger(getActivity(), "weather_info", "refreshTime", 14400000);
                        } else if (group.getChildAt(2).getId() == checkedId) {
                            SharePreferenceMananger.saveSharePreferenceFromInteger(getActivity(), "weather_info", "refreshTime", 28800000);
                        } else if (group.getChildAt(3).getId() == checkedId) {
                            SharePreferenceMananger.saveSharePreferenceFromInteger(getActivity(), "weather_info", "refreshTime", 57600000);
                        }
                        if (ApplicationUtils.stopService(getActivity())) {
                            ApplicationUtils.runService(getActivity());
                        }
                        Toast.makeText(getActivity(), getString(R.string.widget_refresh_time)
                                + SharePreferenceMananger.getSharePreferenceFromInteger(getActivity(), "weather_info", "refreshTime") + "毫秒吧~", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                dialog.setContentView(mRadioLayout).setTitle(R.string.widget_refresh).setNegativeButton(R.string.close, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                }).show();
                break;
            case 2:
                final EditText editText = new EditText(getActivity());
                editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                editText.setText(sharedPreferences.getString("clockPackageName", "com.google.android.deskclock"));
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                dialog.setContentView(editText).setTitle(getString(R.string.input_package_name)).setNegativeButton(R.string.close, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                }).setPositiveButton(getString(R.string.open), new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        SharePreferenceMananger.saveSharePreferenceFromString(getActivity(), "weather_info", "clockPackageName", editText.getText().toString());
                        dialog.dismiss();
                    }
                }).setMessage("修改之后您可能需要重新添加小部件").show();
                break;
            case 3:
                mRadioLayout = mLayoutInflater.inflate(R.layout.dialog_radio, null);
                group = (RadioGroup) mRadioLayout.findViewById(R.id.setting_radio_radioGroup);
                String currentCity = SharePreferenceMananger.getSharePreferenceFromString(getActivity(), "weather_info", "currentCity");
                JSONArray citys = null;
                try {
                    citys = new JSONObject(SharePreferenceMananger.getSharePreferenceFromString(getActivity(), "weather_info", "citylist")).getJSONArray("citylist");
                    for (int i = 0; i < citys.length(); i++) {
                        RadioButton rb = new RadioButton(getActivity());
                        rb.setChecked(false);
                        RadioGroup.LayoutParams rbParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                        rbParams.setMargins(0, ScreenUtils.dip2px(getActivity(), 4), 0, ScreenUtils.dip2px(getActivity(), 4));
                        rb.setText((String) citys.get(i));
                        rb.setLayoutParams(rbParams);
                        if (currentCity.equals((String) citys.get(i))) rb.setChecked(true);
                        group.addView(rb);
                    }
                } catch (JSONException e) {

                }
                group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        for (int i = 0; i < group.getChildCount(); i++) {
                            RadioButton rb = (RadioButton) group.getChildAt(i);
                            if (checkedId == rb.getId()) {
                                rb.setChecked(true);
                                SharePreferenceMananger.saveSharePreferenceFromString(getActivity(), "weather_info", "currentCity", rb.getText().toString());
                                Toast.makeText(getActivity(), getString(R.string.select_city) + rb.getText().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        dialog.dismiss();
                    }
                });
                dialog.setContentView(mRadioLayout).setTitle(R.string.choose_city).setNegativeButton(R.string.close, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                }).show();

        }
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(SettingFragment.TAG);
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    fragmentCallBack.callbackSettingFragment(null);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(SettingFragment.TAG);
    }

    @Override
    public void onAttach(Activity activity) {// 启动Fragment调用
        // TODO Auto-generated method stub
        super.onAttach(activity);
        fragmentCallBack = (MainActivity) activity;// 将Activity实例赋给fragmentCallBack
    }
}
