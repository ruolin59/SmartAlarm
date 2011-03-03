package edu.ucla.cs.SmartAlarm;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class Tabs extends TabActivity {
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	
	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	    
	    intent = new Intent().setClass(this, HeartMonitor.class);
		
	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("main").setIndicator("Main",
	                      res.getDrawable(R.drawable.ic_tab_settings))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, settingsActivity.class);
	
	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("settings").setIndicator("Settings",
	                      res.getDrawable(R.drawable.ic_tab_settings))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    
	
	    tabHost.setCurrentTab(0);
	}
}