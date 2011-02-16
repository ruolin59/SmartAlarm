package edu.ucla.cs.SmartAlarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class SAService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
    public void onCreate() {
		Context context = getApplicationContext();

		Toast.makeText(context, "Service Created", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
    }
}

