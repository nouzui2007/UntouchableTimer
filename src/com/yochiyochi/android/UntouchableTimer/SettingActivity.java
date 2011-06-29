package com.yochiyochi.android.UntouchableTimer;

import android.os.Bundle;
// import android.preference.CheckBoxPreference;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

public class SettingActivity extends PreferenceActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		

		CheckBoxPreference safe = (CheckBoxPreference) findPreference(getResources().getText(R.string.pref_key_vibrator));
		safe.setOnPreferenceChangeListener(
			new OnPreferenceChangeListener()
			{
				@Override
				public boolean onPreferenceChange(Preference pref, Object newValue)
				{
					return true;
				}
			});
		
	}
}
