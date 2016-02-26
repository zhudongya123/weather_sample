package com.stu.zdy.weather.activity;

import java.util.ArrayList;
import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

import com.stu.zdy.weather.Ldrawer.ActionBarDrawerToggle;
import com.stu.zdy.weather.Ldrawer.DrawerArrowDrawable;
import com.stu.zdy.weather.db.DBManager;
import com.stu.zdy.weather.fragment.HelpFragment;
import com.stu.zdy.weather.fragment.InfoFragment;
import com.stu.zdy.weather.fragment.ManageCityFragment;
import com.stu.zdy.weather.fragment.SettingFragment;
import com.stu.zdy.weather.fragment.WeatherFragment;
import com.stu.zdy.weather.object.FragmentCallBack;
import com.stu.zdy.weather.object.MaterialDialog;
import com.stu.zdy.weather.object.MyFragmentPagerAdapter;
import com.stu.zdy.weather.service.WidgetService;
import com.stu.zdy.weather.util.FileUtils;
import com.stu.zdy.weather.util.NetWorkUtils;
import com.stu.zdy.weather.util.ScreenUtils;
import com.stu.zdy.weather_sample.R;

import android.R.integer;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.KITKAT)
public class MainActivity extends Activity implements FragmentCallBack {

	private Context mContext;
	private DrawerLayout mDrawerLayout;// 抽屉布局
	private AbsoluteLayout rootContentLayout;// 根内容布局
	private RelativeLayout drawerContentLayout;// 抽屉根布局
	private LinearLayout toolBar;
	private LinearLayout weatherLayout;
	private LinearLayout fragmentLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerArrowDrawable drawerArrow;

	private ViewPager viewPager;
	private FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;

	private SharedPreferences sharedPreferences;
	private ImageView drawerBackGround;
	private ImageView drawerWeatherPictureImageView;
	private TextView drawerTextView;
	private MaterialDialog mMaterialDialog;

	private ManageCityFragment cityFragment;
	private SettingFragment settingFragment;
	private InfoFragment infoFragment;
	private HelpFragment helpFragment;

	private JSONObject citylist;
	private int height, width;
	private int cityNumber;
	private String forWidgetString;
	private ArrayList<String> cityWeatherArrayList = new ArrayList<String>();
	private ArrayList<Fragment> fragments;
	private Hashtable<Integer, Integer> citysCurrentTemper = new Hashtable<Integer, Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		sharedPreferences = mContext.getSharedPreferences("weather_info", Context.MODE_PRIVATE);
		getActionBar().hide();
		setScreenParameter();
		fragmentManager = getFragmentManager();
		fragmentTransaction = fragmentManager.beginTransaction();
		initUI();
		initDrawer();
		fragments = new ArrayList<Fragment>();
		citylist = FileUtils.getCityList(mContext);

		try {
			if (citylist.getJSONArray("citylist").length() == 0) {// 城市列表为空
				if (NetWorkUtils.hasInternetConnection(mContext)) {// 存在网络
					showMaterialDialog();
				} else {
					Toast.makeText(mContext, "当前没有网络又没有文件缓存", Toast.LENGTH_SHORT).show();
				}
			} else {// 城市列表不为空
				initViewPager();
				if (!NetWorkUtils.hasInternetConnection(mContext)) {// 不存在网络的时候添加提示
					Toast.makeText(mContext, "当前没有网络，正在使用文件缓存", Toast.LENGTH_SHORT).show();
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// runService();
	}

	/**
	 * 设置虚拟按键透明和状态栏透明
	 */
	private void setScreenParameter() {
		height = ScreenUtils.getScreenHeight(mContext);
		width = ScreenUtils.getScreenWidth(mContext);
		height = height - ScreenUtils.getStatusHeight(mContext);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// AP>=19的时候启用透明状态栏
			Window window = getWindow();
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);// 透明状态栏
		}
		if (checkDeviceHasNavigationBar(mContext)// 根据设置选择是否透明状态栏
				&& sharedPreferences.getInt("navibar", 0) == 1) {
			Window window = getWindow();
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			height = (int) (height / 0.925);
		}
	}

