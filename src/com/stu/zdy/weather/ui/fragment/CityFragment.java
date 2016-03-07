package com.stu.zdy.weather.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.stu.zdy.weather.ui.MainActivity;
import com.stu.zdy.weather.object.FragmentCallBack;
import com.stu.zdy.weather_sample.R;

@SuppressWarnings("deprecation")
public class CityFragment extends Fragment {
	private ScrollView scrollView;
	private AbsoluteLayout absoluteLayout;
	private Bundle bundle;
	private FragmentCallBack fragmentCallBack = null;
	private String[] cityStrings = { "", "", "", "", "", "", "", "", "" };
	private int height, width;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		height = dm.heightPixels;
		width = dm.widthPixels;
		scrollView = new ScrollView(getActivity());
		scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		scrollView.setBackgroundColor(Color.WHITE);
		scrollView.setFillViewport(true);
		absoluteLayout = new AbsoluteLayout(getActivity());
		absoluteLayout.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		scrollView.addView(absoluteLayout);
		getDataFromActivity();
		initUI();
		return scrollView;
	}

	private void getDataFromActivity() {
		bundle = getArguments();
		String citys = bundle.getString("citys", "北京");
		citys = citys + ",";
		Log.v("从MainFragment获得城市列表", citys);
		int j = 0, k = 0;
		for (int i = 0; i < citys.length(); i++) {
			if (citys.substring(i, i + 1).equals(",")) {
				cityStrings[k] = citys.substring(j, i);
				j = i + 1;
				k++;
			}
		}
	}

	private void initUI() {
		// TODO Auto-generated method stub

		int index = 0;
		for (int i = 0; i < cityStrings.length; i++) {
			if (!cityStrings[i].equals("")) {
				RelativeLayout layout = new RelativeLayout(getActivity());
				layout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
				layout.setLayoutParams(new LayoutParams(width / 3, height / 4));
				ImageView deleteButton = new ImageView(getActivity());
				RelativeLayout.LayoutParams deleteButtonLayoutParams = new RelativeLayout.LayoutParams(
						width / 14, width / 14);
				deleteButtonLayoutParams.addRule(
						RelativeLayout.ALIGN_PARENT_TOP, 1);
				deleteButtonLayoutParams.addRule(
						RelativeLayout.ALIGN_PARENT_RIGHT, 1);
				deleteButton.setLayoutParams(deleteButtonLayoutParams);
				deleteButton
						.setImageResource(R.drawable.ic_cancel_grey600_24dp);
				deleteButton.setScaleType(ScaleType.CENTER_CROP);
				final int[] a = { i, 1 };
				deleteButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if (absoluteLayout.getChildCount() == 1) {
							Toast.makeText(getActivity(),
									"只剩一个城市啦，不能删除(｡・`ω´･)", Toast.LENGTH_SHORT)
									.show();
						} else {
							cityStrings[a[0]] = "";
							// TODO Auto-generated method stub
							Log.v("删除了第" + a[0] + "个城市", cityStrings[a[0]]);
							absoluteLayout.removeAllViews();
							initUI();
						}

					}
				});
				TextView cityNameTextView = new TextView(getActivity());
				cityNameTextView.setLayoutParams(new LayoutParams(width / 3,
						height / 4));
				cityNameTextView.setGravity(Gravity.CENTER);
				cityNameTextView.setText(cityStrings[i]);
				layout.addView(deleteButton);
				layout.addView(cityNameTextView);
				layout.setX((width / 3) * (index % 3));
				layout.setY((height / 4) * (index / 3));
				index++;
				absoluteLayout.addView(layout);
			}
		}

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
					// 在CityFragment位于界面上时，按下back键自动调用此方法
					String string = new String();
					for (int i = 0; i < cityStrings.length; i++) {
						if (!cityStrings[i].equals("")) {
							string = string + "," + cityStrings[i];
						}
					}
					string = string.substring(1, string.length());
					Bundle bundle = new Bundle();
					bundle.putString("citys", string);
					FragmentManager fragmentManager = getFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager
							.beginTransaction();
					fragmentTransaction.remove(fragmentManager
							.findFragmentByTag("cityFragment"));
					fragmentTransaction.commit();
					Log.v("cityFragment中点击了Back键", "cityFragment中点击了Back键");
					fragmentCallBack.callbackCityFragment(bundle);// 调用了Activity中的callbackCityFragment方法
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
		Log.v("调用了CityFragment中的onAttach方法", "调用了CityFragment中的onAttach方法");
		fragmentCallBack = (MainActivity) activity;// 将Activity实例赋给fragmentCallBack
	}
}
