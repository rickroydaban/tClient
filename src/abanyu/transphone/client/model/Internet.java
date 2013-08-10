package abanyu.transphone.client.model;

import abanyu.transphone.client.view.ConnectionUnavailable;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Internet {
	ConnectionUnavailable connectionUnavailable;
	ConnectivityManager connManager;
	NetworkInfo mWifi;
	
	public Internet(ConnectionUnavailable pClientMap){
		connectionUnavailable = pClientMap;
		connManager = (ConnectivityManager) connectionUnavailable.getSystemService(Context.CONNECTIVITY_SERVICE);
	    mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);		
	}
      

    public boolean hasInternetConnection(){
    	if(mWifi.isConnected())
    		return true;
    		
    	return false;
    }
}
