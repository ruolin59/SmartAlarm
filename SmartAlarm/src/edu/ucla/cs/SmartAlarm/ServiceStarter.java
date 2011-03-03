package edu.ucla.cs.SmartAlarm;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;

public class ServiceStarter extends TabActivity{
	Button buttonStart, buttonStop;
    //@Override
	//public void onCreate(Bundle savedInstanceState) {
    //  super.onCreate(savedInstanceState);
    //  setContentView(R.layout.servicestarter);
    //}
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabhost);
		
	    TabHost tabhost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Reusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each
	    
	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, AlarmActivity.class);
	    
	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabhost.newTabSpec("Home");
	    spec.setContent(intent);
	    spec.setIndicator("Home");
	    tabhost.addTab(spec);
	    
	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, SettingsActivity.class);
	    
	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabhost.newTabSpec("Settings");
	    spec.setContent(intent);
	    spec.setIndicator("Settings");
	    tabhost.addTab(spec);
	    
	    tabhost.setCurrentTab(0);

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
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, 1);
            return true;
        }
        return false;
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
