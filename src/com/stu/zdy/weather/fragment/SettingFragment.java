package com.stu.zdy.weather.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.stu.zdy.weather.activity.MainActivity;
import com.stu.zdy.weather.object.FragmentCallBack;
import com.stu.zdy.weather.object.MaterialDialog;
import com.stu.zdy.weather.object.MyListView;
import com.stu.zdy.weather.service.WidgetService;
import com.stu.zdy.weather_sample.R;

@SuppressLint("InflateParams")
public class SettingFragment extends Fragment {

	private int i;
	private FragmentCallBack fragmentCallBack = null;
	private SharedPreferences sharedPreferences;
	private Editor editor;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.fragment_setting, null);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		sharedPreferences = getActivity().getSharedPreferences("citys",
				Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();
		MyListView listView = (MyListView) getActivity().findViewById(
				R.id.setting_listview);
		// listView.setDividerHeight(0);
		ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();
		SimpleAdapter adapter = new SimpleAdapter(getActivity(), arrayList,
				R.layout.setting_listview, new String[] { "descride", "info" },
				new int[] { R.id.descride, R.id.detail });
		for (int i = 0; i < 10; i++) {

			HashMap<String, Object> map = new HashMap<String, Object>();
			switch (i) {
			case 0:
				map.put("descride", "小部件后台刷新频率");
				map.put("info", "重新设置之后需要重新添加桌面小部件");
				arrayList.add(map);
				break;
			case 1:
				map.put("descride", "是否显示生活建议");
				map.put("info", "就算打开估计也不会看吧(重启生效)");
				arrayList.add(map);
				break;
			case 2:
				map.put("descride", "导航栏透明（实验性功能&&重启生效）");
				map.put("info", "没有虚拟按键就不要打开啦(。・`ω´・)");
				arrayList.add(map);
				break;
			case 3:
				map.put("descride", "多彩温度模式（下拉刷新生效）");
				map.put("info", "打开后温度数字将会显示更丰富的颜色");
				arrayList.add(map);
				break;
			case 4:
				map.put("descride", "更改桌面小部件点击事件包名");
				map.put("info", "点击时间跳转样例（仅支持4*2部件），默认为谷歌时钟，当您需要更改为其他应用时选择此项");
				arrayList.add(map);
				break;
			default:
				break;
			}

		}
		listView.setAdapter(adapter);
		listView.setBackgroundResource(R.drawable.button);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				switch (arg2) {
				case 0:
					final MaterialDialog dialog = new MaterialDialog(
							getActivity());
					LinearLayout layout = new LinearLayout(getActivity());
					layout.setLayoutParams(new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT));
					layout.setOrientation(LinearLayout.VERTICAL);
					for (i = 0; i < 4; i++) {
						final TextView textView = new TextView(getActivity());
						LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT);
						textView.setLayoutParams(layoutParams);
						// layoutParams.setMargins(0, 18, 0, 18);
						textView.setPadding(0, 24, 0, 24);
						textView.setText((int) (Math.pow(2, i + 1)) + "小时");
						textView.setId((i + 13));
						textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
						textView.setGravity(Gravity.CENTER);
						textView.setBackgroundResource(R.drawable.button);
						textView.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								sharedPreferences = getActivity()
										.getSharedPreferences("citys",
												Context.MODE_PRIVATE);
								editor = sharedPreferences.edit();
								switch (textView.getId()) {
								case 13:
									Log.v("设置为两小时", "设置为两小时");
									editor.putInt("time", 7200000);
									break;
								case 14:
									Log.v("设置为si小时", "设置为si小时");
									editor.putInt("time", 14400000);
									break;
								case 15:
									Log.v("设置为liu小时", "设置为liu小时");
									editor.putInt("time", 28800000);
									break;
								case 16:
									Log.v("设置为ba小时", "设置为ba小时");
									editor.putInt("time", 57600000);
									break;
								}
								editor.commit();
								Intent intent = new Intent(getActivity(),
										WidgetService.class);
								getActivity().stopService(intent);
								getActivity().startService(intent);
								dialog.dismiss();
							}
						});
						layout.addView(textView);
					}
					dialog.setContentView(layout).setTitle("刷新频率")
							.setNegativeButton("取消", new OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									dialog.dismiss();
								}
							}).show();
					Log.v("已经存储了", "已经存储了");
					break;
				case 1:
					final MaterialDialog dialog2 = new MaterialDialog(
							getActivity());
					LinearLayout layout2 = new LinearLayout(getActivity());
					layout2.setLayoutParams(new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT));
					layout2.setOrientation(LinearLayout.VERTICAL);
					dialog2.setContentView(layout2).setTitle("开关")
							.setNegativeButton("取消", new OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									dialog2.dismiss();
								}
							}).show();
					for (i = 0; i < 2; i++) {
						final TextView textView = new TextView(getActivity());
						LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT);
						textView.setLayoutParams(layoutParams);
						textView.setPadding(0, 24, 0, 24);
						if (i == 0) {
							textView.setText("打开");
						}
						if (i == 1) {
							textView.setText("关闭");
						}
						textView.setId(i + 17);
						textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
						textView.setGravity(Gravity.CENTER);
						textView.setBackgroundResource(R.drawable.button);
						textView.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								sharedPreferences = getActivity()
										.getSharedPreferences("citys",
												Context.MODE_PRIVATE);
								editor = sharedPreferences.edit();
								switch (textView.getId()) {
								case 17:
									Log.e("设置为开启", "设置为开启");
									editor.putInt("advice", 1);
									editor.commit();
									break;
								case 18:
									Log.e("设置为关闭", "设置为关闭");
									editor.putInt("advice", 0);
									editor.commit();
									break;
								}
								dialog2.dismiss();
							}
						});
						layout2.addView(textView);
					}
					break;
				case 2:
					final MaterialDialog dialog3 = new MaterialDialog(
							getActivity());
					LinearLayout layout3 = new LinearLayout(getActivity());
					layout3.setLayoutParams(new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT));
					layout3.setOrientation(LinearLayout.VERTICAL);
					for (i = 0; i < 2; i++) {
						final TextView textView = new TextView(getActivity());
						LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT);
						textView.setLayoutParams(layoutParams);
						textView.setPadding(0, 24, 0, 24);
						if (i == 0) {
							textView.setText("打开");
						}
						if (i == 1) {
							textView.setText("关闭");
						}
						textView.setId(i + 19);
						textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
						textView.setGravity(Gravity.CENTER);
						textView.setBackgroundResource(R.drawable.button);
						textView.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								sharedPreferences = getActivity()
										.getSharedPreferences("citys",
												Context.MODE_PRIVATE);
								editor = sharedPreferences.edit();
								switch (textView.getId()) {
								case 19:
									Log.e("设置为开启", "设置为开启");
									editor.putInt("bar", 1);
									editor.commit();
									break;
								case 20:
									Log.e("设置为关闭", "设置为关闭");
									editor.putInt("bar", 0);
									editor.commit();
									break;
								}
								dialog3.dismiss();
							}
						});
						layout3.addView(textView);
					}
					dialog3.setContentView(layout3).setTitle("开关")
							.setNegativeButton("取消", new OnClickListener() {
								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									dialog3.dismiss();
								}
							}).show();
					break;
				case 3:
					final MaterialDialog dialog4 = new MaterialDialog(
							getActivity());
					LinearLayout layout4 = new LinearLayout(getActivity());
					layout4.setLayoutParams(new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT));
					layout4.setOrientation(LinearLayout.VERTICAL);
					for (i = 0; i < 2; i++) {
						final TextView textView = new TextView(getActivity());
						LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT);
						textView.setLayoutParams(layoutParams);
						textView.setPadding(0, 24, 0, 24);
						if (i == 0) {
							textView.setText("打开");
						}
						if (i == 1) {
							textView.setText("关闭");
						}
						textView.setId(i + 21);
						textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
						textView.setGravity(Gravity.CENTER);
						textView.setBackgroundResource(R.drawable.button);
						textView.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								sharedPreferences = getActivity()
										.getSharedPreferences("citys",
												Context.MODE_PRIVATE);
								editor = sharedPreferences.edit();
								switch (textView.getId()) {
								case 21:
									Log.e("设置为开启", "设置为开启");
									editor.putInt("morecolor", 1);
									editor.commit();
									break;
								case 22:
									Log.e("设置为关闭", "设置为关闭");
									editor.putInt("morecolor", 0);
									editor.commit();
									break;
								}
								dialog4.dismiss();
							}
						});

						layout4.addView(textView);
					}
					dialog4.setContentView(layout4).setTitle("开关")
							.setNegativeButton("取消", new OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									dialog4.dismiss();
								}
							}).show();
					break;

				case 4:
					sharedPreferences = getActivity().getSharedPreferences(
							"citys", Context.MODE_PRIVATE);
					final MaterialDialog dialog5 = new MaterialDialog(
							getActivity());
					LinearLayout layout5 = new LinearLayout(getActivity());
					layout5.setLayoutParams(new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT));
					layout5.setOrientation(LinearLayout.VERTICAL);
					final EditText editText = new EditText(getActivity());
					editText.setLayoutParams(new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));
					editText.setText(sharedPreferences.getString("packagename",
							"com.google.android.deskclock"));
					editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
					TextView textView = new TextView(getActivity());
					textView.setLayoutParams(new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));
					textView.setText("修改过后需要移除小部件之后重新添加才能生效。");
					textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
					layout5.addView(textView);
					layout5.addView(editText);
					dialog5.setContentView(layout5).setTitle("输入包名")
							.setNegativeButton("取消", new OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									dialog5.dismiss();
								}
							}).setPositiveButton("确定", new OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									sharedPreferences = getActivity()
											.getSharedPreferences("citys",
													Context.MODE_PRIVATE);
									editor = sharedPreferences.edit();
									editor.putString("packagename", editText
											.getText().toString());
									editor.commit();
									dialog5.dismiss();
								}
							}).setText("修改之后您可能需要重新添加小部件").show();
					break;
				default:
					break;
				}

			}
		});
		sharedPreferences = getActivity().getSharedPreferences("citys",
				Context.MODE_PRIVATE);
		Log.e("刷新时间", String.valueOf(sharedPreferences.getInt("time", 0)));
		Log.e("开关呢", String.valueOf(sharedPreferences.getInt("advice", 3)));
		Log.e("导航栏呢", String.valueOf(sharedPreferences.getInt("bar", 3)));
		Log.e("颜色呢", String.valueOf(sharedPreferences.getInt("morecolor", 3)));
		Log.e("包名呢", String.valueOf(sharedPreferences.getString("packagename",
				"com.google.android.deskclock")));

	}

	@Override
	public void onResume() {
		super.onResume();
		getView().setFocusableInTouchMode(true);
		getView().requestFocus();
		getView().setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (event.getAction() == KeyEvent.ACTION_UP
						&& keyCode == KeyEvent.KEYCODE_BACK) {
					fragmentCallBack.callbackSettingFragment(null);
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public void onAttach(Activity activity) {// 启动Fragment调用
		// TODO Auto-generated method stub
		super.onAttach(activity);
		fragmentCallBack = (MainActivity) activity;// 将Activity实例赋给fragmentCallBack
	}
}
