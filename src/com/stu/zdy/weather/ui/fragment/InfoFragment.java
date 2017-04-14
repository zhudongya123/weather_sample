package com.stu.zdy.weather.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.Fragment;
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

import com.stu.zdy.weather.interfaces.FragmentCallBack;
import com.stu.zdy.weather.ui.MainActivity;
import com.stu.zdy.weather.util.ScreenUtils;
import com.stu.zdy.weather_sample.R;
import com.umeng.analytics.MobclickAgent;

@SuppressLint("InflateParams")
public class InfoFragment extends Fragment {
    private FragmentCallBack fragmentCallBack = null;
    public final static String TAG = "InfoFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        View view = inflater.inflate(R.layout.fragment_info, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return view;
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
        String string = "<p>天气icon作者:<a href=\"http://azuresol.deviantart.com\">AzureSol</a>";
        string = string
                + "<a href=\"http://creativecommons.org/licenses/by-sa/3.0/\">    许可协议</a></p>";
        string = string
                + "<p>开源支持:</p>";
        string = string
                + "<p>Material Design Icons  <a href=\"https://github.com/google/material-design-icons/releases/tag/1.0.0\">Github</a></p>";
        string = string
                + "<p>MaterialDialog  <a href=\"https://github.com/drakeet\">GitHub</a></p>";
        string = string
                + "<p>Ldrawer  <a href=\"https://github.com/keklikhasan/LDrawer\">GitHub</a></p>";
        string = string
                + "<p>FloatingActionButton  <a href=\"https://github.com/makovkastar/FloatingActionButton\">GitHub</a></p>";
        string = string
                + "<p>materialish-progress-master  <a href=\"https://github.com/pnikosis/materialish-progress\">GitHub</a></p>";
        string = string
                + "<p>Java:json-lib  <a href=\"http://json-lib.sourceforge.net\">GitHub</a></p>";
        string = string
                + "<p>okhttp-utils  <a href=\"https://github.com/hongyangAndroid/okhttp-utils\">GitHub</a></p>";
        string = string
                + "<p>glide  <a href=\"https://github.com/bumptech/glide\">GitHub</a></p>";
        CharSequence charSequence = Html.fromHtml(string);
        textView2.setText(charSequence);
        textView2.setMovementMethod(LinkMovementMethod.getInstance());
        String string2 = "<p><a href=\"http://weibo.com/u/3123268127\">联系作者</a></p>";

        CharSequence charSequence2 = Html.fromHtml(string2);
        textView3.setText(charSequence2);
        textView3.setMovementMethod(LinkMovementMethod.getInstance());

    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(InfoFragment.TAG);
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
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(InfoFragment.TAG);
    }

    @Override
    public void onAttach(Activity activity) {// Fragment
        // TODO Auto-generated method stub
        super.onAttach(activity);
        fragmentCallBack = (MainActivity) activity;// ActivityʵfragmentCallBack
    }
}
