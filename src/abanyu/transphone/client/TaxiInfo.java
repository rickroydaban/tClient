package abanyu.transphone.client;


import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TaxiInfo extends Activity{
	
	private TextView text;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.taxi_info);
		
		String serverMsg = getIntent().getStringExtra("server_msg");
		text = (TextView) findViewById(R.id.text);
		text.setText(serverMsg);
	}
}
