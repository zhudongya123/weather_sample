package com.stu.zdy.weather.object;

import android.os.Bundle;

public interface FragmentCallBack {
	public void callbackWeatherFragment(Bundle arg, int number);

	public void callbackCityFragment(Bundle arg);

	public void callbackSettingFragment(Bundle arg);

	public void callbackInfoFragment(Bundle arg);

	public void callbackHelpFragment(Bundle arg);
}
