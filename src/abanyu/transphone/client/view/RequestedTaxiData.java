package abanyu.transphone.client.view;

import abanyu.transphone.client.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import actors.MyTaxi;

public class RequestedTaxiData extends Activity{
	
	private TextView plateno, bodyno, compname, taxidesc, drivername, compno;
	private Button back;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.taxi_info);
		
		MyTaxi taxiData = (MyTaxi) getIntent().getSerializableExtra("Taxi Info");
		
		System.out.println("Plate No: "+taxiData.getPlateNumber());
		System.out.println("Body No:"+taxiData.getBodyNumber());
		System.out.println("Desc: "+taxiData.getDescription());
		System.out.println("Company: "+taxiData.getCompanyName());
		System.out.println("Driver: "+taxiData.getDriverName());
		
		plateno = (TextView) findViewById(R.id.plateno);
		plateno.append(taxiData.getPlateNumber());
		
		bodyno = (TextView) findViewById(R.id.bodyno);
		bodyno.append(taxiData.getBodyNumber());
		
		taxidesc = (TextView) findViewById(R.id.taxidesc);
		taxidesc.append(taxiData.getDescription());
		
		compname = (TextView) findViewById(R.id.compname);
		compname.append(taxiData.getCompanyName());
		
		compno = (TextView) findViewById(R.id.compno);
		compno.append(taxiData.getCompanyNumber());
		
		drivername = (TextView) findViewById(R.id.drivername);
		drivername.append(taxiData.getDriverName());
		
		back = (Button) findViewById(R.id.backButton);
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
