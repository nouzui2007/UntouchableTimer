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
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.widget.TextView;



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
		alarm = MediaPlayer.create(mContext, R.raw.alarm1);

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

		List<Sensor> sensors = sensorMgr.getSensorList(Sensor.TYPE_PROXIMITY);
		if (sensors.size() > 0) {
			// センサーリスナー開始
			Sensor sensor = sensors.get(0);
			hasSensor = sensorMgr.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_NORMAL);
		}

		SharedPreferences prefs;
		prefs = this.getSharedPreferences("CountdownTimerPrefs", 0);
		Uri fn = Uri.parse(prefs.getString("alarm", ""));

		if (fn != null) {
			rt = RingtoneManager.getRingtone(this, fn);
			System.out.println(fn.toString());
		} else {
			rt = RingtoneManager.getRingtone(this,
					Settings.System.DEFAULT_ALARM_ALERT_URI);
		}
		if (rt != null) {
			if (!rt.isPlaying())
				rt.play();
		}

		if (prefs.getBoolean("vibrator", true))
			vib.vibrate(new long[] { 0, 1000, 500, 1000, 500, 1000 }, -1);
		// アラーム音を出す
		alarm.setLooping(true);
		alarm.start();

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


//
////
//package com.yochiyochi.android.UntouchableTimer;
//
//import java.util.List;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.media.MediaPlayer;
//import android.media.Ringtone;
//import android.media.RingtoneManager;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.PowerManager;
//import android.os.Vibrator;
//import android.provider.Settings;
//import android.widget.TextView;
//
//public class AlarmActivity extends Activity implements SensorEventListener {
//
//
//	Context mContext;
//	Ringtone rt;
//	Vibrator vib;
//	public PowerManager.WakeLock wl;
//	static TextView tv_sensor_message;
//	private SensorManager sensorMgr;
//	private boolean hasSensor;
//	private MediaPlayer alarm;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//
//		setContentView(R.layout.alarm);
//
//		mContext = this;
//		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
//		hasSensor = false;
//
//		PowerManager pm=(PowerManager) getSystemService(Context.POWER_SERVICE);
//		wl=pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK+PowerManager.ON_AFTER_RELEASE, "My Tag");
//		wl.acquire();
//
//		vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//		alarm = MediaPlayer.create(mContext, R.raw.alarm1);
//
//		// 画面メッセージ
//		tv_sensor_message = (TextView) findViewById(R.id.sensor_message);
//		tv_sensor_message.setText(R.string.hand_Alarm_Stop);
//	}
//
//	@Override
//	protected void onResume() {
//		super.onResume();
//
//		List<Sensor> sensors = sensorMgr.getSensorList(Sensor.TYPE_PROXIMITY);
//		if (sensors.size() > 0) {
//			// センサーリスナー開始
//			Sensor sensor = sensors.get(0);
//			hasSensor = sensorMgr.registerListener(this, sensor,
//					SensorManager.SENSOR_DELAY_NORMAL);
//		}
//
//		SharedPreferences prefs;
//		prefs = this.getSharedPreferences("CountdownTimerPrefs", 0);
//		Uri fn = Uri.parse(prefs.getString("alarm", ""));
//
//		if (fn != null) {
//			rt = RingtoneManager.getRingtone(this, fn);
//			System.out.println(fn.toString());
//		} else {
//			rt = RingtoneManager.getRingtone(this,
//					Settings.System.DEFAULT_ALARM_ALERT_URI);
//		}
//		if (rt != null) {
//			if (!rt.isPlaying())
//				rt.play();
//		}
//
//		if (prefs.getBoolean("vibrator", true))
//			vib.vibrate(new long[] { 0, 1000, 500, 1000, 500, 1000 }, -1);
//		// アラーム音を出す
//		alarm.setLooping(true);
//		alarm.start();
//
//	}
//
//	@Override
//	protected void onPause() {
//		super.onPause();
//		// センサーリスナー終了
//		if (hasSensor) {
//			sensorMgr.unregisterListener(this);
//			hasSensor = false;
//		}
//		// アラーム音を消す
//		alarm.stop();
//	}
//
//	@Override
//	public void onAccuracyChanged(Sensor arg0, int arg1) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onSensorChanged(SensorEvent event) {
//		// TODO Auto-generated method stub
//		if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
//			if (event.values[0] < 1.0) // 近接センサーで「近い」
//			{
//				finish();
//			}
//
//		}
//	}
//
//}
