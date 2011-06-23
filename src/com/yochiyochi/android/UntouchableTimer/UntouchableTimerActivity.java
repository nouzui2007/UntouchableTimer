package com.yochiyochi.android.UntouchableTimer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.os.Vibrator;
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
	private Vibrator vibrator;
    private MediaPlayer sensorcatch;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        sensorMgr = (SensorManager)getSystemService(SENSOR_SERVICE);
        hasSensor = false;
        mContext=this;
    	tv=(TextView)findViewById(R.id.CountdownTimer);
        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        sensorcatch = MediaPlayer.create(mContext, R.raw.sensorcatch);
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
				//ヴァイブレーションさせる
    			vibrator.vibrate(50);
				//音を出す
    			sensorcatch.start();
    		}
    	}
    }
        @Override
        public void onActivityResult(int req, int res, Intent data)
        {
        	if(res == Activity.RESULT_OK && req == REQUEST_CODE)
        	{
        		ArrayList<String> strList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        		// ここまでは、音声認識結果を取得する常套句

        		// 以下、全ての音声認識候補に対して秒数を取得してみて、最も値の大きいもの(=最も期待通りに値を取れたもの)を採用
        		// 値(秒数)は、最終的には maxVal に入る。この値をタイマーに引き渡せばよい
        		int curVal, maxVal = 0, maxIdx = 0;
        		for(int i = 0; i < strList.size(); i++)
        		{
            		//文字解析メソッド
        			curVal = getSecondFromText(strList.get(i));
        			if(curVal > maxVal)
        			{
        				maxVal = curVal;
        				maxIdx = i;
        			}
        		}
        		
        		
        		if (maxVal != 0) {
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
        
    	//音声認識で取得したテキストから、秒数を抜き出す
    	//へんな入力だと０が返ってくる

        private int getSecondFromText(String str)
        {
        	String regexNum = "[0-9]+";		// 数字列を表現する正規表現、ほんとは [1-9][0-9]* かな
        	String regexHMS[] = { "(時間|じかん|ジカン)", "(分|ふん|ぷん|フン|プン)", "(秒|びょう|ビョウ)" };	// 時間、分、秒の音声誤認識となりそうなものをどんどん追加
        	Pattern ptn, ptnN;
        	Matcher mch, mchN;

//        	String s = str.replaceAll("\\s", "");	// 音声認識結果の単語間にスペースが開くことがあるので、詰める、と思ったが副作用が多そうなのでやめる
        	String s = str;
        	ptnN = Pattern.compile(regexNum);

        	int resSecond = 0;
        	for(int i = 0; i < 3; i++)	// 時間、分、秒の順に処理する
        	{
        		resSecond *= 60;
        		ptn = Pattern.compile(regexNum + regexHMS[i]);
        		mch = ptn.matcher(s);
        		if(mch.find())
        		{
        			mchN = ptnN.matcher(mch.group());
        			if(mchN.find())
        				resSecond += Integer.valueOf(mchN.group());
        		}
        	}
        	return resSecond;
        }

}
