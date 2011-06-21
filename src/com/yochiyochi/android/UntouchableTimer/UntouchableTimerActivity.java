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
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class UntouchableTimerActivity extends Activity implements SensorEventListener{
	
	//言い訳
	//メモがわりにいろいろ書いてるけど、後でコメントきれいにします
	//Javadocのことも薄々覚えてます
	
    static final String TAG = "UntouchableTimerActivity";
    {  Log.d(TAG, "@@@---start---@@@"); }



	static TextView tv;
	static Context mContext;
	static int timeLeft=0;
	private SensorManager sensorMgr;
	private boolean hasSensor;
	private static final int REQUEST_CODE = 7856;


	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.d(TAG, "onCreate1");
        sensorMgr = (SensorManager)getSystemService(SENSOR_SERVICE);
        hasSensor = false;
        mContext=this;
        Log.d(TAG, "onCreate2");
    	tv=(TextView)findViewById(R.id.CountdownTimer);
        Log.d(TAG, "onCreate3");
    }
    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	List<Sensor> sensors = sensorMgr.getSensorList(Sensor.TYPE_PROXIMITY);
    	if(sensors.size() > 0)
    	{
    		//センサーリスナー開始
    		Sensor sensor = sensors.get(0);
    		hasSensor = sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    	}
    }
    
    @Override
    protected void onPause()
    {
    	super.onPause();
    	//センサーリスナー終了
    	if(hasSensor)
    	{
    		sensorMgr.unregisterListener(this);
    		hasSensor = false;
    	}
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }
    
    @Override
    //センサーを感知した時
    public void onSensorChanged(SensorEvent event)
    {
    	if(event.sensor.getType() == Sensor.TYPE_PROXIMITY)
    	{
    		if(event.values[0] < 1.0)	// 近接センサーで「近い」
    		{
    			//サービスのストップ(カウントダウン中断)
                Intent intent = new Intent(mContext, TimerService.class);
                mContext.stopService(intent);

                //RecognizerIntentクラス=音声認識
   				intent = new Intent();
   				intent.setAction(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
   				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
   				try
				{
					startActivityForResult(intent, REQUEST_CODE);
				}
				catch (ActivityNotFoundException e)
				{
					Toast.makeText(UntouchableTimerActivity.this,
							"音声認識がインストールされていません", Toast.LENGTH_LONG).show();
				}
				//音を出す
//    			vibrator.vibrate(50);
    		}
    	}
    }
        @Override
        public void onActivityResult(int req, int res, Intent data)
        {
        	if(res == Activity.RESULT_OK && req == REQUEST_CODE)
        	{
        		String resText;
        		ArrayList<String> strList = 
        			data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        		resText = strList.get(0);	// 1つめの認識候補のみを採用
        		
        		//文字解析メソッド　最初は簡単に
        		if (parseResult(resText)) {
            		//タイマーサービススタート
                    Intent intent = new Intent(mContext, TimerService.class);
                    intent.putExtra("counter", timeLeft);
                    startService(intent);
        		} else {
            		//取得した文字列が数値じゃなければエラーメッセージ的+エラー音
        			//後で書く
        		}
        	}
        }

		//文字解析メソッド　最初は簡単に
        static boolean parseResult(String resText) {
			// 後でパターンマッチ処理を書く
			return true;
		}

		//画面のカウント更新
    	static void showTime(int timeSeconds){
    		SimpleDateFormat form=new SimpleDateFormat("mm:ss");
    		tv.setText(form.format(timeSeconds*1000));
    	}

    	//カウントダウン処理
    	public static void onTimerChanged(int counter){
    		showTime(counter);
    		timeLeft=counter;
    	}

}
