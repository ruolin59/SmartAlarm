package edu.ucla.cs.SmartAlarm;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;
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
            }
        };
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.time_pick);
		
		final ToggleButton button = (ToggleButton) findViewById(R.id.togglebutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(button.isChecked())
    		        startService(new Intent(getApplicationContext(), SAService.class));
            	else
    		        stopService(new Intent(getApplicationContext(), SAService.class));
            }
        });
        
        mPickTime = (Button) findViewById(R.id.pickTime);
        
        mPickTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID);
            }
        });
		
	}

}
