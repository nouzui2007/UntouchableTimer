package com.yochiyochi.android.UntouchableTimer;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.util.Log;


public class SettingActivity extends PreferenceActivity
{
	static final String TAG = "SettingActivity";
	{
		Log.d(TAG, "@@@---start---@@@");
	}
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		//背景色の設定。ここのSuperにOnCreateを呼ぶ前じゃないと受け付けてくれない
//	    setTheme(R.style.simpleTheme);

		super.onCreate(savedInstanceState);
		
		// XML で Preference を設定
		addPreferencesFromResource(R.xml.preference);
		getPreferenceManager().setSharedPreferencesName("UntouchableTimerPrefs");

		// ListPreference の取得
		ListPreference listPreferrence = (ListPreference) findPreference(getString(R.string.pref_key_sound));

		
		
//		Log.d(TAG, "onCreate1");
//		setContentView(R.xml.preference);
//		Log.d(TAG, "onCreate2");
//		
//		getPreferenceManager().setSharedPreferencesName("CountdownTimerPrefs");
//		Log.d(TAG, "onCreate3");
//		addPreferencesFromResource(R.xml.preference);
//		Log.d(TAG, "onCreate4");
		
//
//		CheckBoxPreference safe = (CheckBoxPreference) findPreference(getResources().getText(R.string.pref_key_vibrator));
//		safe.setOnPreferenceChangeListener(
//			new OnPreferenceChangeListener()
//			{
//				@Override
//				public boolean onPreferenceChange(Preference pref, Object newValue)
//				{
//					return true;
//				}
//			});
		
	}
}
