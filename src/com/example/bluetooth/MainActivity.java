package com.example.bluetooth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import com.example.tools.BlueToothMSG;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity {
	private BluetoothAdapter mBluetoothAdapter;
	private final int REQUEST_ENABLE_BT = 1;
	private ListView mListView;
	private ArrayAdapter<String> mArrayAdapter;
	private BroadcastReceiver mReceiver;
	private ArrayList<BluetoothDevice> mDevicesList;
	private ProgressBar mProgressBar;
	private Handler mConnectHandler;
	private Handler mServerHandler;
	private Handler mMainHandler;
	private boolean mScanning;


	private MenuItem mFind;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.findbluetooth);
		mListView = (ListView)findViewById(R.id.listview);
		mProgressBar = (ProgressBar)findViewById(R.id.progressbar);

		mMainHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case BlueToothMSG.HIDE_VIEW:
					//mProgressBar.setVisibility(View.INVISIBLE);
					mMainHandler.removeMessages(BlueToothMSG.HIDE_VIEW);
					//mFind.setVisible(true);
					break;

				default:
					break;
				}
			}
		};

		final BluetoothManager bluetoothManager =
		        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		mArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			//new AcceptThread(mBluetoothAdapter).start();
		} else {
			startSearchDevices();
		}
		
		mDevicesList = new ArrayList<BluetoothDevice>();

		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		// Create a BroadcastReceiver for ACTION_FOUND
		mReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
//				String action = intent.getAction();
//				// When discovery finds a device
//				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//					// Get the BluetoothDevice object from the Intent
//					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//					// Add the name and address to an array adapter to show in a ListView
//					mArrayAdapter.add(device.getName() + " - " + device.getAddress());
//					mDevicesList.add(device);
//					mArrayAdapter.notifyDataSetChanged();
//				} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
//					mProgressBar.setVisibility(View.VISIBLE);
//				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//					mProgressBar.setVisibility(View.GONE);
//				}
			}
		};
//		registerReceiver(mReceiver, filter);

		mListView.setOnItemClickListener(new ListItemClickListener());
		mListView.setAdapter(mArrayAdapter);
	}


	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.find:
			if (mBluetoothAdapter != null) {
				mArrayAdapter.clear();
				startSearchDevices();
			}
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
//		if (mReceiver != null) {
//			unregisterReceiver(mReceiver);
//			mReceiver = null;
//		}
		super.onDestroy();
	}

	private class ListItemClickListener implements OnItemClickListener
	{

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int pos,
				long arg3) {
			// TODO Auto-generated method stub
			BluetoothDevice device = mDevicesList.get(pos);
			if (device != null) {
				//mMainHandler.sendEmptyMessage(BlueToothMSG.HIDE_VIEW);
//				mBluetoothAdapter.cancelDiscovery();
				Intent intent = new Intent(MainActivity.this, CamActivity.class);
				intent.putExtra("device", device);
				startActivity(intent);

			}
		}
		
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (resultCode != RESULT_OK) return;
		if(requestCode == REQUEST_ENABLE_BT){
			startSearchDevices();
		} 
		super.onActivityResult(requestCode, resultCode, data);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// TODO Auto-generated method stub
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.bluetooth, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	private void getBondedDevices(BluetoothAdapter adapter, ArrayAdapter<String> arrayAdapter, ListView listView) {

		Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
		arrayAdapter.clear();
		
		if (pairedDevices.size() > 0)
		{
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices)
			{
				// Add the name and address to an array adapter to show in a ListView
				arrayAdapter.add(device.getName() + " - " + device.getAddress());
			}
			listView.setAdapter(arrayAdapter);
		}
	}

	private void startSearchDevices() {
		//只能在主线程了调用
		Toast.makeText(this, "正在搜索可连接的蓝牙设备", Toast.LENGTH_SHORT).show();
//		mBluetoothAdapter.startDiscovery();
		scanLeDevice(true);
		mProgressBar.setVisibility(View.VISIBLE);//必须在startDiscovery之后进行一次调用
//		//mFind.setVisible(false);
//		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//		startActivity(intent);
//		mMainHandler.sendMessageDelayed(mMainHandler.obtainMessage(BlueToothMSG.HIDE_VIEW), 100*1000);
	}
	
    private void scanLeDevice(final boolean enable) {
        if (enable) {
        	mBluetoothAdapter.stopLeScan(mLeScanCallback);
            // Stops scanning after a pre-defined scan period.
        	mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mProgressBar.setVisibility(View.GONE);
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, 120*1000);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }
    
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                byte[] scanRecord) {
            runOnUiThread(new Runnable() {
               @Override
               public void run() {
					mArrayAdapter.add(device.getName() + " - " + device.getAddress());
					mDevicesList.add(device);
					mArrayAdapter.notifyDataSetChanged();
               }
           });
       }
    };


}
