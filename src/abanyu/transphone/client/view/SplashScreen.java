package abanyu.transphone.client.view;

import abanyu.transphone.client.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

/*
 * This class shows the template view for a defined cause of error
 */

public class SplashScreen extends Activity{
	SplashScreen context;
	Bundle extras;
	
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.splash);    
  }
}
