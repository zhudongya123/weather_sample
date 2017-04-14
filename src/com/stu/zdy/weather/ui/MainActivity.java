package com.stu.zdy.weather.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.stu.zdy.weather.app.MyApplication;
import com.stu.zdy.weather.appwidget.HugeWeatherWidget;
import com.stu.zdy.weather.data.DBManager;
import com.stu.zdy.weather.interfaces.FragmentCallBack;
import com.stu.zdy.weather.open_source.Ldrawer.ActionBarDrawerToggle;
import com.stu.zdy.weather.open_source.Ldrawer.DrawerArrowDrawable;
import com.stu.zdy.weather.open_source.MaterialDialog;
import com.stu.zdy.weather.ui.fragment.HelpFragment;
import com.stu.zdy.weather.ui.fragment.InfoFragment;
import com.stu.zdy.weather.ui.fragment.ManageCityFragment;
import com.stu.zdy.weather.ui.fragment.SettingFragment;
import com.stu.zdy.weather.ui.fragment.WeatherFragment;
import com.stu.zdy.weather.util.FileUtils;
import com.stu.zdy.weather.util.NetWorkUtils;
import com.stu.zdy.weather.util.ScreenUtils;
import com.stu.zdy.weather.view.MyFragmentPagerAdapter;
import com.stu.zdy.weather_sample.R;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;

import static com.stu.zdy.weather.util.FileUtils.getCityFromJsonArray;

public class MainActivity extends AppCompatActivity implements FragmentCallBack {

    private Context mContext;
    private DrawerLayout mDrawerLayout;// 抽屉布局
    private RelativeLayout drawerContentLayout;// 抽屉根布局
    private Toolbar toolBar;
    private LinearLayout fragmentLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private ImageView drawerWeatherPictureImageView;
    private TextView drawerTextView;

    private ViewPager viewPager;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private SharedPreferences sharedPreferences;
    private ImageView drawerBackGround;
    private MaterialDialog mMaterialDialog;

    private ManageCityFragment manageCityFragment;
    private SettingFragment settingFragment;
    private InfoFragment infoFragment;
    private HelpFragment helpFragment;

    private JSONObject cityList;
    private int height, width;
    private ArrayList<Fragment> fragments;
    private Hashtable<Integer, Integer> citysCurrentTemper = new Hashtable<>();
    private Hashtable<Integer, Integer> citysCurrentWeatherCode = new Hashtable<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        sharedPreferences = mContext.getSharedPreferences("weather_info", Context.MODE_PRIVATE);
        toolBar = new Toolbar(mContext);
        toolBar.setTitleTextColor(getResources().getColor(R.color.white_100));
        setSupportActionBar(toolBar);
        setScreenParameter();


        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        initUI();
        initDrawer();
        fragments = new ArrayList<Fragment>();
        cityList = FileUtils.getCityList(mContext);

