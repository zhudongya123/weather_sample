package com.stu.zdy.weather.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.stu.zdy.weather.activity.MainActivity;
import com.stu.zdy.weather.object.FragmentCallBack;
import com.stu.zdy.weather.util.ScreenUtils;
import com.stu.zdy.weather_sample.R;

@SuppressLint("InflateParams") public class InfoFragment extends Fragment {
	private FragmentCallBack fragmentCallBack = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		return inflater.inflate(R.layout.fragment_info, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		ScrollView scrollView = (ScrollView) getActivity().findViewById(
				R.id.scrollview_info);

		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int height = dm.heightPixels;
		scrollView.getLayoutParams().height = (height - ScreenUtils
				.getStatusHeight(getActivity())) * 586 / 640;
		TextView textView2 = (TextView) getActivity().findViewById(
				R.id.textview2);
		TextView textView3 = (TextView) getActivity().findViewById(
				R.id.textview3);
		String string = "<p>素描天气图标作者:<a href=\"http://azuresol.deviantart.com\">AzureSol</a>";
		string = string
				+ "<a href=\"http://creativecommons.org/licenses/by-sa/3.0/\">使用许可</a></p>";
		string = string
				+ "<p>其他图标:Material Design Icons  <a href=\"https://github.com/google/material-design-icons/releases/tag/1.0.0\">Github</a></p>";
		string = string
				+ "<p>开源控件:MaterialDialog  <a href=\"https://github.com/drakeet\">GitHub</a></p>";
		string = string
				+ "<p>开源控件:Ldrawer  <a href=\"https://github.com/keklikhasan/LDrawer\">GitHub</a></p>";
		string = string
				+ "<p>开源控件:FloatingActionButton  <a href=\"https://github.com/makovkastar/FloatingActionButton\">GitHub</a></p>";
		string = string
				+ "<p>开源控件:materialish-progress-master  <a href=\"https://github.com/pnikosis/materialish-progress\">GitHub</a></p>";
		string = string
				+ "<p>Java第三方库:json-lib  <a href=\"http://json-lib.sourceforge.net\">网站</a></p>";
		string = string + "<p>天气照片为个人拍摄，LoGo为个人二次创作</p>";
		CharSequence charSequence = Html.fromHtml(string);
		textView2.setText(charSequence);
		textView2.setMovementMethod(LinkMovementMethod.getInstance());
		String string2 = "<p>编码设计:<a href=\"http://weibo.com/u/3123268127\">@先生的后花园</a></p>";

		CharSequence charSequence2 = Html.fromHtml(string2);
		textView3.setText(charSequence2);
		textView3.setMovementMethod(LinkMovementMethod.getInstance());

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
					fragmentCallBack.callbackInfoFragment(null);
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
