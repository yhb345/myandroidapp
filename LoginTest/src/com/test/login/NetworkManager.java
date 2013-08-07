package com.test.login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkManager {
	private ConnectivityManager mConnectivityManager;
	private NetworkInfo mNetworkInfo;
	private Context context;
	NetworkWatcher networkWatcher;
    
	public NetworkManager(Context context) {
		this.context = context;
		mConnectivityManager = (ConnectivityManager) context  
				    .getSystemService(Context.CONNECTIVITY_SERVICE);  
		mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();  
	}
	
	public boolean isNetworkAvailable() {
		mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if(mNetworkInfo == null)
			return false;
		return true;
	}
	
	public void registerNetworkWatcher() {
		networkWatcher = new NetworkWatcher();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		context.registerReceiver(networkWatcher, intentFilter);
	}
	
	public void unRegisterNetworkWatcher() {
		context.unregisterReceiver(networkWatcher);
	}
    
	private class NetworkWatcher extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				mNetworkInfo = mConnectivityManager.getActiveNetworkInfo(); 
				if(mNetworkInfo == null) {
					Toast.makeText(context, "网络连接已断开！", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(context, "网络恢复连接！", Toast.LENGTH_LONG).show();
				}
			}
		}
		
	}
}
