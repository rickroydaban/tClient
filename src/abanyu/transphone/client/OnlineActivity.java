package abanyu.transphone.client;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;


/*
 * this class will check if there is a wireless connection within this devices
 */
public class OnlineActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE); //removes title bar	
      
    ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
      
    if(mWifi.isConnected()){
      Intent i = new Intent(this,ClientActivity.class);
      startActivity(i);
    }else{
      setContentView(R.layout.enablewifi);

      Button reloadButton = (Button)findViewById(R.id.reloadButton);
      reloadButton.setOnClickListener(new OnClickListener() {
    	@Override
    	public void onClick(View v) {
          finish();
          startActivity(getIntent());
        }
      });
    }  
  }
}
