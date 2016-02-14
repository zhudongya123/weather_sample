package com.stu.zdy.weather.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stu.zdy.weather.activity.MainActivity;
import com.stu.zdy.weather.object.FragmentCallBack;
import com.stu.zdy.weather_sample.R;

@SuppressLint("InflateParams") public class HelpFragment extends Fragment {
	private FragmentCallBack fragmentCallBack = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.fragment_help, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		AssetManager mgr = getActivity().getAssets();
		@SuppressWarnings("unused")
		Typeface tf = Typeface.createFromAsset(mgr, "fonts/Roboto-Regular.ttf");// 根据路径得到Typeface
		// TextView textView = (TextView) getActivity().findViewById(R.id.help);
		// textView.setTypeface(tf);
	}

	public void onResume() {
		super.onResume();
		getView().setFocusableInTouchMode(true);
		getView().requestFocus();
		getView().setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (event.getAction() == KeyEvent.ACTION_UP
						&& keyCode == KeyEvent.KEYCODE_BACK) {
					fragmentCallBack.callbackHelpFragment(null);
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
