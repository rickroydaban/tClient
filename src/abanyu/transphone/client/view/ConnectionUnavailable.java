package abanyu.transphone.client.view;

import abanyu.transphone.client.R;
import abanyu.transphone.client.model.Internet;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


/*
 * this class will check if there is a wireless connection within this devices
 */
public class ConnectionUnavailable extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
      
    Internet internet = new Internet(this);
    
    if(internet.hasInternetConnection()){
      Intent i = new Intent(this,ClientMap.class);
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
