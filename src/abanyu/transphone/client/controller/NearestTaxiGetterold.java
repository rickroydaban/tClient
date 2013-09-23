package abanyu.transphone.client.controller;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import android.os.Handler;
import android.os.Looper;
import connections.MyConnection;

public class NearestTaxiGetterold implements Runnable{
  //parameters for onPreExecute, doInBackground, onPostExecute respectively
	
  //Constructor Fetcher Variables	
	private MyConnection conn; //used to set the server ip in this context
  private String url; //used to dynamically create the url
  private MapController mapController;     
	private boolean registerTaxi;
  //JSON manipulation variables
  InputStream input;
  JSONObject jObj;
  String json;
  String result;
  Handler handler;
	
  //CONSTRUCTOR	
  public NearestTaxiGetterold(MyConnection pConn, MapController pMapController, boolean pRegisterTaxi){    
    //dynamically calculates the nearest taxi to the passenger through php scripting
    conn = pConn;
    mapController = pMapController;
    registerTaxi = pRegisterTaxi;

    url = new StringBuilder(conn.getDBUrl()+"/thesis/dbmanager.php?fname=getNearestTaxiServerIP").
    											append("&arg1=").
    											append(pMapController.getPassenger().getCurLat()).
    											append("&arg2=").
    											append(pMapController.getPassenger().getCurLng()).
    											toString();
    System.out.println("Taxi Log: =");
    System.out.println("Taxi Log: NearestTaxiGetter Constructor finished with url: "+url);
    System.out.println("Taxi Log: =");
  }
		
  public NearestTaxiGetterold(MyConnection pConn, MapController pMapController, boolean pRegisterTaxi, String plateNo){    
    //dynamically calculates the nearest taxi to the passenger through php scripting
    conn = pConn;
    mapController = pMapController;
    registerTaxi = pRegisterTaxi;

    url = new StringBuilder(conn.getDBUrl()+"/thesis/dbmanager.php?fname=getNearestTaxiServerIP").
    											append("&arg1=").
    											append(pMapController.getPassenger().getCurLat()).
    											append("&arg2=").
    											append(pMapController.getPassenger().getCurLng()).
    											append("&arg3=").
    											append(plateNo).
    											toString();
    
    System.out.println("Taxi Log: =");
    System.out.println("Taxi Log: NearestTaxiGetter Constructor finished with url: "+url);
    System.out.println("Taxi Log: =");
  }

	@Override
	public void run() {
    handler = new Handler(Looper.getMainLooper());
	  handler.post(new Runnable() {				
	  	@Override
	  	public void run() {
	  		mapController.getProgressDialog().setMessage("Calculating the nearest Taxi, please wait...");
	    	mapController.getProgressDialog().show();
	      //retrieve the json as returned by the url
	  	}
	  });
	  
    try{
    	DefaultHttpClient client = new DefaultHttpClient();
    	HttpGet get = new HttpGet(url);
			
    	HttpResponse response = client.execute(get);
      result = EntityUtils.toString(response.getEntity());  
      //returns the server ip to be contacted and the plate number of the calculated nearest taxi
    }catch (Exception e) {
    	System.out.println("Taxi Log: Error. Exception while parsing the data of the retrieved nearest taxi: "+e.getMessage());
    } 	
    
    System.out.println("Taxi Log: result has been retrieved: "+result);

    handler = new Handler(Looper.getMainLooper());
	  handler.post(new Runnable() {				
	  	@Override
	  	public void run() {
	    	mapController.getProgressDialog().hide();
	  	}
	  });

	  if(!result.equals("0")){
    	String [] results = result.split(";");
	  	
    	System.out.println("Taxi Log: Setting the server ip: "+results[0]+" at port: "+conn.getServerPort());
    	conn.setServerIp(results[0]);
    	System.out.println("Taxi Log: Setting the system taxi to retrieved nearest Taxi from online DB: "+results[1]);
    	mapController.getPassenger().setRequestedTaxi(results[1]);
    	
    	if(registerTaxi)
    		new Thread(new SocketWriter(mapController, conn, false)).start(); //sends a request to the server
    	else
    		new Thread(new SocketWriter(mapController, conn, true)).start(); //sends a request to the server
    	
    }else{
    	System.out.println("Taxi Log: DB returns 0 result. No Taxi is available as of the moment");
    	mapController.makeToast("No taxi is available as of the moment.");
      handler = new Handler(Looper.getMainLooper());
  	  handler.post(new Runnable() {				
  	  	@Override
  	  	public void run() {
  	    	mapController.cancelRequest();
  	  	}
  	  });
    }		


	}
}
