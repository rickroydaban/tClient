package abanyu.transphone.client;

import actors.MyTaxi;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TaxiInfo extends Activity{
	
	private TextView plateno, bodyno, compname, taxidesc, drivername, compno;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.taxi_info);
		
		MyTaxi taxiData = (MyTaxi) getIntent().getSerializableExtra("server_msg");
		
		plateno = (TextView) findViewById(R.id.plateno);
		plateno.append(taxiData.getPlateNumber());
		
//		bodyno = (TextView) findViewById(R.id.bodyno);
//		bodyno.append(taxiData.getBodyNumber());
		
//		taxidesc = (TextView) findViewById(R.id.taxidesc);
//		taxidesc.append(taxiData.getDescription());
		
//		compname = (TextView) findViewById(R.id.compname);
//		compname.append(taxiData.getCompanyName());
		
//		compno = (TextView) findViewById(R.id.compno);
//		compno.append(taxiData.getCompanyNumber());
		
//		drivername = (TextView) findViewById(R.id.drivername);
//		drivername.append(taxiData.getDriverName());
	}
}
