package com.yochiyochi.android.UntouchableTimer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class UntouchableTimerActivity extends Activity implements
		SensorEventListener {

	// 言い訳
	// メモがわりにいろいろ書いてるけど、後でコメントきれいにします
	// Javadocのことも薄々覚えてます
	
	//メニューの部分はまだダミー状態

	static final String TAG = "UntouchableTimerActivity";
	{
		Log.d(TAG, "@@@---start---@@@");
	}

	static TextView tv;
	static TextView tv_sensor_message;
	static Context mContext;
//	static int timeLeft = 0;
	private SensorManager sensorMgr;
	private boolean hasSensor;
	private static final int REQUEST_CODE = 7856;
	private Vibrator vibrator;
	private MediaPlayer sensorcatch;

	//どっちがいいか検討中　画面消さない方法
	public PowerManager.WakeLock wl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		hasSensor = false;
		mContext = this;
		tv = (TextView) findViewById(R.id.CountdownTimer);
		tv_sensor_message = (TextView) findViewById(R.id.sensor_message);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		sensorcatch = MediaPlayer.create(mContext, R.raw.sensorcatch);

		// 自動的に画面ロックしないようにする
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		//画面メッセージ
		tv_sensor_message.setText(R.string.hand_sensor);

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
	
	//メニューで何か選択された場合
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    super.onOptionsItemSelected(item);
	    switch(item.getItemId()){
	    case R.id.item1 :
	        //何か処理
	        return true;
	    }
	    return false;
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
				// サービスのストップ(カウントダウン中断)
				Intent intentTimer = new Intent(mContext, TimerService.class);
				mContext.stopService(intentTimer);
				showTime(0);

				// RecognizerIntentクラス=音声認識
				Intent intent = new Intent();
				intent.setAction(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
						RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
				//プロンプトに表示する文字列を指定
				intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
						"３分、２０秒、１分３０秒のようにお話下さい");
				//画面メッセージ
				tv_sensor_message.setText("");

				try {
					startActivityForResult(intent, REQUEST_CODE);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(UntouchableTimerActivity.this,
							"音声認識がインストールされていません", Toast.LENGTH_LONG).show();
				}
				// ヴァイブレーションさせる
				vibrator.vibrate(50);
				// 音を出す
				sensorcatch.start();
			}
		}
	}

	@Override
	public void onActivityResult(int req, int res, Intent data) {
		Log.d(TAG, "onActivityResult" + RecognizerIntent.getVoiceDetailsIntent(getApplicationContext()));
		//実験：後で消す
/*		if (res == RecognizerIntent.RESULT_AUDIO_ERROR && req == REQUEST_CODE) {
			//実験：後で消す
			Log.d(TAG, "onActivityResult");
			finish();
		}
*/		if (res == Activity.RESULT_OK && req == REQUEST_CODE) {
			ArrayList<String> strList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);	// 音声認識結果を取得
    		int result = SpeechAnalyzer.speechToSecond(strList);		// 音声認識結果から秒数値を取得する、この値をタイマーにセットする

			Log.d(TAG, "onActivityResult result:" + result);
			if (result != 0) {				
				// タイマーサービススタート
				Intent intent = new Intent(mContext, TimerService.class);
				intent.putExtra("counter", result);
				startService(intent);

			} else {
				// 取得した文字列が数値じゃなければエラーメッセージ的+エラー音
				Toast.makeText(UntouchableTimerActivity.this,
						"うまく認識出来ませんでした", Toast.LENGTH_LONG).show();
				long[] pattern = {0,1000,500,300,100,50};  
				vibrator.vibrate(pattern, -1);  
			}
		}
	}

	// 画面のカウント更新
	static void showTime(int timeSeconds) {
		SimpleDateFormat form = new SimpleDateFormat("mm:ss");
		tv.setText(form.format(timeSeconds * 1000));
		//画面メッセージ
		tv_sensor_message.setText(R.string.hand_cancel);
	}

	// カウントダウン処理
	public static void onTimerChanged(int counter) {
		showTime(counter);
//		timeLeft = counter;
	}

}