        try {
            if (cityList.getJSONArray("citylist").length() == 0) {// 城市列表为空
                if (NetWorkUtils.hasInternetConnection(mContext)) {// 存在网络
                    showMaterialDialog();
                } else {
                    Toast.makeText(mContext, "当前没有网络又没有文件缓存", Toast.LENGTH_SHORT).show();
                }
            } else {// 城市列表不为空
                initViewPager(false);
                if (!NetWorkUtils.hasInternetConnection(mContext)) {// 不存在网络的时候添加提示
                    Toast.makeText(mContext, "当前没有网络，正在使用文件缓存", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MobclickAgent.onPageStart(WeatherFragment.TAG);
    }

    /**
     * 设置虚拟按键透明和状态栏透明
     */
    private void setScreenParameter() {
        height = ScreenUtils.getScreenHeight(mContext);
        width = ScreenUtils.getScreenWidth(mContext);
        //height = height- ScreenUtils.getStatusHeight(mContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// AP>=19的时候启用透明状态栏
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);// 透明状态栏
        }
        if (ScreenUtils.checkDeviceHasNavigationBar(mContext)// 根据设置选择是否透明导航栏
                && sharedPreferences.getBoolean("naviBar", false)) {
            Window window = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
            height = (int) (height / 0.925);
        }
    }

    private void initViewPager(boolean bool) {
        // TODO Auto-generated method stub
        int length = 0;
        fragments = new ArrayList<>();
        cityList = FileUtils.getCityList(mContext);
        try {
            length = cityList.getJSONArray("citylist").length();
            for (int i = 0; i < length; i++) {
                String city = (String) cityList.getJSONArray("citylist").get(i);
                WeatherFragment weatherFragment = new WeatherFragment();
                Bundle bundle = new Bundle();
                bundle.putString("city", city);// 将城市名称传递给fragment
                bundle.putInt("index", i);
                weatherFragment.setArguments(bundle);
                fragments.add(weatherFragment);
            }
            MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(fragmentManager, fragments);
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(4);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        setViewListener();
        if (length >= 4) {
            Toast.makeText(mContext, "城市太多啦！为了不让机器造成卡顿，请自行左右滑动", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bool) viewPager.setCurrentItem(fragments.size() - 1);
    }

    private void initUI() {
        mDrawerLayout = new DrawerLayout(mContext);// 抽屉布局
        mDrawerLayout.setLayoutParams(new DrawerLayout.LayoutParams(LayoutParams.MATCH_PARENT, height));
        AbsoluteLayout rootContentLayout = new AbsoluteLayout(this);
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


        initToolBar();
        LinearLayout weatherLayout = new LinearLayout(mContext);
        weatherLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, ScreenUtils.getScreenHeight(this) * 584 / 640
                - ScreenUtils.getStatusHeight(this) + (sharedPreferences.getBoolean("naviBar", false) ? ScreenUtils.getScreenHeight(this) * 75 / 1000 : 0)));
        weatherLayout.setOrientation(LinearLayout.VERTICAL);
        weatherLayout.setId(4);
        fragmentLayout = new LinearLayout(mContext);
        fragmentLayout.setId(5);
        fragmentLayout.setLayoutParams(new LayoutParams(ScreenUtils.getScreenWidth(mContext), ScreenUtils.getScreenHeight(this) * 584 / 640
                - ScreenUtils.getStatusHeight(this) + (sharedPreferences.getBoolean("naviBar", false) ? ScreenUtils.getScreenHeight(this) * 75 / 1000 : 0)));
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

        rootContentLayout.addView(toolBar);
        rootContentLayout.addView(weatherLayout);
        rootContentLayout.addView(fragmentLayout);
        weatherLayout.addView(viewPager);
        mDrawerLayout.addView(rootContentLayout);
        mDrawerLayout.addView(drawerContentLayout);
        setContentView(mDrawerLayout);
    }

    private void initToolBar() {
        toolBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, height * 56 / 640));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            toolBar.setPadding(0, ScreenUtils.getStatusHeight(mContext), 0, 0);
            toolBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    height * 56 / 640 + ScreenUtils.getStatusHeight(mContext)));
        }
        toolBar.setNavigationIcon(R.drawable.ic_drawer);
    }

    private void initDrawerContent(Typeface tf) {
        String[] drawerFunctionText = getResources().getStringArray(R.array.drawer_list1);
        int[] drawerFunctionIcon = {R.drawable.ic_wb_sunny_grey600_24dp, R.drawable.ic_location_city_grey600_24dp};
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
            TextView drawerFunctionItemText = new TextView(mContext);
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

        String[] item1 = getResources().getStringArray(R.array.drawer_list2);
        int[] item2 = {R.drawable.ic_settings_applications_grey600_24dp, R.drawable.ic_info_grey600_24dp,
                R.drawable.ic_help_grey600_24dp};
        LinearLayout drawerItemSecondLayout = new LinearLayout(mContext);
        drawerItemSecondLayout.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams drawerItemSecondLayoutParams = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        if (sharedPreferences.getBoolean("naviBar", false)) {
            drawerItemSecondLayoutParams.setMargins(0, 0, 0, (int) (ScreenUtils.getScreenHeight(this) * 0.075));
        }
        drawerItemSecondLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
        drawerItemSecondLayout.setLayoutParams(drawerItemSecondLayoutParams);
        LinearLayout drawerDivider = new LinearLayout(mContext);
        drawerDivider.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
        drawerDivider.setBackgroundResource(R.drawable.ic_grey);
        drawerItemSecondLayout.addView(drawerDivider);
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
            TextView drawer_ItemTextView = new TextView(mContext);
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


    private void initDrawer() {
        DrawerArrowDrawable drawerArrow;
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
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
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

    public void setActionbarColor(int arg0) {
        if (arg0 > 30) {
            toolBar.setBackgroundResource(R.color.actionbar_hot);
        } else if (arg0 > 24) {
            toolBar.setBackgroundResource(R.color.actionbar_warm);
        } else if (arg0 > 16) {
            toolBar.setBackgroundResource(R.color.actionbar_comfortable);
        } else if (arg0 > 10) {
            toolBar.setBackgroundResource(R.color.actionbar_cool);
        } else {
            toolBar.setBackgroundResource(R.color.actionbar_cold);
        }
    }


    private void changeWeatherPicture(int kind, final ImageView view) {
        final Context context = this;
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_huge);
        Glide.with(context).load(MyApplication.WEATHER_ICON_URL + kind + ".png").asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                view.setImageBitmap(resource);
                ComponentName thisWidget = new ComponentName(context, HugeWeatherWidget.class);
                AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, views);
            }
        });


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    private void showMaterialDialog() {
        mMaterialDialog = new MaterialDialog(mContext).setTitle("添加城市").setMessage("理论上支持县及其以上城市").setText("")
                .setPositiveButton("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (DBManager.getIdByCityName(mMaterialDialog.getText()).equals("")) {
                            Toast.makeText(mContext.getApplicationContext(), "输入有错误", Toast.LENGTH_SHORT).show();
                        } else {// 输入成功
                            try {
                                FileUtils.saveCityList(mContext, mMaterialDialog.getText());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            viewPager.removeAllViews();
                            initViewPager(true);
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
                toolBar.setTitle(getCityFromJsonArray(mContext, viewPager.getCurrentItem()));
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
                try {
                    changeWeatherPicture(citysCurrentWeatherCode.get(viewPager.getCurrentItem()),
                            drawerWeatherPictureImageView);
                    drawerTextView.setText(getCityFromJsonArray(mContext,
                            viewPager.getCurrentItem()) + "\n" + citysCurrentTemper.get(viewPager.getCurrentItem()) + getString(R.string.degree));
                    if (citysCurrentTemper.get(viewPager.getCurrentItem()) > 28) {
                        drawerBackGround.setImageResource(R.drawable.high_temper);
                    } else if (citysCurrentTemper.get(viewPager.getCurrentItem()) < 14) {
                        drawerBackGround.setImageResource(R.drawable.low_temper);
                    } else {
                        drawerBackGround.setImageResource(R.drawable.middle_temper);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onDrawerClosed(View arg0) {
                // TODO Auto-generated method stub
                if (viewPager != null) {
                    toolBar.setTitle(getCityFromJsonArray(mContext, viewPager.getCurrentItem()));
                }
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
            Toast.makeText(mContext, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void callbackSettingFragment(Bundle arg) {
        // TODO Auto-generated method stub
        removeFragmentByTag(SettingFragment.TAG);
        MobclickAgent.onPageStart(WeatherFragment.TAG);
    }

    @Override
    public void callbackInfoFragment(Bundle arg) {
        // TODO Auto-generated method stub
        removeFragmentByTag(InfoFragment.TAG);
        MobclickAgent.onPageStart(WeatherFragment.TAG);
    }

    @Override
    public void callbackHelpFragment(Bundle arg) {
        // TODO Auto-generated method stub
        removeFragmentByTag(HelpFragment.TAG);
        MobclickAgent.onPageStart(WeatherFragment.TAG);
    }

    private void removeFragmentByTag(String name) {
        fragmentTransaction = fragmentManager.beginTransaction();
        switch (name) {
            case ManageCityFragment.TAG:
                if (fragmentManager.findFragmentByTag(ManageCityFragment.TAG) != null)
                    fragmentTransaction.remove(manageCityFragment);
                break;
            case InfoFragment.TAG:
                if (fragmentManager.findFragmentByTag(InfoFragment.TAG) != null)
                    fragmentTransaction.remove(infoFragment);
                break;
            case HelpFragment.TAG:
                if (fragmentManager.findFragmentByTag(HelpFragment.TAG) != null)
                    fragmentTransaction.remove(helpFragment);
                break;
            case SettingFragment.TAG:
                if (fragmentManager.findFragmentByTag(SettingFragment.TAG) != null)
                    fragmentTransaction.remove(settingFragment);
                break;
        }
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
            mDrawerLayout.closeDrawer(Gravity.LEFT);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    fragmentTransaction = fragmentManager.beginTransaction();
                    switch (position) {
                        case 0:// 显示天气
                            removeFragmentByTag(ManageCityFragment.TAG);
                            removeFragmentByTag(InfoFragment.TAG);
                            removeFragmentByTag(HelpFragment.TAG);
                            removeFragmentByTag(SettingFragment.TAG);
                            break;
                        case 1:// 城市管理
                            Bundle bundle = new Bundle();
                            bundle.putString("citys", FileUtils.getCityList(mContext).toString());
                            if (fragmentManager.findFragmentByTag(ManageCityFragment.TAG) == null) {
                                manageCityFragment = new ManageCityFragment();
                                manageCityFragment.setArguments(bundle);
                                fragmentTransaction.replace(fragmentLayout.getId(), manageCityFragment, ManageCityFragment.TAG);
                                fragmentTransaction.commit();
                            }
                            MobclickAgent.onPageEnd(WeatherFragment.TAG);
                            break;
                        case 2:// 设置
                            if (fragmentManager.findFragmentByTag(SettingFragment.TAG) == null) {
                                settingFragment = new SettingFragment();
                                fragmentTransaction.replace(fragmentLayout.getId(), settingFragment, SettingFragment.TAG);
                            }
                            fragmentTransaction.commit();
                            MobclickAgent.onPageEnd(WeatherFragment.TAG);
                            break;
                        case 3:// 信息
                            if (fragmentManager.findFragmentByTag(InfoFragment.TAG) == null) {
                                infoFragment = new InfoFragment();
                                fragmentTransaction.replace(fragmentLayout.getId(), infoFragment, InfoFragment.TAG);
                            }
                            fragmentTransaction.commit();
                            MobclickAgent.onPageEnd(WeatherFragment.TAG);
                            break;
                        case 4:// 帮助
                            if (fragmentManager.findFragmentByTag(HelpFragment.TAG) == null) {
                                helpFragment = new HelpFragment();
                                fragmentTransaction.replace(fragmentLayout.getId(), helpFragment, HelpFragment.TAG);
                            }
                            fragmentTransaction.commit();
                            MobclickAgent.onPageEnd(WeatherFragment.TAG);
                            break;
                    }
                }
            }, 300);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(mContext);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(mContext);
    }

    public void setMyTheme(Context context, int type) {
        if (type > 30) {
            context.setTheme(R.style.AppTheme_Cold);
        } else if (type > 24) {
            context.setTheme(R.style.AppTheme_Cool);
        } else if (type > 16) {
            context.setTheme(R.style.AppTheme_Comfortable);
        } else if (type > 10) {
            context.setTheme(R.style.AppTheme_Warm);
        } else {
            context.setTheme(R.style.AppTheme_Hot);
        }
        this.finish();
        final Intent intent = getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public void onChangeActionbar(int temper, int index) {
        // TODO Auto-generated method stub
        citysCurrentTemper.put(index, temper);
    }

    public void onChangeDrawerWeather(int code, int index) {
        citysCurrentWeatherCode.put(index, code);
    }
}