	private void initViewPager() {
		// TODO Auto-generated method stub

		fragments = new ArrayList<Fragment>();
		citylist = FileUtils.getCityList(mContext);
		try {
			for (int i = 0; i < citylist.getJSONArray("citylist").length(); i++) {
				String city = (String) citylist.getJSONArray("citylist").get(i);
				WeatherFragment weatherFragment = new WeatherFragment();
				Bundle bundle = new Bundle();
				bundle.putString("city", city);// 将城市名称传递给fragment
				bundle.putInt("index", i);
				weatherFragment.setArguments(bundle);
				fragments.add(weatherFragment);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(fragmentManager, fragments);
		viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(4);
		if (cityNumber >= 4) {
			Toast.makeText(mContext, "城市太多啦！为了不让机器造成卡顿，请自行左右滑动", Toast.LENGTH_SHORT).show();
		}
		// adapter.notifyDataSetChanged();
		setViewListener();
	}

	private void initUI() {
		mDrawerLayout = new DrawerLayout(mContext);// 抽屉布局
		mDrawerLayout.setLayoutParams(new DrawerLayout.LayoutParams(LayoutParams.MATCH_PARENT, height));
		rootContentLayout = new AbsoluteLayout(this);// 主内容布局
		rootContentLayout
				.setLayoutParams(new DrawerLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		rootContentLayout.setId(1);
		drawerContentLayout = new RelativeLayout(mContext);// 抽屉内容区域布局
		drawerContentLayout.setBackgroundColor(Color.rgb(255, 255, 255));
		DrawerLayout.LayoutParams drawerContentLayoutParams = new DrawerLayout.LayoutParams(width * 304 / 360,
				LayoutParams.MATCH_PARENT);
		drawerContentLayoutParams.gravity = Gravity.LEFT;
		drawerContentLayout.setLayoutParams(drawerContentLayoutParams);
		drawerBackGround = new ImageView(mContext);
		drawerBackGround.setId(2);
		RelativeLayout.LayoutParams drawerBackGroundParams = new RelativeLayout.LayoutParams(
				drawerContentLayoutParams.width, drawerContentLayoutParams.width / 16 * 9);
		drawerBackGroundParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
		drawerBackGround.setLayoutParams(drawerBackGroundParams);
		drawerBackGround.setScaleType(ScaleType.CENTER_CROP);
		drawerBackGround.setImageResource(R.drawable.middle_temper);
		drawerTextView = new TextView(mContext);
		RelativeLayout.LayoutParams drawerTextViewParams = new android.widget.RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, width * 56 / 360);
		drawerTextView.setPadding(width * 10 / 360, width * 10 / 360, 0, 0);
		drawerTextViewParams.setMargins(16 * width / 360, 0, 0, 0);
		drawerTextViewParams.addRule(RelativeLayout.ALIGN_LEFT, drawerBackGround.getId());
		drawerTextViewParams.addRule(RelativeLayout.ALIGN_BOTTOM, drawerBackGround.getId());
		drawerTextView.setLayoutParams(drawerTextViewParams);
		drawerTextView.setGravity(Gravity.LEFT);
		drawerTextView.setTextColor(Color.WHITE);
		drawerTextView.setId(3);
		drawerWeatherPictureImageView = new ImageView(mContext);
		RelativeLayout.LayoutParams drawerWeatherPictureImageViewLayoutParams = new android.widget.RelativeLayout.LayoutParams(
				width * 56 / 360, width * 56 / 360);
		drawerWeatherPictureImageView.setScaleType(ScaleType.FIT_CENTER);
		drawerWeatherPictureImageViewLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, drawerTextView.getId());
		drawerWeatherPictureImageViewLayoutParams.addRule(RelativeLayout.ABOVE, drawerTextView.getId());
		drawerWeatherPictureImageView.setLayoutParams(drawerWeatherPictureImageViewLayoutParams);
		AssetManager mgr = getAssets();// 得到AssetManager
		Typeface tf = Typeface.createFromAsset(mgr, "fonts/Roboto-Medium.ttf");// 根据路径得到Typeface
		drawerContentLayout.addView(drawerBackGround);
		drawerContentLayout.addView(drawerWeatherPictureImageView);
		drawerContentLayout.addView(drawerTextView);
		initDrawerContent(tf);

		toolBar = new LinearLayout(mContext);
		toolBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, height * 56 / 640));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			toolBar.setPadding(0, ScreenUtils.getStatusHeight(mContext), 0, 0);
			toolBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					height * 56 / 640 + ScreenUtils.getStatusHeight(mContext)));
		}
		toolBar.setGravity(Gravity.CENTER_VERTICAL);
		initToolBar();
		weatherLayout = new LinearLayout(mContext);
		weatherLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, height * 584 / 640));
		weatherLayout.setOrientation(LinearLayout.VERTICAL);
		weatherLayout.setId(4);
		fragmentLayout = new LinearLayout(mContext);
		fragmentLayout.setId(5);
		fragmentLayout.setLayoutParams(new LayoutParams(ScreenUtils.getScreenWidth(mContext), height * 584 / 640));
		fragmentLayout.setOrientation(LinearLayout.VERTICAL);
		viewPager = new ViewPager(mContext);
		viewPager.setId(6);
		viewPager.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		toolBar.setX(0);
		toolBar.setY(0);
		weatherLayout.setX(0);
		weatherLayout.setY(height * 56 / 640);
		fragmentLayout.setY(height * 56 / 640);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			weatherLayout.setY(height * 56 / 640 + ScreenUtils.getStatusHeight(mContext));
			fragmentLayout.setY(height * 56 / 640 + ScreenUtils.getStatusHeight(mContext));
		}
		fragmentLayout.setX(ScreenUtils.getScreenWidth(mContext));
		// progressWheel = new ProgressWheel(mContext);
		// progressWheel.setLayoutParams(new LayoutParams(width / 8, width /
		// 8));
		// progressWheel.setBarColor(Color.rgb(85, 136, 255));
		// progressWheel.setX((float) (width / 2 * 0.9));
		// progressWheel.setY((float) (height / 2 * 1.2));
		// progressWheel.setBarWidth(6);

		// progressWheel.spin();
		rootContentLayout.addView(toolBar);
		rootContentLayout.addView(weatherLayout);
		rootContentLayout.addView(fragmentLayout);
		// rootContentLayout.addView(progressWheel);
		weatherLayout.addView(viewPager);
		mDrawerLayout.addView(rootContentLayout);
		mDrawerLayout.addView(drawerContentLayout);
		setContentView(mDrawerLayout);
	}

	private void initToolBar() {
		ImageView arrow = new ImageView(mContext);
		LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(height * 56 / 640, height * 56 / 640);
		arrow.setPadding(0, width / 12 * 360, 0, width / 12 * 360);
		arrow.setScaleType(ScaleType.CENTER);
		arrow.setImageResource(R.drawable.ic_drawer);
		arrow.setLayoutParams(arrowParams);
		arrow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
					mDrawerLayout.closeDrawer(Gravity.LEFT);
				} else {
					mDrawerLayout.openDrawer(Gravity.LEFT);
				}
			}
		});
		TextView title = new TextView(mContext);
		LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		titleParams.setMargins(width * 16 / 360, 0, 0, 0);
		title.setGravity(Gravity.LEFT);
		title.setLayoutParams(titleParams);
		title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
		title.setText("质感天气");
		title.setTextColor(Color.WHITE);
		toolBar.addView(arrow);
		toolBar.addView(title);
	}

	private void initDrawerContent(Typeface tf) {
		String[] drawerFunctionText = { "查看天气", "城市管理" };
		int[] drawerFunctionIcon = { R.drawable.ic_wb_sunny_grey600_24dp, R.drawable.ic_location_city_grey600_24dp };
		LinearLayout drawerFunctionLayout = new LinearLayout(mContext);
		drawerFunctionLayout.setOrientation(LinearLayout.VERTICAL);
		RelativeLayout.LayoutParams drawerFunctionLayoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		drawerFunctionLayoutParams.setMargins(0, height / 50, 0, 0);
		drawerFunctionLayoutParams.addRule(RelativeLayout.BELOW, drawerBackGround.getId());
		drawerFunctionLayout.setLayoutParams(drawerFunctionLayoutParams);
		for (int i = 0; i < drawerFunctionIcon.length; i++) {
			LinearLayout drawerFunctionItem = new LinearLayout(mContext);
			drawerFunctionItem.setGravity(Gravity.CENTER_VERTICAL);
			drawerFunctionItem.setOrientation(LinearLayout.HORIZONTAL);
			RelativeLayout.LayoutParams drawerItem_1Params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					width * 48 / 360);
			drawerItem_1Params.addRule(RelativeLayout.BELOW, drawerBackGround.getId());
			drawerFunctionItem.setLayoutParams(drawerItem_1Params);
			drawerItem_1Params.setMargins(0, width * 16 / 360, 0, width * 8 / 360);
			ImageView drawerFunctionItemImage = new ImageView(mContext);
			LinearLayout.LayoutParams drawerFunctionItemImageParams = new LinearLayout.LayoutParams(width * 24 / 360,
					width * 24 / 360);
			drawerFunctionItemImageParams.setMargins(width * 16 / 360, 0, 0, 0);
			drawerFunctionItemImage.setLayoutParams(drawerFunctionItemImageParams);
			drawerFunctionItemImage.setScaleType(ScaleType.CENTER_CROP);
			drawerFunctionItemImage.setImageResource(drawerFunctionIcon[i]);
			final TextView drawerFunctionItemText = new TextView(mContext);
			drawerFunctionItemText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, height * 24 / 640));
			drawerFunctionItemText.setText(drawerFunctionText[i]);
			drawerFunctionItemText.setTextColor(Color.rgb(150, 150, 150));
			drawerFunctionItemText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			drawerFunctionItemText.setGravity(Gravity.CENTER_VERTICAL);
			drawerFunctionItemText.setPadding(height * 32 / 640, 0, 0, 0);
			drawerFunctionItem.addView(drawerFunctionItemImage);
			drawerFunctionItem.addView(drawerFunctionItemText);
			drawerFunctionLayout.addView(drawerFunctionItem);
			drawerFunctionItem.setOnClickListener(new MyDrawerClickListener(i));

		}
		drawerContentLayout.addView(drawerFunctionLayout);
		String[] item1 = { "设置", "关于", "帮助" };
		int[] item2 = { R.drawable.ic_settings_applications_grey600_24dp, R.drawable.ic_info_grey600_24dp,
				R.drawable.ic_help_grey600_24dp };
		LinearLayout drawerItemSecondLayout = new LinearLayout(mContext);
		drawerItemSecondLayout.setOrientation(LinearLayout.VERTICAL);
		RelativeLayout.LayoutParams drawerItemSecondLayoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		drawerItemSecondLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
		drawerItemSecondLayout.setLayoutParams(drawerItemSecondLayoutParams);
		LinearLayout drawerItem_1Division = new LinearLayout(mContext);
		drawerItem_1Division.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
		drawerItem_1Division.setBackgroundResource(R.drawable.ic_grey);
		drawerItemSecondLayout.addView(drawerItem_1Division);
		for (int i = 0; i < item2.length; i++) {
			LinearLayout drawer_Item = new LinearLayout(mContext);
			drawer_Item.setGravity(Gravity.CENTER_VERTICAL);
			drawer_Item.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout.LayoutParams drawer_ItemParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					width * 48 / 360);
			drawer_Item.setLayoutParams(drawer_ItemParams);
			ImageView drawer_ItemImageView = new ImageView(mContext);
			LinearLayout.LayoutParams drawer_ItemImageViewLayoutParams = new LinearLayout.LayoutParams(width * 24 / 360,
					width * 24 / 360);
			drawer_ItemImageViewLayoutParams.setMargins(width * 16 / 360, 0, 0, 0);
			drawer_ItemImageView.setLayoutParams(drawer_ItemImageViewLayoutParams);
			drawer_ItemImageView.setScaleType(ScaleType.CENTER_CROP);
			drawer_ItemImageView.setImageResource(item2[i]);
			final TextView drawer_ItemTextView = new TextView(mContext);
			drawer_ItemTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, height * 24 / 640));
			drawer_ItemTextView.setText(item1[i]);
			drawer_ItemTextView.setTextColor(Color.rgb(150, 150, 150));
			drawer_ItemTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			drawer_ItemTextView.setGravity(Gravity.CENTER_VERTICAL);
			drawer_ItemTextView.setPadding(height * 32 / 640, 0, 0, 0);
			drawer_ItemTextView.setTypeface(tf);// 设置字体
			drawer_Item.addView(drawer_ItemImageView);
			drawer_Item.addView(drawer_ItemTextView);
			drawer_Item.setOnClickListener(new MyDrawerClickListener(i + 2));
			drawerItemSecondLayout.addView(drawer_Item);
		}
		drawerContentLayout.addView(drawerItemSecondLayout);
	}

	private void runService() {
		// TODO Auto-generated method stub
		if (!isMyServiceRunning(mContext)) {
			Log.v("服务未启动", "正在启动");
			Intent intent2 = new Intent(mContext, WidgetService.class);
			mContext.startService(intent2);
		}
	}

	private boolean isMyServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.stu.zdy.weather.service.WidgetService".equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private void initDrawer() {
		drawerArrow = new DrawerArrowDrawable(this) {
			@Override
			public boolean isLayoutRtl() {
				return false;
			}
		};
		drawerArrow = new DrawerArrowDrawable(this) {
			@Override
			public boolean isLayoutRtl() {
				return false;
			}
		};
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, drawerArrow, R.string.drawer_open,
				R.string.drawer_close) {
			@Override
			public void onDrawerClosed(View view) {
				invalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle("质感天气");
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();
	}

	public void setActionbarColor(int arg0) {
		if (arg0 > 30) {
			toolBar.setBackgroundResource(R.drawable.hot);
		} else if (arg0 > 24) {
			toolBar.setBackgroundResource(R.drawable.warm);
		} else if (arg0 > 16) {
			toolBar.setBackgroundResource(R.drawable.comfortable);
		} else if (arg0 > 10) {
			toolBar.setBackgroundResource(R.drawable.cool);
		} else {
			toolBar.setBackgroundResource(R.drawable.cold);
		}
	}

	private void sendDataToWidget(JSONObject jsonObject) {
		try {
			if (jsonObject.toString().substring(1, 2).equals("{") && sharedPreferences.getString("defaultCity", "")
					.equals(jsonObject.getJSONArray("HeWeather data service 3.0").getJSONObject(0)
							.getJSONObject("basic").getString("city"))) {
				Editor editor = sharedPreferences.edit();
				forWidgetString = jsonObject.getJSONArray("HeWeather data service 3.0").getJSONObject(0)
						.getJSONObject("basic").getString("city")
						+ ","
						+ jsonObject.getJSONArray("HeWeather data service 3.0").getJSONObject(0).getJSONObject("now")
								.getString("tmp")
						+ ","
						+ jsonObject.getJSONArray("HeWeather data service 3.0").getJSONObject(0).getJSONObject("now")
								.getJSONObject("cond").getString("txt")
						+ ","
						+ jsonObject.getJSONArray("HeWeather data service 3.0").getJSONObject(0).getJSONObject("basic")
								.getJSONObject("update").getString("loc").substring(11, 16)
						+ "更新" + "," + jsonObject.getJSONArray("HeWeather data service 3.0").getJSONObject(0)
								.getJSONObject("now").getJSONObject("cond").getString("code");
				editor.putString("widget", forWidgetString);
				editor.commit();
				Intent intent = new Intent("com.stu.zdy.weather.big");
				intent.putExtra("index", 1);
				sendBroadcast(intent);
				Intent intent2 = new Intent("com.stu.zdy.weather.small");
				sendBroadcast(intent2);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void changeWeatherPicture(int kind, ImageView view) {
		switch (kind) {
		case 100:
		case 102:
		case 103:
			view.setImageDrawable(getResources().getDrawable(R.drawable.sunny_pencil));
			break;
		case 101:
			view.setImageDrawable(getResources().getDrawable(R.drawable.cloudy_pencil));
			break;
		case 104:
			view.setImageDrawable(getResources().getDrawable(R.drawable.overcast_pencil));
			break;
		case 302:
		case 303:
		case 304:
			view.setImageDrawable(getResources().getDrawable(R.drawable.storm_pencil));
			break;
		case 300:
		case 301:
		case 305:
		case 306:
		case 307:
		case 308:
		case 309:
		case 310:
		case 311:
		case 312:
		case 313:
			view.setImageDrawable(getResources().getDrawable(R.drawable.rain_pencil));
			break;
		case 400:
		case 401:
		case 402:
		case 403:
		case 404:
		case 405:
		case 406:
		case 407:
			view.setImageDrawable(getResources().getDrawable(R.drawable.snow_pencil));
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {// 手动点击actionbar弹出Drawerlayout
			if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
				mDrawerLayout.closeDrawer(Gravity.LEFT);
			} else {
				mDrawerLayout.openDrawer(Gravity.LEFT);
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	} // activity运行后加载该方法

	// 发生变动时执行方法，例如屏幕旋转
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	public void closeDrawer() {
		if (mDrawerLayout != null)
			mDrawerLayout.closeDrawers();
	}

	public static boolean checkDeviceHasNavigationBar(Context activity) {
		// 通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
		boolean hasMenuKey = ViewConfiguration.get(activity).hasPermanentMenuKey();
		boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
		if (!hasMenuKey && !hasBackKey) {
			return true;
		}
		return false;
	}

	private void showMaterialDialog() {
		mMaterialDialog = new MaterialDialog(mContext).setTitle("添加城市").setMessage("理论上支持县及其以上城市").setText("")
				.setPositiveButton("确定", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (DBManager.getIdByCityName(mMaterialDialog.getText()).equals("")) {
							Toast.makeText(mContext.getApplicationContext(), "输入有错误", Toast.LENGTH_SHORT).show();
						} else {// 输入成功
							FileUtils.saveCityList(mContext, FileUtils.DataOperate.ADD, mMaterialDialog.getText());
							viewPager.removeAllViews();
							initViewPager();
							mMaterialDialog.dismiss();

						}
					}
				}).setNegativeButton("取消", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mMaterialDialog.dismiss();
					}
				});
		mMaterialDialog.show();
	}

	private void setViewListener() {
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				((TextView) toolBar.getChildAt(1))
						.setText(FileUtils.getCityFromJsonArray(mContext, viewPager.getCurrentItem()));
				// try {
				// Log.v("当前标签页的温度",
				// String.valueOf(cityTemperature[viewPager.getCurrentItem()]));
				// setActionbarColor(Integer.valueOf(cityTemperature[viewPager.getCurrentItem()]));
				// } catch (Exception e) {
				// // TODO: handle exception
				// }
				try {

					setActionbarColor(citysCurrentTemper.get(arg0));
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});
		mDrawerLayout.setDrawerListener(new DrawerListener() {

			@Override
			public void onDrawerStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDrawerSlide(View arg0, float arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDrawerOpened(View arg0) {
				// TODO Auto-generated method stub
				getActionBar().setTitle("质感天气");
				// changeWeatherPicture(Integer.valueOf(cityWeatherArrayList.get(viewPager.getCurrentItem())),
				// drawerWeatherPictureImageView);
				// drawerTextView.setText(FileUtils.getCityFromJsonArray(mContext,
				// viewPager.getCurrentItem()) + "°");
				// ((TextView) toolBar.getChildAt(1)).setText("质感天气");
				// if
				// (Integer.valueOf(cityTemperature[viewPager.getCurrentItem()])
				// > 28) {
				// drawerBackGround.setImageResource(R.drawable.high_temper);
				// } else if
				// (Integer.valueOf(cityTemperature[viewPager.getCurrentItem()])
				// < 14) {
				// drawerBackGround.setImageResource(R.drawable.low_temper);
				// } else {
				// drawerBackGround.setImageResource(R.drawable.middle_temper);
				// }
			}

			@Override
			public void onDrawerClosed(View arg0) {
				// TODO Auto-generated method stub
				// if (viewPager != null) {
				// ((TextView) toolBar.getChildAt(1))
				// .setText(FileUtils.getCityFromJsonArray(mContext,
				// viewPager.getCurrentItem()));
				// }
			}
		});
	}

	@Override
	public void callbackWeatherFragment(Bundle arg, int number) {
		// TODO Auto-generated method stub
		if (number == 0) {
			if (NetWorkUtils.hasInternetConnection(mContext)) {
				showMaterialDialog();
			} else {
				Toast.makeText(mContext, "请打开网络后再试", Toast.LENGTH_SHORT).show();
			}
		}
		if (number == 1) {
			Intent i = getBaseContext().getPackageManager()
					.getLaunchIntentForPackage(getBaseContext().getPackageName());
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		}
		if (number == 2) {
			if (viewPager.getCurrentItem() == 0) {
			}
		}

	}

	@Override
	public void callbackCityFragment(Bundle arg) {
		// TODO Auto-generated method stub
		if (NetWorkUtils.hasInternetConnection(mContext)) {

			Intent i = getBaseContext().getPackageManager()
					.getLaunchIntentForPackage(getBaseContext().getPackageName());
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		} else {
			Toast.makeText(mContext, "请打开网络后重试", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void callbackSettingFragment(Bundle arg) {
		// TODO Auto-generated method stub
		fragmentTransaction = fragmentManager.beginTransaction();
		weatherLayout.setX(0);
		fragmentLayout.setX(ScreenUtils.getScreenWidth(mContext));
		fragmentTransaction.remove(settingFragment);
		fragmentTransaction.commit();
	}

	@Override
	public void callbackInfoFragment(Bundle arg) {
		// TODO Auto-generated method stub
		fragmentTransaction = fragmentManager.beginTransaction();
		weatherLayout.setX(0);
		fragmentLayout.setX(ScreenUtils.getScreenWidth(mContext));
		fragmentTransaction.remove(infoFragment);
		fragmentTransaction.commit();
	}

	@Override
	public void callbackHelpFragment(Bundle arg) {
		// TODO Auto-generated method stub
		fragmentTransaction = fragmentManager.beginTransaction();
		weatherLayout.setX(0);
		fragmentLayout.setX(ScreenUtils.getScreenWidth(mContext));
		fragmentTransaction.remove(helpFragment);
		fragmentTransaction.commit();
	}

	class MyDrawerClickListener implements View.OnClickListener {
		int position;

		MyDrawerClickListener(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			fragmentTransaction = fragmentManager.beginTransaction();
			switch (position) {
			case 0:// 显示天气
				weatherLayout.setX(0);
				fragmentLayout.setX(ScreenUtils.getScreenWidth(mContext));
				mDrawerLayout.closeDrawer(drawerContentLayout);
				break;
			case 1:// 城市管理
				Bundle bundle = new Bundle();
				bundle.putString("citys", FileUtils.getCityList(mContext).toString());
				if (fragmentManager.findFragmentByTag("cityFragment") == null) {
					fragmentLayout.setX(0);
					weatherLayout.setX(ScreenUtils.getScreenWidth(mContext));
					cityFragment = new ManageCityFragment();
					cityFragment.setArguments(bundle);
					fragmentTransaction.replace(fragmentLayout.getId(), cityFragment, "cityFragment");
					fragmentTransaction.commit();
				} else {
					Toast.makeText(mContext, "已经打开管理城市的列表啦！(｡・`ω´･)", Toast.LENGTH_SHORT).show();
				}
				mDrawerLayout.closeDrawer(drawerContentLayout);
				break;
			case 2:// 设置
				fragmentLayout.setX(0);
				weatherLayout.setX(ScreenUtils.getScreenWidth(mContext));
				if (fragmentManager.findFragmentByTag("settingFragment") == null) {
					settingFragment = new SettingFragment();
					fragmentTransaction.replace(fragmentLayout.getId(), settingFragment, "settingFragment");
				}
				fragmentTransaction.commit();
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				break;
			case 3:// 信息
				fragmentLayout.setX(0);
				weatherLayout.setX(ScreenUtils.getScreenWidth(mContext));
				if (fragmentManager.findFragmentByTag("infoFragment") == null) {
					infoFragment = new InfoFragment();
					fragmentTransaction.replace(fragmentLayout.getId(), infoFragment, "infoFragment");
				}
				fragmentTransaction.commit();
				mDrawerLayout.closeDrawer(Gravity.LEFT);

				break;
			case 4:// 帮助
				fragmentLayout.setX(0);
				weatherLayout.setX(ScreenUtils.getScreenWidth(mContext));
				if (fragmentManager.findFragmentByTag("helpFragment") == null) {
					helpFragment = new HelpFragment();
					fragmentTransaction.replace(fragmentLayout.getId(), helpFragment, "helpFragment");
				}
				fragmentTransaction.commit();
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				break;

			}
		}

	}

	public void onChangeAcitonbar(int temper, int index) {
		// TODO Auto-generated method stub
		citysCurrentTemper.put(index, temper);
	}
}
