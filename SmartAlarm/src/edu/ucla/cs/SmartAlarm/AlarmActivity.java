package edu.ucla.cs.SmartAlarm;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.ToggleButton;

public class AlarmActivity extends Activity{
	
	private Button mPickTime;

    static int mHour;
    static int mMinute;

    static final int TIME_DIALOG_ID = 0;
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case TIME_DIALOG_ID:
            return new TimePickerDialog(this,
                    mTimeSetListener, mHour, mMinute, false);
        }
        return null;
    }
    
    // the callback received when the user "sets" the time in the dialog
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
        new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mHour = hourOfDay;
                mMinute = minute;
                mPickTime.setText("Alarm: " + mHour + " hr " + mMinute + " min");
                if (ServiceStarter.serviceOn)
                {
                	SAService.wakeupTime = hourOfDay*60 + minute;
                	SAService.alarmSounded = false;
                }
            }
        };
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.time_pick);
		
		final ToggleButton button = (ToggleButton) findViewById(R.id.togglebutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(button.isChecked())
            	{
                    // Launch the DeviceListActivity to see devices and do scan
    		        startService(new Intent(getApplicationContext(), SAService.class));
    		        ServiceStarter.serviceOn = true;
    		        
    		        // Go grab the device list
                    Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                    startActivityForResult(serverIntent, 1);
            	}
            	else
            	{
    		        stopService(new Intent(getApplicationContext(), SAService.class));
    		        ServiceStarter.serviceOn = false;
            	}
            }
        });
        
        mPickTime = (Button) findViewById(R.id.pickTime);
        mPickTime.setText("Alarm: " + mHour + " hr " + mMinute + " min");
        
        mPickTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID);
            }
        });
		
	}
	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case 1:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                
                // Send the adderss to the service
                Intent msgToService = new Intent(this, SAService.class);
                msgToService.putExtra("edu.ucla.cs.SmartAlarm.address", address);
                startService(msgToService);
            }
            break;
        }
    }
}
