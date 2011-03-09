package edu.ucla.cs.SmartAlarm;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class ServiceStarter extends TabActivity{
	static boolean serviceOn = false;
	
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
}
