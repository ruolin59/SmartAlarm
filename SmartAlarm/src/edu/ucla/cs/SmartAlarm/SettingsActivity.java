package edu.ucla.cs.SmartAlarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

public class SettingsActivity extends ListActivity{
	
	static final int RANGE_DIALOG_ID = 0;
	
	protected Dialog onCreateDialog(int id) {
        switch (id) {
        case RANGE_DIALOG_ID:
        	final CharSequence[] items = {"30 min", "1 hr", "1.5hr", "2 hr"};
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pick a range");
            builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
                }
            });
            AlertDialog alert = builder.create();
            return alert;
        }
        return null;
	}
	
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String[] listItems = {"Set Time Range", "Bluetooth", "Device Name"};
        
        
        setListAdapter(new ArrayAdapter<String>(this, R.layout.setting, listItems));
    
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, 
        			int position, long id) {
        			showDialog(RANGE_DIALOG_ID);
        	}
        				
        });
     
	}
}
