package edu.ucla.cs.SmartAlarm;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

public class SettingsActivity extends ListActivity{
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String[] listItems = {"Connect Bluetooth"};
        
        setListAdapter(new ArrayAdapter<String>(this, R.layout.setting, listItems));
    
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, 
        			int position, long id) {
        		Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
        		
        	}
        				
        });
     
	}
}
