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

public class ErrorView extends Activity{
	ErrorView context;
	Bundle extras;
	
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.errorview);
    
    context = this; //get the context of this class here since it can't be referenced inside onclicklistener
    extras = getIntent().getExtras(); //get the passed extras sent by the previous intent
    
    TextView errorField = (TextView)findViewById(R.id.errorCause);
    ImageButton reloadButton = (ImageButton)findViewById(R.id.reloadButton);

    errorField.setText((String)extras.get("errorMsg")); //sets the defined error cause setted by the previous intent
    reloadButton.setOnClickListener(new OnClickListener() {
    	@Override
    	public void onClick(View v) {        
    		finish();

    		if(extras.get("intentType").equals("refresh"))
    			startActivity(new Intent(context,ClientMap.class));
      }
    });
  }
}
