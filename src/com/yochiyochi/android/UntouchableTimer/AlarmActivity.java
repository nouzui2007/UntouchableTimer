package com.yochiyochi.android.UntouchableTimer;

import java.text.SimpleDateFormat;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.ListPreference;
import android.provider.Settings;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;



public class AlarmActivity extends Activity implements SensorEventListener {


	Context mContext;
	Ringtone rt;
	Vibrator vib;
	public PowerManager.WakeLock wl;
	static TextView tv;
	static TextView tv_message1;
	static TextView tv_sensor_message;
	private SensorManager sensorMgr;
	private boolean hasSensor;
	private MediaPlayer alarm;

	AudioManager am;
	SeekBar ringVolSeekBar;
	TextView ringVolText;

	private String pref_sound;
	private boolean pref_vibrator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		mContext = this;
		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		hasSensor = false;

		PowerManager pm=(PowerManager) getSystemService(Context.POWER_SERVICE);
		wl=pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK+PowerManager.ON_AFTER_RELEASE, "My Tag");
		wl.acquire();

		vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

		// 画面メッセージ
		tv = (TextView) findViewById(R.id.CountdownTimer);
		SimpleDateFormat form = new SimpleDateFormat("mm:ss");
		tv.setText(form.format(0));
		tv.setTextColor(Color.RED);
		tv_message1 = (TextView) findViewById(R.id.message1);
		tv_message1.setText(R.string.message_timeup);
		tv_message1.setTextColor(Color.RED);
		tv_sensor_message = (TextView) findViewById(R.id.sensor_message);
		tv_sensor_message.setText(R.string.message_Alarm_Stop);
		
	}

	@Override
	protected void onResume() {
		super.onResume();

		// アラーム音量
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int ringVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC); // 音量の取得
		ringVolSeekBar = (SeekBar) findViewById(R.id.ringVolSeekBar); // 音量シークバー
		ringVolSeekBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)); //最大音量の設定
		ringVolText = (TextView) findViewById(R.id.ringVolText); // 音量TextView
		ringVolText.setText("Volume:" + ringVolume); // TextViewに設定値を表示
		am.setStreamVolume(AudioManager.STREAM_MUSIC, ringVolume, 0); // 着信音量設定
		ringVolSeekBar.setProgress(ringVolume); // 音量をSeekBarにセット

		ringVolSeekBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						// TODO Auto-generated method stub
						ringVolText.setText("Volume:" + progress); // TextViewに設定値を表示
						am.setStreamVolume(AudioManager.STREAM_MUSIC, progress,
								0); // 着信音量設定
						ringVolSeekBar.setProgress(progress); // 音量をSeekBarにセット
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub

					}

					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub

					}
				});

		List<Sensor> sensors = sensorMgr.getSensorList(Sensor.TYPE_PROXIMITY);
		if (sensors.size() > 0) {
			// センサーリスナー開始
			Sensor sensor = sensors.get(0);
			hasSensor = sensorMgr.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_NORMAL);
		}

		SharedPreferences prefs;
		prefs = this.getSharedPreferences("UntouchableTimerPrefs", 0);
		pref_sound = prefs.getString(
				(String) getResources().getText(R.string.pref_key_sound), "");
		if (pref_sound == null)
			pref_sound = "1";
		else if (pref_sound.equals(""))
			pref_sound = "1";
		String[] sounds = getResources().getStringArray(R.array.entries);

		String selected_sound = sounds[Integer.parseInt(pref_sound) - 1];
		pref_vibrator = prefs.getBoolean(
				(String) getResources().getText(R.string.pref_key_vibrator),
				true);

//		Uri fn = Uri.parse(prefs.getString("alarm", ""));
//
//		if (fn != null) {
//			rt = RingtoneManager.getRingtone(this, fn);
//			System.out.println(fn.toString());
//		} else {
//			rt = RingtoneManager.getRingtone(this,
//					Settings.System.DEFAULT_ALARM_ALERT_URI);
//		}

		switch (Integer.valueOf(pref_sound).intValue()) {
		case 1:
			alarm = null; break;
		case 2:
			alarm = MediaPlayer.create(mContext, R.raw.alarm1); break;
		case 3:
			alarm = MediaPlayer.create(mContext, R.raw.alarm2); break;
		case 4:
			alarm = MediaPlayer.create(mContext, R.raw.alarm3); break;
		case 5:
			alarm = MediaPlayer.create(mContext, R.raw.sensorcatch); break;
		}
		if (alarm != null) {
			alarm.setLooping(true);
			// アラーム音を出す
			alarm.start();
		}

		if (pref_vibrator)
			vib.vibrate(new long[] { 0, 1000, 500, 1000, 500, 1000 }, -1);

	}

	@Override
	protected void onPause() {
		super.onPause();
		// センサーリスナー終了
		if (hasSensor) {
			sensorMgr.unregisterListener(this);
			hasSensor = false;
		}
		// アラーム音を消す
		alarm.stop();
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
			if (event.values[0] < 1.0) // 近接センサーで「近い」
			{
				finish();
			}

		}
	}

}


