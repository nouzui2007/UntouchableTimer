package com.yochiyochi.android.UntouchableTimer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;

//これは後でクラス名からして書き変える
public class AlarmDialog extends Activity {

	Ringtone rt;
	Vibrator vib;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.alarmdialog);
		vib=(Vibrator)getSystemService(VIBRATOR_SERVICE);

		findViewById(R.id.Button01).setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(rt!=null)rt.stop();
				vib.cancel();
				finish();
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences prefs;
		prefs = this.getSharedPreferences("CountdownTimerPrefs", 0);
		Uri fn=Uri.parse(prefs.getString("alarm", ""));
		
		if(fn!=null){
			rt=RingtoneManager.getRingtone(this, fn);
			System.out.println(fn.toString());
		}
		else{
			rt=RingtoneManager.getRingtone(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
		}
		if(rt!=null){
			if(!rt.isPlaying())rt.play();
		}

		if(prefs.getBoolean("vibrator", true))
			vib.vibrate(new long[]{0,1000,500,1000,500,1000}, -1);
	}

}
