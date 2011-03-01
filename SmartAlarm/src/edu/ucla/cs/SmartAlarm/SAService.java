package edu.ucla.cs.SmartAlarm;

import java.util.ArrayList;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class SAService extends Service{
	
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private HeartMonitorService mChatService = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    
	@Override
    public void onCreate() {
		// Setup a context variable for easy access
		Context context = getApplicationContext();
		
		// Toast service started
		Toast.makeText(context, "SAService Started", Toast.LENGTH_SHORT).show();
		
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
              
        Toast.makeText(this, "Bluetooth initialized" + mBluetoothAdapter.getName(), Toast.LENGTH_SHORT).show();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }
        
        // If BT is not on, enable it.
        if (!mBluetoothAdapter.isEnabled()) {
        	// TODO: this may be bad since we don't ask the user
            Toast.makeText(this, "Turning on Bluetooth", Toast.LENGTH_SHORT).show();
            mBluetoothAdapter.enable();
        } 
        
        // Wait for Bluetooth to turn on
        // TODO: polling wait is pretty bad....
        while (!mBluetoothAdapter.isEnabled());
        
        // Now that we have Bluetooth enabled, setup communication
        if (mChatService == null) 
        {
        	setupChat();
        }
    }

    @Override
    public void onDestroy() {
		Context context = getApplicationContext();
		Toast.makeText(context, "SAService Stopped", Toast.LENGTH_SHORT).show();
		
		// Stop chat service
        if (mChatService != null)
        {
        	mChatService.stop();
        }
    }

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
    
    private void setupChat() {
        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);

        // Initialize the HeartMonitorService to perform bluetooth connections
        mChatService = new HeartMonitorService(this, mHandler);
    }
    
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;    
    //Variables for making threshold
    //ArrayList<Integer> prvEcgData; //Previous buffer
    private int setup = 5;
    private double thresh;
    private double RMS;
    private double curMax = -9999;
    private double curMin = 9999;
    private double total = 0;
    
    //variables for calculating heart rate
	private int curPeak;
	private double curCount;
    private double prevCount;
    //for keeping track of time
    private long timeZero = SystemClock.elapsedRealtime();
    private long clk = 0;
    private final int CONVFCTR = 100;
    
    private boolean pa = false;	//pause
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    // The Handler that gets information back from the HeartMonitorService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case HeartMonitorService.STATE_CONNECTED:
                    mConversationArrayAdapter.clear();
                    break;
                case HeartMonitorService.STATE_CONNECTING:
                case HeartMonitorService.STATE_LISTEN:
                case HeartMonitorService.STATE_NONE:
                    break;
                }
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;

                // construct a string from the valid bytes in the buffer
                String readMessage = getHexString(readBuf, msg.arg1);
                
                new PacketHeader(readMessage);
                
                String dataStr = readMessage.substring(12);	//this is where the ECG starts
               
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
    
    public static String getHexString(byte[] b, int length){
  	  String result = "";
  	  for (int i=0; i < length; i++) {
  	    result +=
  	          Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
  	  }
  	  return result;
  }
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
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

    	String address = intent.getStringExtra("edu.ucla.cs.SmartAlarm.address");

    	if (address != null)
            Toast.makeText(this, "Got device address: " + address, Toast.LENGTH_SHORT).show();  		
    	
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

}




