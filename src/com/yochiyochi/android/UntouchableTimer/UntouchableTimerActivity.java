package com.yochiyochi.android.UntouchableTimer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Toast;

public class UntouchableTimerActivity extends Activity implements SensorEventListener{
	private SensorManager sensorMgr;
	private boolean hasSensor;
	private static final int REQUEST_CODE = 16402;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        sensorMgr = (SensorManager)getSystemService(SENSOR_SERVICE);
        hasSensor = false;
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
    public void onSensorChanged(SensorEvent event)
    {
    	if(event.sensor.getType() == Sensor.TYPE_PROXIMITY)
    	{
    		//状態により処理を変える
    		//待ち状態・音声認識状態・タイマー実行状態・タイマー終了状態
    		if(event.values[0] < 1.0)	// 近接センサーで「近い」
    		{
   				Intent intent = new Intent();
   				intent.setAction(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
   				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
   				try
				{
					startActivityForResult(intent, REQUEST_CODE);
				}
				catch (ActivityNotFoundException e)
				{
					Toast.makeText(UntouchableTimerActivity.this, "音声認識がインストールされていません", Toast.LENGTH_LONG).show();
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
        		ArrayList<String> strList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        		resText = strList.get(0);	// 1つめの認識候補のみを採用
        		//文字解析メソッド　最初は簡単に
        		//取得した文字列が数値じゃなければ＼(^o^)／エラーメッセージ的な物エラー音ブブー
        		//タイマースタート
        	}
        }
        //オンなんとかなんとか
		//タイマーからの戻り値で
		//アラーム鳴らすおしまい
       
        //途中でセンサー感知　戻る判断
    
 
}