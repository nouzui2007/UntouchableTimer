package com.yochiyochi.android.UntouchableTimer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class UntouchableTimerActivity extends Activity implements
		SensorEventListener {

	// 言い訳
	// メモがわりにいろいろ書いてるけど、後でコメントきれいにします
	// Javadocのことも薄々覚えてます

	// ヘルプ画面はもうちょっと何とかしないとなあー

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

	private Vibrator vibrator;

	private MediaPlayer sensorcatch;

	AudioManager am;
	SeekBar ringVolSeekBar;
	TextView ringVolText;

	private String pref_sound;
	private boolean pref_vibrator;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate1");

		setContentView(R.layout.main);

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
		Log.d(TAG, "onCreate2");
		sensorcatch = MediaPlayer.create(mContext, R.raw.sensorcatch);
		//add by makiuchi
		sensorcatch.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				startSpeechRecognizer();
			}
		});
		findViewById(R.id.Button01).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sensorcatch.start();
			}
		});
		//add by makiuchi end
		
		// 自動的に画面ロックしないようにする
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Log.d(TAG, "onCreate3");

	}

	@Override
	protected void onResume() {
		super.onResume();
		// プリファレンスの値を読み込む
		loadSetting();

		// アラーム音量
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int ringVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC); // 音量の取得
		ringVolSeekBar = (SeekBar) findViewById(R.id.ringVolSeekBar); // 音量シークバー
		ringVolSeekBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)); // 最大音量の設定

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


		// サービスのストップ(カウントダウン中断)
		Intent intent = new Intent(mContext, TimerService.class);
		mContext.stopService(intent);

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

		// 通常メッセージをセットしておく
		tv_sensor_message.setText(R.string.message_waiting);

		switch (item.getItemId()) {
		case R.id.menu_setting:
			Intent settingIntent = new Intent(this, SettingActivity.class);
			Log.d(TAG, "SettingActivity yobidasi");
			startActivity(settingIntent);
			break;
		case R.id.menu_help:
			// アラーム画面の起動
			Intent intent = new Intent(mContext, HelpActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;
		case R.id.menu_about:
			showAboutDialog();
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
		if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
			if (event.values[0] < 1.0) // 近接センサーで「近い」
			{
				//add by makiuchi
				sensorcatch.start();
				// 音声認識スタート
				//comment out by makiuchi
//				startSpeechRecognizer();

			}
		}
	}

	// 音声認識スタート
	private void startSpeechRecognizer() {
		// 音を出す
//		sensorcatch.start();
		// 画面メッセージ
		tv_sensor_message.setText("");
		// サービスのストップ(カウントダウン中断)
		Intent intentTimer = new Intent(mContext, TimerService.class);
		mContext.stopService(intentTimer);
		tv.setText("00:00");

		// SpeechRecognizer
		rec.startListening(RecognizerIntent
				.getVoiceDetailsIntent(getApplicationContext()));

		// ヴァイブレーションさせる
		vibrator.vibrate(30);
	}

	// プリファレンスの値を読み込む
	private void loadSetting() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		pref_sound = pref.getString(
				(String) getResources().getText(R.string.pref_key_sound), "");
		Log.d(TAG, "loadSetting() pref_sound =" + pref_sound);
		if (pref_sound == null)
			pref_sound = "2";
		else if (pref_sound.equals(""))
			pref_sound = "2";
		String[] sounds = getResources().getStringArray(R.array.entries);

		String selected_sound = sounds[Integer.parseInt(pref_sound) - 1];
		Log.d(TAG, "loadSetting() selected_sound =" + selected_sound);
		pref_vibrator = pref.getBoolean(
				(String) getResources().getText(R.string.pref_key_vibrator),
				true);
		Log.d(TAG, "loadSetting() pref_vibrator =" + pref_vibrator);
	}

	private void saveSetting() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor edt = pref.edit();
		edt.putString((String) getResources().getText(R.string.pref_key_sound),
				pref_sound);
		Log.d(TAG, "saveSetting() pref_key_sound =" + ((String) getResources().getText(R.string.pref_key_sound)));
		Log.d(TAG, "saveSetting() pref_sound =" + pref_sound);
		edt.putBoolean(
				(String) getResources().getText(R.string.pref_key_vibrator),
				pref_vibrator);
		Log.d(TAG, "saveSetting() pref_key_vibrator =" + ((String) getResources().getText(R.string.pref_key_vibrator)));
		Log.d(TAG, "saveSetting() pref_vibrator =" + pref_vibrator);
		edt.commit();
	}

	// 画面のカウント更新
	static void showTime(long timeSeconds) {
		Log.d(TAG, "showTime timeSeconds=" + timeSeconds);
		SimpleDateFormat form = new SimpleDateFormat("mm:ss");
		tv.setText(form.format(timeSeconds * 1000));
		// 画面メッセージ
		tv_sensor_message.setText(R.string.message_cancel);
	}

	// カウントダウン処理
	public static void onTimerChanged(long counter) {
		showTime(counter);
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

		t.setTextSize(18f);
		t.setTextColor(0xFFCCCCCC);
		t.setLinkTextColor(0xFF9999FF);
		t.setPadding(20, 8, 20, 8);
		AlertDialog.Builder dlg = new AlertDialog.Builder(this)
				.setTitle(getResources().getText(R.string.app_name))
				.setIcon(R.drawable.icon).setView(t).setCancelable(true);
		dlg.create().show();
	}

	// SpeechRecognizerのリスナークラス
	private class speechListenerAdp implements RecognitionListener {

		public void onResults(Bundle results) {

			ArrayList<String> strList = results
					.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION); // 音声認識結果を取得
			Log.d(TAG, "onActivityResult Arraylist:" + strList);

			int result = SpeechAnalyzer.speechToSecond(strList); // 音声認識結果から秒数値を取得する、この値をタイマーにセットする

			Log.d(TAG, "onActivityResult result:" + result);
			if (result != 0) {
				// タイマーサービススタート
				Intent intent = new Intent(mContext, TimerService.class);
				intent.putExtra("counter", result);
				startService(intent);

			} else {
				// 取得した文字列が数値じゃなければエラーメッセージ
				tv_sensor_message.setText(R.string.message_error_analyze);
				// long[] pattern = { 0, 1000, 500, 300, 100, 50 };
				// vibrator.vibrate(pattern, -1);

			}
		}

		public void onBeginningOfSpeech() {
			// 画面メッセージ
			tv_sensor_message.setText(R.string.message_wait_recognize);
		}

		public void onBufferReceived(byte[] buffer) {
		}

		public void onEndOfSpeech() {
			// 画面メッセージ
			tv_sensor_message.setText(R.string.message_wait_analyze);
		}

		public void onError(int error) {
			// 画面メッセージ（一瞬）
			tv_sensor_message.setText(R.string.message_error_recognize);
			// 音声認識スタート
//			startSpeechRecognizer();
		}

		public void onEvent(int eventType, Bundle params) {
		}

		public void onPartialResults(Bundle partialResults) {
		}

		public void onReadyForSpeech(Bundle params) {
			// 画面メッセージ
			tv_sensor_message.setText(R.string.message_talk_to_phone);
		}

		public void onRmsChanged(float rmsdB) {
		}

	}

}
