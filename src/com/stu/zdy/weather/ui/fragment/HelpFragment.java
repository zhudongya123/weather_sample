package com.stu.zdy.weather.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stu.zdy.weather.interfaces.FragmentCallBack;
import com.stu.zdy.weather.ui.MainActivity;
import com.stu.zdy.weather_sample.R;
import com.umeng.analytics.MobclickAgent;

public class HelpFragment extends Fragment {

    public final static String TAG = "HelpFragment";
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
        Typeface tf = Typeface.createFromAsset(mgr, "fonts/Roboto-Regular.ttf");
        // TextView textView = (TextView) getActivity().findViewById(R.id.help);
        // textView.setTypeface(tf);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(HelpFragment.TAG);
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
    public void onAttach(Activity context) {
        // TODO Auto-generated method stub
        super.onAttach(context);
        fragmentCallBack = (MainActivity) context;
    }

    @Override
    public void onPause() {
        MobclickAgent.onPageEnd(HelpFragment.TAG);
        super.onPause();
    }
}
