package edu.ucla.cs.SmartAlarm;

import java.util.ArrayList;
import java.util.Calendar;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class SAService extends Service {
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private HeartMonitorService mChatService = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	
	// variable for alarm
    public static int wakeupTime = 480;	// In minutes
    public static int wakeupRange = 30;	// In minutes
	public static boolean alarmSounded = true;
	public static boolean simulate = false;
	
	// Calculated variables
	private double thresh = 0;
	static double avgRate = 75;
	
	// Heart rate calculation variables
	private double interval = 0;
	private boolean above = false;
	
	// Global constants
	private static final double SAMPLE_RATE = 300;
	private static final double RATE_FLOOR = 30;
	private static final double RATE_CELLING = 150;
	
	// variables for calculating heart rate
	private int numRates = 0;
	private double heartRateTotal = 0;


	// Name of the connected device
	private String mConnectedDeviceName = null;
	
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	private static final double LIGHT_SLEEP_RATE = 55;

	@Override
	public void onCreate() {
		// Setup a context variable for easy access
		Context context = getApplicationContext();

		// Toast service started
		Toast.makeText(context, "SAService Started", Toast.LENGTH_SHORT).show();

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		Toast.makeText(this, "Bluetooth initialized", Toast.LENGTH_SHORT).show();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_SHORT).show();
			stopSelf();
			return;
		}

		// If BT is not on, enable it.
		if (!mBluetoothAdapter.isEnabled()) {
			Toast.makeText(this, "Turning on Bluetooth", Toast.LENGTH_SHORT).show();
			mBluetoothAdapter.enable();
		}

		// Wait for Bluetooth to turn on
		while (!mBluetoothAdapter.isEnabled());

		// Now that we have Bluetooth enabled, setup communication
		if (mChatService == null) {
			setupChat();
		}
	}

	@Override
	public void onDestroy() {
		Context context = getApplicationContext();
		Toast.makeText(context, "SAService Stopped", Toast.LENGTH_SHORT).show();
		// Stop chat service
		if (mChatService != null) {
			mChatService.stop();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private void setupChat() {
		// Initialize the array adapter for the conversation thread
		mConversationArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.message);

		// Initialize the HeartMonitorService to perform Bluetooth connections
		mChatService = new HeartMonitorService(this, mHandler);
	}

	public static String getHexString(byte[] b, int length) {
		String result = "";
		for (int i = 0; i < length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	/* calculate the instantaneous heart rate */
	private double calcHR(ArrayList<Integer> heartData) {
		ArrayList<Double> intervals = new ArrayList<Double>();
		
		for(Integer data : heartData)
		{
			interval++;
			// if we exceeded the threshold
			if ((!above) && data > thresh)
			{
				above = true;
				// sometimes one beat may shift around the threshold, 
				// we filter out intervals that seem to be too small
				if (interval/SAMPLE_RATE > 60/(RATE_CELLING))
				{
					intervals.add(interval/SAMPLE_RATE);
					interval = 0;
				}
			}
			else if (above && data < thresh)
			{
				above = false;
			} 
		}
		
		int sum = 0;
		for (Double intv : intervals)
		{
			sum += 60/(intv);
		}
		
		return intervals.isEmpty()?-1:sum/intervals.size();	
	}

	private void updateThresh(ArrayList<Integer> ecgData)
	{
		double RMS, total, curMax, curMin, curThresh;
		// Initialize variables before calculation
		RMS = 0;
		total = 0;
		curMax = -9999;
		curMin = 9999;
		
		// Calculate the max, min, and total
		for (Integer data : ecgData)
		{
			if (data > curMax)
				curMax = data;
			if (data < curMin)
				curMin = data;
			total += data*data;
		}
		
		// Find the root mean square
		RMS = Math.sqrt(total/ecgData.size());
		
		// Calculate the threshold for this packet
		curThresh = RMS + (curMax - RMS) / 2;
		
		// If we haven't set the threshold before
		if (thresh == 0)
			thresh = curThresh;
		// Set the threshold in a weighted fashion
		else
			thresh = 0.5 * thresh + 0.5 * curThresh;		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// We receive messages from the GUI here
		String address = intent.getStringExtra("edu.ucla.cs.SmartAlarm.address");

		// If this intent carries the address
		if (address != null) {
			Toast.makeText(this, "Got device address: " + address, Toast.LENGTH_SHORT).show();

			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
			// Attempt to connect to the device
			mChatService.connect(device);
		}

		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}
	
	public void alarm()
	{
		alarmSounded = true;
		simulate = false;
		Intent dialogIntent = new Intent(getBaseContext(), WakeupActivity.class);
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(dialogIntent);
	}

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

				// this is where the ECG data starts
				String dataStr = readMessage.substring(12); 

				// Parse the data string to get the integer values
				Ecg myECG = new Ecg(dataStr);
				ArrayList<Integer> ecgData = myECG.getData();

				// Update the threshold
				updateThresh(ecgData);

				// calculate the current Heart rate
				double curRate = calcHR(ecgData);
				if (simulate)
				{
					curRate = 35;
				}
				
				//filter out improbable heart rates
				if (curRate > RATE_FLOOR && curRate < RATE_CELLING){	
					numRates++;
					heartRateTotal += curRate; 
					avgRate = heartRateTotal/numRates;
				}
				
				final Calendar ca = Calendar.getInstance();
				
				int curTime = ca.get(Calendar.HOUR_OF_DAY)*60 + ca.get(Calendar.MINUTE);
				// refresh rates once in a while
				if (ca.get(Calendar.SECOND) == 15)
				{
					if (withinRange(curTime) && avgRate<LIGHT_SLEEP_RATE && (!alarmSounded))
					{
						alarm();
					}
				}

				
				// refresh rates once in a while
				if (ca.get(Calendar.SECOND) == 0)
				{
					numRates = 0;
					heartRateTotal = 0;
					avgRate = 0;
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
				break;
			}
		}

		private boolean withinRange(int curTime) {
			return (Math.abs(curTime-wakeupTime) < wakeupRange);
		}
	};

}
