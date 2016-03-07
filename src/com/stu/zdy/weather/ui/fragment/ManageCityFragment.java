package com.stu.zdy.weather.ui.fragment;

import org.json.JSONArray;
import org.json.JSONException;

import com.stu.zdy.weather.ui.MainActivity;
import com.stu.zdy.weather.object.FragmentCallBack;
import com.stu.zdy.weather.util.FileUtils;
import com.stu.zdy.weather_sample.R;

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
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class ManageCityFragment extends Fragment {
	private ScrollView scrollView;
	private AbsoluteLayout absoluteLayout;
	private FragmentCallBack fragmentCallBack = null;
	private int height, width;
	private JSONArray cityList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
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

		initUI();
		return scrollView;
	}

	private void initUI() {
		try {
			cityList = FileUtils.getCityList(getActivity()).getJSONArray(
					"citylist");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int index = 0;
		for (int i = 0; i < cityList.length(); i++) {
			RelativeLayout layout = new RelativeLayout(getActivity());
			// layout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
			layout.setLayoutParams(new LayoutParams(width / 3, height / 4));
			ImageView deleteButton = new ImageView(getActivity());
			RelativeLayout.LayoutParams deleteButtonLayoutParams = new RelativeLayout.LayoutParams(
					width / 14, width / 14);
			deleteButtonLayoutParams
					.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
			deleteButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
					1);
			deleteButton.setLayoutParams(deleteButtonLayoutParams);
			deleteButton.setImageResource(R.drawable.ic_cancel_grey600_24dp);
			deleteButton.setScaleType(ScaleType.CENTER_CROP);
			deleteButton.setOnClickListener(new DeleteCityOnClickListener(i));
			TextView cityNameTextView = new TextView(getActivity());
			cityNameTextView.setLayoutParams(new LayoutParams(width / 3,
					height / 4));
			cityNameTextView.setGravity(Gravity.CENTER);
			try {
				cityNameTextView.setText((String) cityList.get(i));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			layout.addView(deleteButton);
			layout.addView(cityNameTextView);
			layout.setX((width / 3) * (index % 3));
			layout.setY((height / 4) * (index / 3));
			index++;
			absoluteLayout.addView(layout);
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
					FragmentManager fragmentManager = getFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager
							.beginTransaction();
					fragmentTransaction.remove(fragmentManager
							.findFragmentByTag("cityFragment"));
					fragmentTransaction.commit();
					Log.v("cityFragment中点击了Back键", "cityFragment中点击了Back键");
					fragmentCallBack.callbackCityFragment(null);//
					// 调用了Activity中的callbackCityFragment方法
					return true;
				}

				return false;
			}
		});
	}

	@Override
	public void onAttach(Activity activity) {// 启动Fragment调用
		super.onAttach(activity);
		fragmentCallBack = (MainActivity) activity;// 将Activity实例赋给fragmentCallBack
	}

	class DeleteCityOnClickListener implements View.OnClickListener {

		int index;

		public DeleteCityOnClickListener(int index) {
			this.index = index;
		}

		@Override
		public void onClick(View v) {

			if (absoluteLayout.getChildCount() == 1) {
				Toast.makeText(getActivity(), "只剩一个城市啦，不能删除(｡・`ω´･)",
						Toast.LENGTH_SHORT).show();
			} else {
				Log.v("点击删除按钮", "点击删除按钮");
				FileUtils.removeCityFromJsonArray(getActivity(), index);
				absoluteLayout.removeAllViews();
				initUI();
			}
		}
	}
}
