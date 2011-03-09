package edu.ucla.cs.SmartAlarm;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WakeupActivity extends Activity{
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wakeup);
		
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.mj);
        mp.start();
        
		final Button button = (Button) findViewById(R.id.ok);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	mp.stop();
            	finish();
            }
        });
	}

}
