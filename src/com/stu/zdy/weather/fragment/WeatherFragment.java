package com.stu.zdy.weather.fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;

import com.stu.zdy.weather.activity.MainActivity;
import com.stu.zdy.weather.db.DBmanager;
import com.stu.zdy.weather.floatingActionButton.FloatingActionButton;
import com.stu.zdy.weather.floatingActionButton.ObservableScrollView;
import com.stu.zdy.weather.floatingActionButton.ScrollDirectionListener;
import com.stu.zdy.weather.net.GetInternetInfo;
import com.stu.zdy.weather.net.JsonDataAnalysisByHe;
import com.stu.zdy.weather.object.FragmentCallBack;
import com.stu.zdy.weather.util.ScreenUtils;
import com.stu.zdy.weather_sample.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import net.sf.json.JSONObject;

@SuppressLint("SdCardPath")
public class WeatherFragment extends Fragment {
	private int height, width;
	private FragmentCallBack fragmentCallBack = null;
	private SwipeRefreshLayout swipeRefreshLayout;
	private ObservableScrollView scrollView;
	private RelativeLayout weatherMainCardLayout;// 当前天气布局
	private ImageView weatherPicture;
	private HorizontalScrollView horizontalScrollView;
	private LinearLayout forecast3hLayout;
	private TextView currentTemperature;
	private TextView todayWeatherDetail;
	private FutureWeatherDrawable weatherDrawable;
	private LinearLayout futureWeatherChartLayout;
	private LinearLayout futureWeatherWeekLayout;
	private LinearLayout statusWeatherLayout;
	private FloatingActionButton addButton;
	private TextView[] futureWeatherDatas = { null, null, null, null, null };
	private TextView[] futureWeatherWeeks = { null, null, null, null, null };
	private String[] weekNameStrings = { "周日", "周一", "周二", "周三", "周四", "周五",
			"周六" };
	private int currentTemperatureNumber = 30;
	int[] temperatureDatas = { 30, 30, 30, 30, 30, 23, 23, 23, 23, 23 };
	int[] monthDays = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	private int drawableWidth;

