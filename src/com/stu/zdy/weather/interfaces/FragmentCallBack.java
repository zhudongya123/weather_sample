package com.stu.zdy.weather.interfaces;

import android.os.Bundle;

public interface FragmentCallBack {
	void callbackWeatherFragment(Bundle arg, int number);

	void callbackCityFragment(Bundle arg);

	void callbackSettingFragment(Bundle arg);

	void callbackInfoFragment(Bundle arg);

	void callbackHelpFragment(Bundle arg);
}
