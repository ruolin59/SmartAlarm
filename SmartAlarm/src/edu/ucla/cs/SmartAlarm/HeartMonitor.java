/*
* Copyright (C) 2009 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package edu.ucla.cs.SmartAlarm;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

/**
* This is the main Activity that displays the current chat session.
* This is the heart monitoring portion of the smart alarm
*/
public class HeartMonitor extends Activity {
    // Debugging
    private static final String TAG = "HeartMonitor";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    
    // Layout Views
    private TextView mTitle;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private HeartMonitorService mChatService = null;

    //for keeping track of time
    private long timeZero = SystemClock.elapsedRealtime();
    private long clk = 0;
    private final int CONVFCTR = 100;
    
    private boolean pa = false; //pause
    
    //Variables for making threshold
    //ArrayList<Integer> prvEcgData; //Previous buffer
    private int setup = 5;
    private double thresh;
    private double RMS;
    private double curMax = -9999;
    private double curMin = 9999;
    private double total = 0;
    
    private int curPeak;
private double curCount;
    private double prevCount;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
              
        Toast.makeText(this, "blutooth initialized" + mBluetoothAdapter.getName(), Toast.LENGTH_LONG).show();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == HeartMonitorService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);

        // Initialize the HeartMonitorService to perform bluetooth connections
        mChatService = new HeartMonitorService(this, mHandler);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }
    
    public static String getHexString(byte[] b, int length){
     String result = "";
     for (int i=0; i < length; i++) {
     result +=
     Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
     }
     return result;
    }

    // The Handler that gets information back from the HeartMonitorService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case HeartMonitorService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    mConversationArrayAdapter.clear();
                    break;
                case HeartMonitorService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case HeartMonitorService.STATE_LISTEN:
                case HeartMonitorService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;

                // construct a string from the valid bytes in the buffer
                String readMessage = getHexString(readBuf, msg.arg1);
                
                new PacketHeader(readMessage);
                
                String dataStr = readMessage.substring(12); //this is where the ECG starts
               
                Ecg myECG = new Ecg(dataStr);
                ArrayList<Integer> ecgData = myECG.getData();

             total = 0;
                if(setup > 0){
                 for(int i = 0; i < ecgData.size(); i++){
                 if(ecgData.get(i) > curMax){
                 curMax = ecgData.get(i);
                 }
                 if(ecgData.get(i) < curMin){
                 curMin = ecgData.get(i);
                 }
                 total += (ecgData.get(i)*ecgData.get(i));
                 }
             total /= ecgData.size();
             RMS = Math.sqrt(total);
                 if(setup != 5)
                 thresh = (thresh*0.75 + ((RMS + (curMax-curMin)/ecgData.size()))*0.25);
                 else
                 thresh = RMS + (curMax-curMin)/2;
                 setup--;
                }else if(!pa){
                 clk = (SystemClock.elapsedRealtime() - timeZero)/CONVFCTR;

                 String printTxt = "Threshold: " + thresh + " clock: " + clk/10;
                 for (int i = 0; i < ecgData.size(); i++)
                 printTxt += ecgData.get(i) + "\n";
                
                    TextView tv = new TextView(getApplicationContext());
                 tv.setText(printTxt);
                 setContentView(tv);
       
                 for(int i = 0; i < ecgData.size(); i++){
                 if(ecgData.get(i) > curMax){
                 curMax = ecgData.get(i);
                 }
                 if(ecgData.get(i) < curMin){
                 curMin = ecgData.get(i);
                 }
                 total += (ecgData.get(i)*ecgData.get(i));
                
                 }
             total /= ecgData.size();
             RMS = Math.sqrt(total);
             thresh = (thresh*0.75 + ((RMS + (curMax-curMin)/ecgData.size()))*0.25)+20;
            
             calcHR(ecgData);
                }
                
            
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    /*calculate the heart rate */
    public int calcHR(ArrayList<Integer> heartDat){
     int rate = 0;
     for (int i = 0; i < heartDat.size(); i++){
     if(heartDat.get(i) > curPeak && heartDat.get(i) > thresh){
     curPeak = heartDat.get(i);
     curCount = 0;
     }
curCount++;
prevCount++;
     }
    
     return rate;
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mChatService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
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
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.pause:
         if(pa)
         pa = false;
         else
         pa = true;
         return true;
        }
        return false;
    }

}