	// @Override
	@Override
	@SuppressWarnings("unused")
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		height = dm.heightPixels;
		width = (int) (dm.widthPixels * 0.9528);
		SharedPreferences sharedPreferences = getActivity()
				.getSharedPreferences("citys", Context.MODE_PRIVATE);
		initUI();
		if (-1 != GetInternetInfo.getConnectedType(getActivity())) {
			GetInfomationFromNetInFragment getInfomationFromNet = new GetInfomationFromNetInFragment();
			getInfomationFromNet.execute(getArguments().getString("city"));
		} else {
			File file = new File("/data/data/com.stu.zdy.weather_sample/files");
			String[] filelist = file.list();
			for (String string : filelist) {
				Log.v("文件列表", string);
				if (string.equals(getArguments().getString("city"))) {
					JSONObject jsonObject = JSONObject
							.fromObject(read(getArguments().getString("city")));
					Log.v("取出的文件数据", jsonObject.toString());
					Bundle bundle = new JsonDataAnalysisByHe(jsonObject)
							.getBundle();
					if (bundle.getString("status").equals("ok")) {
						bildView(bundle);
					} else {
						Toast.makeText(getActivity(), "服务器出错",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
		return swipeRefreshLayout;
	}

	private void initUI() {
		int cardwidth = width;
		int cardheightPicture = cardwidth / 16 * 9;
		swipeRefreshLayout = new SwipeRefreshLayout(getActivity());// 刷新部件，底级布局
		swipeRefreshLayout.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		swipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_blue_light,
				android.R.color.holo_red_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_green_light);
		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (GetInternetInfo.getConnectedType(getActivity()) != -1) {
					GetInfomationFromNetInFragment getInfomationFromNet = new GetInfomationFromNetInFragment();
					getInfomationFromNet.execute(getArguments().getString(
							"city"));
					Toast.makeText(getActivity(), "正在刷新", Toast.LENGTH_SHORT)
							.show();
				} else {
					swipeRefreshLayout.setRefreshing(false);
				}
			}
		});
		scrollView = new ObservableScrollView(getActivity());// 滚动视图，一级布局
		scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		scrollView.setFillViewport(true);
		weatherMainCardLayout = new RelativeLayout(getActivity());// 天气显示主布局，二级布局
		weatherMainCardLayout.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		weatherMainCardLayout
				.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
		weatherPicture = new ImageView(getActivity());// 天气背景，三级布局
		RelativeLayout.LayoutParams weatherPictureLayoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, cardheightPicture);
		weatherPictureLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
		weatherPicture.setLayoutParams(weatherPictureLayoutParams);
		weatherPicture.setScaleType(ScaleType.CENTER_CROP);
		weatherPicture.setId(7);
		currentTemperature = new TextView(getActivity());// 实况温度文字布局，三级布局
		todayWeatherDetail = new TextView(getActivity());// 实况天气详细信息布局，三级布局
		RelativeLayout.LayoutParams currentTemperatureParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		currentTemperatureParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
		currentTemperatureParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
		currentTemperatureParams.setMargins(0, width / 30, width / 30, 0);
		currentTemperature.setLayoutParams(currentTemperatureParams);
		currentTemperature.setGravity(Gravity.LEFT);
		currentTemperature.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				cardheightPicture / 4);
		currentTemperature.setTextColor(Color.rgb(245, 245, 245));
		currentTemperature.setText("-1300");
		currentTemperature.setId(8);
		RelativeLayout.LayoutParams cityParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		cityParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
		cityParams.addRule(RelativeLayout.ALIGN_BOTTOM, weatherPicture.getId());
		cityParams.setMargins(0, 0, width / 30, width / 30);

		todayWeatherDetail.setGravity(Gravity.RIGHT);
		todayWeatherDetail.setLayoutParams(cityParams);
		todayWeatherDetail.setTextColor(getResources().getColor(
				android.R.color.white));
		todayWeatherDetail.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				cardwidth / 40);
		horizontalScrollView = new HorizontalScrollView(getActivity());// 水平滚动条，三级布局
		RelativeLayout.LayoutParams horizontalLayoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		horizontalLayoutParams.addRule(RelativeLayout.BELOW,// 位于今日天气以下
				weatherPicture.getId());
		horizontalScrollView.setLayoutParams(horizontalLayoutParams);
		horizontalScrollView.setId(9);
		// horizontalScrollView.setHorizontalScrollBarEnabled(false);
		horizontalScrollView.setFillViewport(true);
		forecast3hLayout = new LinearLayout(getActivity());// 三小时天气预报，四级布局
		forecast3hLayout.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		forecast3hLayout.setOrientation(LinearLayout.HORIZONTAL);
		horizontalScrollView.addView(forecast3hLayout);
		futureWeatherWeekLayout = new LinearLayout(getActivity());// 星期与未来五日高温布局，三级布局
		RelativeLayout.LayoutParams futureWeatherWeekLayoutParams = new RelativeLayout.LayoutParams(
				width, LayoutParams.WRAP_CONTENT);
		futureWeatherWeekLayoutParams.setMargins(0, height / 90, 0, 0);
		futureWeatherWeekLayoutParams.addRule(RelativeLayout.BELOW,
				horizontalScrollView.getId());// 位于三小时预报以下
		futureWeatherWeekLayout.setLayoutParams(futureWeatherWeekLayoutParams);
		for (int i = 0; i < 5; i++) {
			TextView futureWeatherWeek = new TextView(getActivity());// 星期与未来五日布局，四级布局
			futureWeatherWeeks[i] = futureWeatherWeek;
			futureWeatherWeek.setLayoutParams(new LayoutParams(width / 5,
					LayoutParams.WRAP_CONTENT));
			futureWeatherWeek.setGravity(Gravity.CENTER);
			futureWeatherWeek.setTextSize(TypedValue.COMPLEX_UNIT_PX,
					width / 30);
			futureWeatherWeekLayout.addView(futureWeatherWeek);
		}
		futureWeatherWeekLayout.setId(10);
		weatherDrawable = new FutureWeatherDrawable(getActivity(),
				temperatureDatas, 0);// 曲线图控件，三级布局
		drawableWidth = width / 2;
		RelativeLayout.LayoutParams drawableParams = new RelativeLayout.LayoutParams(
				cardwidth, drawableWidth);
		drawableParams.addRule(RelativeLayout.BELOW,
				futureWeatherWeekLayout.getId());// 位于周以下
		drawableParams.setMargins(0, 0, 0, 0);
		weatherDrawable.setLayoutParams(drawableParams);
		weatherDrawable.setId(11);
		futureWeatherChartLayout = new LinearLayout(getActivity());// 未来天气与低温布局，三级布局
		RelativeLayout.LayoutParams futureWeatherChartLayoutParams = new RelativeLayout.LayoutParams(
				width, LayoutParams.WRAP_CONTENT);
		futureWeatherChartLayoutParams.setMargins(0, 0, 0, height / 60);
		futureWeatherChartLayoutParams.addRule(RelativeLayout.BELOW,
				weatherDrawable.getId());
		futureWeatherChartLayout
				.setLayoutParams(futureWeatherChartLayoutParams);
		futureWeatherChartLayout.setId(12);
		for (int i = 0; i < 5; i++) {
			TextView futureWeatherData = new TextView(getActivity());// 未来天气与低温文字布局，四级布局
			futureWeatherDatas[i] = futureWeatherData;
			futureWeatherData.setLayoutParams(new LayoutParams(width / 5,
					LayoutParams.WRAP_CONTENT));
			futureWeatherData.setGravity(Gravity.CENTER);
			futureWeatherData.setTextSize(TypedValue.COMPLEX_UNIT_PX,
					width / 30);
			futureWeatherData.setMaxLines(2);
			futureWeatherChartLayout.addView(futureWeatherData);
		}
		addButton = new FloatingActionButton(getActivity());// 浮动按钮，三级布局
		RelativeLayout.LayoutParams addButtonLayoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		addButtonLayoutParams.setMargins(0, 0, 16, 16);
		addButton.setLayoutParams(addButtonLayoutParams);
		addButtonLayoutParams.rightMargin = ScreenUtils
				.getScreenWidth(getActivity()) / 20;
		addButtonLayoutParams.bottomMargin = ScreenUtils
				.getScreenWidth(getActivity()) / 20;
		addButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
				RelativeLayout.TRUE);
		addButtonLayoutParams.addRule(RelativeLayout.BELOW,
				futureWeatherChartLayout.getId());
		addButton.setImageResource(R.drawable.ic_add_white_24dp);
		addButton.setColorNormal(getResources().getColor(
				android.R.color.holo_orange_light));
		addButton.setColorPressed(getResources().getColor(
				android.R.color.holo_orange_dark));
		addButton.attachToScrollView(scrollView, new ScrollDirectionListener() {

			@Override
			public void onScrollUp() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScrollDown() {
				// TODO Auto-generated method stub

			}
		}, new ObservableScrollView.OnScrollChangedListener() {

			@Override
			public void onScrollChanged(ScrollView who, int l, int t, int oldl,
					int oldt) {
				// TODO Auto-generated method stub

			}
		});
		addButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				fragmentCallBack.callbackMainFragment(null, 0);
			}
		});
		statusWeatherLayout = new LinearLayout(getActivity());// 天气建议布局，三级布局
		RelativeLayout.LayoutParams statusWeatherLayoutParams = new android.widget.RelativeLayout.LayoutParams(
				width / 5 * 4, LayoutParams.WRAP_CONTENT);
		statusWeatherLayoutParams.setMargins(width / 20, 0, 0, width / 20);
		statusWeatherLayoutParams.addRule(RelativeLayout.BELOW,
				futureWeatherChartLayout.getId());
		statusWeatherLayout.setOrientation(LinearLayout.VERTICAL);
		statusWeatherLayout.setLayoutParams(statusWeatherLayoutParams);
		int[] icons = { R.drawable.ic_favorite_outline_grey600_24dp,
				R.drawable.ic_local_car_wash_grey600_24dp,
				R.drawable.ic_directions_walk_grey600_24dp,
				R.drawable.ic_directions_bike_grey600_24dp };
		LinearLayout item1layout = new LinearLayout(getActivity());// 天气布局部件1，四级布局
		item1layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		item1layout.setOrientation(LinearLayout.HORIZONTAL);
		ImageView icon = new ImageView(getActivity());// 天气布局部件1图片，五级布局
		icon.setLayoutParams(new LayoutParams(width * 22 / 360,
				width * 30 / 360));
		icon.setScaleType(ScaleType.FIT_CENTER);
		icon.setImageResource(icons[0]);
		TextView text = new TextView(getActivity());// 天气布局部件1文字，五级布局
		text.setLayoutParams(new LayoutParams(width / 5 * 4 - width * 22 / 360
				- width / 20, LayoutParams.WRAP_CONTENT));
		text.setMinHeight(width * 30 / 360);
		text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		text.setPadding(width / 40, 0, 0, 0);
		text.setGravity(Gravity.LEFT);

		item1layout.setPadding(0, 0, 0, 2);
		LinearLayout item2layout = new LinearLayout(getActivity());// 天气建议部件2，四级布局
		item2layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		item2layout.setOrientation(LinearLayout.HORIZONTAL);
		item2layout.setGravity(Gravity.CENTER_VERTICAL);
		for (int i = 0; i < icons.length - 1; i++) {
			ImageView icon2 = new ImageView(getActivity());// 天气建议部件2图片，五级布局
			icon2.setLayoutParams(new LayoutParams(width * 22 / 360,
					width * 22 / 360));
			icon2.setScaleType(ScaleType.FIT_CENTER);
			icon2.setImageResource(icons[i + 1]);
			TextView text2 = new TextView(getActivity());// 天气建议部件2文字，五级布局
			text2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					width * 26 / 360));
			text2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
			text2.setPadding(width / 30, 0, width / 30, 0);
			text2.setGravity(Gravity.CENTER);
			item2layout.addView(icon2);
			item2layout.addView(text2);
		}
		item1layout.addView(icon);
		item1layout.addView(text);
		statusWeatherLayout.addView(item1layout);
		statusWeatherLayout.addView(item2layout);
		weatherMainCardLayout.addView(weatherPicture);
		weatherMainCardLayout.addView(currentTemperature);
		weatherMainCardLayout.addView(todayWeatherDetail);
		weatherMainCardLayout.addView(horizontalScrollView);
		weatherMainCardLayout.addView(futureWeatherWeekLayout);
		weatherMainCardLayout.addView(weatherDrawable);
		weatherMainCardLayout.addView(futureWeatherChartLayout);
		weatherMainCardLayout.addView(statusWeatherLayout);
		weatherMainCardLayout.addView(addButton);
		scrollView.addView(weatherMainCardLayout);
		swipeRefreshLayout.addView(scrollView);
	}

	public void bildView(Bundle bundle) {
		ArrayList<String> item1 = bundle.getStringArrayList("item1");
		ArrayList<String> item2 = bundle.getStringArrayList("item2");
		ArrayList<String> item3 = bundle.getStringArrayList("item3");
		ArrayList<String> item4 = bundle.getStringArrayList("item4");
		Calendar calendar = Calendar.getInstance();
		currentTemperatureNumber = Integer.valueOf(item1.get(6));
		currentTemperature.setText(item1.get(6) + "°");
		todayWeatherDetail.setText(item1.get(0) + "\n"
				+ item1.get(2).substring(11, 16) + "刷新" + "\n" + item1.get(3)
				+ "\n" + "湿度:" + item1.get(4) + "°" + "\n" + item1.get(5)
				+ "\n" + "紫外线强度:" + item1.get(8));
		for (int i = 0; i < 10; i++) {
			temperatureDatas[i] = Integer.valueOf(item2.get(i));
		}

		int j = 0;
		SharedPreferences sharedPreferences = getActivity()
				.getSharedPreferences("citys", Context.MODE_PRIVATE);
		for (int i = calendar.get(Calendar.DAY_OF_WEEK); i < calendar
				.get(Calendar.DAY_OF_WEEK) + 5; i++) {// 绑定星期
			futureWeatherWeeks[j]
					.setText(weekNameStrings[i > 7 ? i - 8 : i - 1] + "\n"
							+ temperatureDatas[j + 5] + "°");
			if (sharedPreferences.getInt("morecolor", 0) == 1) {
				SpannableStringBuilder builder = new SpannableStringBuilder(
						futureWeatherWeeks[j].getText().toString());
				ForegroundColorSpan span = new ForegroundColorSpan(
						changeColorByTemper(temperatureDatas[j + 5]));
				builder.setSpan(span, 2, futureWeatherWeeks[j].getText()
						.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				futureWeatherWeeks[j].setText(builder);
			}

			j++;
		}
		for (int i = 0; i < 5; i++) {
			futureWeatherDatas[i]
					.setText(temperatureDatas[i]
							+ "°"
							+ "\n"
							+ (item2.get(i * 2 + 15).equals(
									item2.get(i * 2 + 16)) ? item2
									.get(i * 2 + 16) : item2.get(i * 2 + 15)
									+ "转" + item2.get(i * 2 + 16)));
			Log.v("当前天气",
					temperatureDatas[i]
							+ "°"
							+ "\n"
							+ (item2.get(i * 2 + 15).equals(
									item2.get(i * 2 + 16)) ? item2
									.get(i * 2 + 16) : item2.get(i * 2 + 15)
									+ "转" + item2.get(i * 2 + 16)));
			if (sharedPreferences.getInt("morecolor", 0) == 1) {
				SpannableStringBuilder builder = new SpannableStringBuilder(
						futureWeatherDatas[i].getText().toString());
				ForegroundColorSpan span = new ForegroundColorSpan(
						changeColorByTemper(temperatureDatas[i]));
				builder.setSpan(span, 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				futureWeatherDatas[i].setText(builder);
			}
		}
		Log.v("当前页面天气状态代码", item1.get(7));
		switch (Integer.valueOf(item1.get(7))) {
		case 100:
		case 102:
		case 103:
			weatherPicture.setImageDrawable(getResources().getDrawable(
					R.drawable.sunny));
			break;
		case 101:
			weatherPicture.setImageDrawable(getResources().getDrawable(
					R.drawable.cloudy));
			break;
		case 104:
			weatherPicture.setImageDrawable(getResources().getDrawable(
					R.drawable.overcast));
			break;
		case 300:
		case 301:
		case 302:
		case 303:
		case 304:
		case 305:
		case 306:
		case 307:
		case 308:
		case 309:
		case 310:
		case 311:
		case 312:
		case 313:
			weatherPicture.setImageDrawable(getResources().getDrawable(
					R.drawable.rain));
			break;
		case 400:
		case 401:
		case 402:
		case 403:
		case 404:
		case 405:
		case 406:
		case 407:
			weatherPicture.setImageDrawable(getResources().getDrawable(
					R.drawable.snow));
			break;
		case 500:
		case 501:
		case 502:
			weatherPicture.setImageDrawable(getResources().getDrawable(
					R.drawable.fog));
			break;
		}
		weatherDrawable.invalidate();
		if (1 == sharedPreferences.getInt("advice", 1)) {
			LinearLayout itemLayout = (LinearLayout) statusWeatherLayout
					.getChildAt(0);
			((TextView) itemLayout.getChildAt(1)).setText(item4.get(0));
			LinearLayout itemLayout2 = (LinearLayout) statusWeatherLayout
					.getChildAt(1);
			((TextView) itemLayout2.getChildAt(1)).setText(item4.get(1));
			((TextView) itemLayout2.getChildAt(3)).setText(item4.get(2));
			((TextView) itemLayout2.getChildAt(5)).setText(item4.get(3));
		}
		if (0 == sharedPreferences.getInt("advice", 1)) {
			statusWeatherLayout.removeAllViews();
		}
		forecast3hLayout.removeAllViews();
		for (int i = 0; i < item3.size() / 2; i++) {// 三小时天气预报部分
			LinearLayout gridLayout = new LinearLayout(getActivity());
			LinearLayout.LayoutParams gridLayoutParams = new LinearLayout.LayoutParams(
					width / 5, LayoutParams.WRAP_CONTENT);
			gridLayout.setLayoutParams(gridLayoutParams);
			gridLayout.setGravity(Gravity.CENTER_HORIZONTAL);
			gridLayout.setOrientation(LinearLayout.VERTICAL);
			TextView date = new TextView(getActivity());
			date.setLayoutParams(new LinearLayout.LayoutParams(width / 5,
					LayoutParams.WRAP_CONTENT));
			date.setText(item3.get(i).substring(11, 13) + "时");
			date.setGravity(Gravity.CENTER);
			TextView temperView = new TextView(getActivity());
			temperView.setLayoutParams(new LayoutParams(width / 5,
					LayoutParams.WRAP_CONTENT));
			temperView.setText(item3.get(item3.size() / 2 + i) + "°");
			temperView.setGravity(Gravity.CENTER);
			temperView.setTextColor(changeColorByTemper(Integer
					.valueOf(temperView.getText().toString()
							.substring(0, temperView.getText().length() - 1))));
			gridLayout.addView(date);
			gridLayout.addView(temperView);
			forecast3hLayout.addView(gridLayout);
		}
	}

	private int changeColorByTemper(int temper) {
		int red = 0, green = 0, blue = 0;
		int hotIndex = temper;
		hotIndex = hotIndex > 40 ? 40 : hotIndex;
		hotIndex = hotIndex < 0 ? 0 : hotIndex;
		if (hotIndex <= 32 || hotIndex <= 24) {
			green = (32 - hotIndex) * 12;
			red = 255;
			blue = 0;
		}
		if (hotIndex <= 24 || hotIndex <= 16) {
			red = 255;
			green = 96 + (24 - hotIndex) * 12;
			blue = 0;
		}
		if (hotIndex <= 16 || hotIndex <= 8) {

			red = 0;
			blue = 255;
			green = 192 - (16 - hotIndex) * 24;
		}
		if (hotIndex <= 8) {
			red = (8 - hotIndex) * 24;
			green = 0;
			blue = 255;
		}
		return Color.rgb(red, green, blue);
	}

	@SuppressWarnings("unused")
	private void changeWeatherPicture(int kind, View view) {
		switch (kind) {
		case 0:
			((ImageView) view).setImageResource(R.drawable.sunny_pencil_grey);
			break;
		case 1:
			((ImageView) view).setImageResource(R.drawable.cloudy_pencil_grey);
			break;
		case 2:
			((ImageView) view)
					.setImageResource(R.drawable.overcast_pencil_grey);
			break;
		case 4:
		case 5:
			((ImageView) view).setImageResource(R.drawable.storm_pencil_grey);
			break;
		case 3:
		case 7:
		case 8:
		case 9:
		case 10:
		case 11:
		case 12:
		case 21:
		case 22:
		case 23:
		case 24:
		case 25:
			((ImageView) view).setImageResource(R.drawable.rain_pencil_grey);
			break;
		case 13:
		case 14:
		case 15:
		case 16:
		case 17:
		case 26:
		case 27:
		case 28:
			((ImageView) view).setImageResource(R.drawable.snow_pencil_grey);
			break;
		}
	}

	private void write(String cityName, String data) {// 写入文件
		// TODO Auto-generated method stub
		try {
			FileOutputStream fileOutputStream = getActivity().openFileOutput(
					cityName, Context.MODE_PRIVATE);
			PrintStream printStream = new PrintStream(fileOutputStream);
			printStream.print(data);
			printStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String read(String cityName) {// 读取文件
		// TODO Auto-generated method stub
		try {
			FileInputStream fileInputStream = getActivity().openFileInput(
					cityName);
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

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		fragmentCallBack = (MainActivity) activity;
	}

	/**
	 * 三种泛型类型分别代表“启动任务执行的输入参数” “后台任务执行的进度”、“后台计算结果的类型
	 * 
	 * @author Zdy
	 */
	class GetInfomationFromNetInFragment extends
			AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			Log.d("Fragment+doInBackground", "Fragment+doInBackground");
			DBmanager dbHelper = new DBmanager(getActivity());
			dbHelper.openDatabase();
			dbHelper.closeDatabase();
			String httpUrl = "https://api.heweather.com/x3/weather?cityid="
					+ dbHelper.getIdByCityName(params[0])
					+ "&key=57efa20515e94db68ae042319463dba4";
			String jsonResult = GetInternetInfo.request(httpUrl);
			write(params[0], jsonResult);
			return jsonResult;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			Log.d("Fragment+onPostExecute", "Fragment+onPostExecute");
			JSONObject jsonObject = JSONObject.fromObject(result);
			Bundle bundle = new Bundle();
			bundle = new JsonDataAnalysisByHe(jsonObject).getBundle();
			if (bundle.getString("status").equals("ok")) {
				bildView(bundle);
				swipeRefreshLayout.setRefreshing(false);
				bundle.clear();
				bundle.putString("city",
						jsonObject.getJSONArray("HeWeather data service 3.0")
								.getJSONObject(0).getJSONObject("basic")
								.getString("city"));
				fragmentCallBack.callbackMainFragment(bundle, 2);
			} else {
				Toast.makeText(getActivity(), "服务器出错", Toast.LENGTH_SHORT)
						.show();
			}
			super.onPostExecute(result);
		}
	}

	class FutureWeatherDrawable extends View {
		Paint paint;

		public FutureWeatherDrawable(Context context, int[] temperatureDatas,
				int color) {
			super(context);
			paint = new Paint(); // 设置一个笔刷大小是3的黄色的画笔
			paint.setAntiAlias(true);
			paint.setFlags(Paint.ANTI_ALIAS_FLAG);
			paint.setStrokeJoin(Paint.Join.BEVEL);
			paint.setStrokeCap(Paint.Cap.ROUND);
			paint.setStrokeWidth(width / 160);
			paint.setTextSize(width / 30);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onDraw(Canvas canvas) {
			// TODO Auto-generated method stub
			super.onDraw(canvas);
			int maxNumber = temperatureDatas[0], minNumber = temperatureDatas[0];
			for (int i = 0; i < 10; i++) {
				if (maxNumber < temperatureDatas[i]) {
					maxNumber = temperatureDatas[i];
				}
				if (minNumber > temperatureDatas[i]) {
					minNumber = temperatureDatas[i];
				}
			}
			// minNumber = (minNumber * 8 + maxNumber + 2) / 10;
			int height = (int) (drawableWidth * 0.9);// 可用于绘图区域
			int length = (height) / (maxNumber - minNumber);// 各区间长度
			int temper = Integer.valueOf(currentTemperature.getText()
					.toString()
					.substring(0, currentTemperature.getText().length() - 1));
			if (temper > 30) {
				paint.setColor(Color.rgb(219, 68, 55));
			} else if (temper > 24) {
				paint.setColor(Color.rgb(239, 108, 0));
			} else if (temper > 16) {
				paint.setColor(Color.rgb(104, 159, 56));
			} else if (temper > 10) {
				paint.setColor(Color.rgb(98, 175, 255));
			} else if (temper == -130) {
				paint.setColor(Color.rgb(245, 245, 245));
			} else {
				paint.setColor(Color.rgb(28, 86, 255));
			}

			paint.setAlpha(96);
			for (int i = 0; i < 4; i++) {// 画低温线
				canvas.drawLine(
						(i * 2 + 1) * (width) / 10 - width / 100,
						(int) ((height / 0.9)
								- (temperatureDatas[i] - minNumber) * length - drawableWidth * 0.05),
						((i + 1) * 2 + 1) * (width) / 10 - width / 100,
						(int) ((height / 0.9)
								- (temperatureDatas[i + 1] - minNumber)
								* length - drawableWidth * 0.05), paint);
			}
			for (int i = 5; i < 9; i++) {// 画高温线
				canvas.drawLine(
						(width) / 10 * ((i - 5) * 2 + 1) - width / 100,
						(int) ((height / 0.9)
								- (temperatureDatas[i] - minNumber) * length - drawableWidth * 0.05),
						(width) / 10 * ((i - 4) * 2 + 1) - width / 100,
						(int) ((height / 0.9)
								- (temperatureDatas[i + 1] - minNumber)
								* length - drawableWidth * 0.05), paint);
			}
			paint.setAlpha(255);
			for (int i = 0; i < 10; i++) {
				if (i >= 5) {// 高温
					if (i == 5) {
						canvas.drawCircle((width) / 10 * ((i - 5) * 2 + 1)
								- width / 100, (int) ((height / 0.9)
								- (currentTemperatureNumber - minNumber)
								* length - drawableWidth * 0.05), width / 100,
								paint);
						paint.setTextSize(width / 40);
						canvas.drawText(
								"当前温度get ! !",
								(width) / 10 * ((i - 5) * 2 + 1) - width / 100
										+ width / 60,
								(int) ((height / 0.9)
										- (currentTemperatureNumber - minNumber)
										* length - drawableWidth * 0.05)
										+ width / 200, paint);
					}
					canvas.drawCircle((width) / 10 * ((i - 5) * 2 + 1) - width
							/ 100,
							(int) ((height / 0.9)
									- (temperatureDatas[i] - minNumber)
									* length - drawableWidth * 0.05),
							width / 100, paint);// 画圆

				} else {// 低温
					canvas.drawCircle((width) / 10 * (i * 2 + 1) - width / 100,
							(int) ((height / 0.9)
									- (temperatureDatas[i] - minNumber)
									* length - drawableWidth * 0.05),
							width / 100, paint);// 画圆

				}
			}

		}
	}

}
