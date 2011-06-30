package com.yochiyochi.android.UntouchableTimer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

public class UntouchableTimerActivity extends Activity implements
		SensorEventListener {

	// 言い訳
	// メモがわりにいろいろ書いてるけど、後でコメントきれいにします
	// Javadocのことも薄々覚えてます

	// メニューの部分はまだダミー状態
	// SpeechRecognizerにしたバージョン

	static final String TAG = "UntouchableTimerActivity";
	{
		Log.d(TAG, "@@@---start---@@@");
	}

	static TextView tv;
	static TextView tv_message1;
	static TextView tv_sensor_message;
	static Context mContext;

	private SpeechRecognizer rec;

	private SensorManager sensorMgr;
	private boolean hasSensor;
	
	//せっかく設定したがダイアログが終了したという情報がないと役にたってない
	private boolean isSensorOnOk = true;
	
	private Vibrator vibrator;
	private MediaPlayer sensorcatch;

	private boolean pref_vibrator;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate1");
		// このsetContentView(R.layout.main);前後４行いらないみたい？
		// CustomTitleBar
		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		// requestWindowFeature(Window.FEATURE_LEFT_ICON);

		setContentView(R.layout.main);

		// setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,R.drawable.icon);
		// CustomTitleBar
		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
		// R.layout.custom_title);

		// 音声認識
		rec = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
		rec.setRecognitionListener(new speechListenerAdp());

		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		hasSensor = false;
		mContext = this;
		tv = (TextView) findViewById(R.id.CountdownTimer);
		tv.setTextColor(Color.BLACK);
		tv.setText("00:00");

		tv_message1 = (TextView) findViewById(R.id.message1);
		tv_message1.setText("");
		tv_sensor_message = (TextView) findViewById(R.id.sensor_message);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		sensorcatch = MediaPlayer.create(mContext, R.raw.sensorcatch);

		// ハードウェアキーによる音量設定をONにしておく
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// 自動的に画面ロックしないようにする
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// プリファレンスの値を読み込む
		loadSetting();

		// 画面メッセージ
		tv_sensor_message.setText(R.string.message_waiting);

		List<Sensor> sensors = sensorMgr.getSensorList(Sensor.TYPE_PROXIMITY);
		if (sensors.size() > 0) {
			// センサーリスナー開始
			Sensor sensor = sensors.get(0);
			hasSensor = sensorMgr.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		saveSetting();

		// センサーリスナー終了
		if (hasSensor) {
			sensorMgr.unregisterListener(this);
			hasSensor = false;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		// サービスのストップ(カウントダウン中断)
		Intent intent = new Intent(mContext, TimerService.class);
		mContext.stopService(intent);
		// 自動的に画面ロックしないようにするのを解除
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub

		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	// メニューで何か選択された場合
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		//フラグ　今は役にたってない
		isSensorOnOk = false;
		//通常メッセージをセットしておく
		tv_sensor_message.setText(R.string.message_waiting);

		switch (item.getItemId()) {
		case R.id.menu_setting:
			Intent settingIntent = new Intent(this, SettingActivity.class);
			startActivity(settingIntent);
			isSensorOnOk = true;
			break;
		case R.id.menu_help:
			showHelpDialog();
			isSensorOnOk = true;
			break;
		case R.id.menu_about:
			showAboutDialog();
			isSensorOnOk = true;
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	// センサーを感知した時
	public void onSensorChanged(SensorEvent event) {
		if ((event.sensor.getType() == Sensor.TYPE_PROXIMITY) && (isSensorOnOk)) {
			if (event.values[0] < 1.0) // 近接センサーで「近い」
			{
				// サービスのストップ(カウントダウン中断)
				Intent intentTimer = new Intent(mContext, TimerService.class);
				mContext.stopService(intentTimer);
				showTime(0);

				// SpeechRecognizer
				Log.d(TAG, "onSensorChanged1");
				rec.startListening(RecognizerIntent
						.getVoiceDetailsIntent(getApplicationContext()));
				// 画面メッセージ
				tv_sensor_message.setText("");

				// ヴァイブレーションさせる
				if (pref_vibrator) {
					vibrator.vibrate(50);
				}
				// 音を出す
				sensorcatch.start();
			}
		}
	}

	private void loadSetting() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		pref_vibrator = pref.getBoolean(
				(String) getResources().getText(R.string.pref_key_vibrator),
				true);
	}

	private void saveSetting() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor edt = pref.edit();
		edt.putBoolean(
				(String) getResources().getText(R.string.pref_key_vibrator),
				pref_vibrator);
		edt.commit();
	}

	// 画面のカウント更新
	static void showTime(int timeSeconds) {
		SimpleDateFormat form = new SimpleDateFormat("mm:ss");
		tv.setText(form.format(timeSeconds * 1000));
		// 画面メッセージ
		tv_sensor_message.setText(R.string.message_cancel);
	}

	// カウントダウン処理
	public static void onTimerChanged(int counter) {
		showTime(counter);
	}

	// ヘルプ
	private void showHelpDialog() {
		TextView t = new TextView(this);
		t.setText("This is help Dialog Atodekaku");
		t.setTextSize(18f);
		t.setTextColor(0xFFCCCCCC);
		t.setLinkTextColor(0xFF9999FF);
		t.setPadding(20, 8, 20, 8);
		AlertDialog.Builder dlg = new AlertDialog.Builder(this)
				.setTitle(getResources().getText(R.string.app_name))
				.setIcon(R.drawable.icon).setView(t).setCancelable(true);
		dlg.create().show();
	}

	// アプリの説明
	private void showAboutDialog() {
		TextView t = new TextView(this);

		t.setAutoLinkMask(Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
		t.setText(String.format(
				(String) getResources().getText(R.string.app_dlg_format),
				getResources().getText(R.string.app_version), getResources()
						.getText(R.string.app_release),
				getResources().getText(R.string.app_author), getResources()
						.getText(R.string.app_author_mail)));

		// t.setText(String.format((String)getResources().getText(R.string.app_dlg_format),
		// getResources().getText(R.string.app_version),
		// getResources().getText(R.string.app_release),
		// getResources().getText(R.string.app_author),
		// getResources().getText(R.string.app_author_mail),
		// getResources().getText(R.string.app_author_url)));
		t.setTextSize(18f);
		t.setTextColor(0xFFCCCCCC);
		t.setLinkTextColor(0xFF9999FF);
		t.setPadding(20, 8, 20, 8);
		AlertDialog.Builder dlg = new AlertDialog.Builder(this)
				.setTitle(getResources().getText(R.string.app_name))
				.setIcon(R.drawable.icon).setView(t).setCancelable(true);
		dlg.create().show();
	}

	//SpeechRecognizerのリスナークラス
	private class speechListenerAdp implements RecognitionListener {

		public void onBeginningOfSpeech() {
			Log.d(TAG, "Start onBeginningOfSpeech");
		}

		public void onBufferReceived(byte[] buffer) {
			Log.d(TAG, "Start reconBufferReceivedognize");
		}

		public void onEndOfSpeech() {
			Log.d(TAG, "Start onEndOfSpeech");
		}

		public void onError(int error) {
			Log.d(TAG, "Start onError");
			// 画面メッセージ
			tv_sensor_message.setText(R.string.message_error_recognize);
		}

		public void onEvent(int eventType, Bundle params) {
			Log.d(TAG, "Start onEvent");
		}

		public void onPartialResults(Bundle partialResults) {
			Log.d(TAG, "Start onPartialResults");
		}

		public void onReadyForSpeech(Bundle params) {
			Log.d(TAG, "Start onReadyForSpeech");
			// 画面メッセージ
			tv_sensor_message.setText(R.string.message_talk_to_phone);
		}

		public void onResults(Bundle results) {
			Log.d(TAG, "Start onResults");
			ArrayList<String> strList = results
					.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION); // 音声認識結果を取得

			int result = SpeechAnalyzer.speechToSecond(strList); // 音声認識結果から秒数値を取得する、この値をタイマーにセットする

			Log.d(TAG, "onActivityResult result:" + result);
			if (result != 0) {
				// タイマーサービススタート
				Intent intent = new Intent(mContext, TimerService.class);
				intent.putExtra("counter", result);
				startService(intent);

			} else {
				// 取得した文字列が数値じゃなければエラーメッセージ的+エラー音
				tv_sensor_message.setText(R.string.message_error_recognize);
				long[] pattern = { 0, 1000, 500, 300, 100, 50 };
				vibrator.vibrate(pattern, -1);

			}

		}

		public void onRmsChanged(float rmsdB) {
			Log.d(TAG, "Start onRmsChanged");
		}
		
	}
}
