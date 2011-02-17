package edu.ucla.cs.SmartAlarm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ServiceStarter extends Activity{
	Button buttonStart, buttonStop;
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.servicestarter);
    }
    
	public void SASSH(View view) {
		switch (view.getId()) {
	      case R.id.buttonStart:
		        startService(new Intent(this, SAService.class));
		        break;
		      case R.id.buttonStop:
		        stopService(new Intent(this, SAService.class));
		        break;
		}
	}
}
