package abanyu.transphone.client.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Looper;
import connections.MyConnection;

public class NearestTaxiGetter implements Runnable{
  //parameters for onPreExecute, doInBackground, onPostExecute respectively
	
  //Constructor Fetcher Variables	
	private MyConnection conn; //used to set the server ip in this context
  private String url; //used to dynamically create the url
  private MapController mapController;     
  //JSON manipulation variables
  InputStream input;
  JSONObject jObj;
  String json;
  String result;
  Handler handler;
	private JSONArray taxis;
	private JSONObject jo;
	private String plateNo;
	private String servrIP;
	private String eta;
	private String[] taxiList;	
  //CONSTRUCTOR	
  public NearestTaxiGetter(MyConnection pConn, MapController pMapController){    
    //dynamically calculates the nearest taxi to the passenger through php scripting
    conn = pConn;
    mapController = pMapController;

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
	  mapController.resetTaxiIndex();
	  
	  if(!result.equals("0")){
			try {
				taxis = new JSONArray(result);

				handler = new Handler(Looper.getMainLooper());
	  	  handler.post(new Runnable() {				
	  	  	@Override
	  	  	public void run() {
	  	    	mapController.makeToast("Retrieved "+taxis.length()+" Available Taxi.");
	  	  	}
	  	  });
	  	
	  	  System.out.println("TAXI LOG: Setting retrieved taxi count: "+taxis.length());
	  	  mapController.setRetrievedTaxiCount(taxis.length());
	  	  
	  	  //update the taxi list in the map controller
	  	  taxiList = new String[taxis.length()];	  	  
				for (int ctr=0; ctr < taxiList.length; ctr++) {
					jo = taxis.getJSONObject(ctr);
					plateNo = jo.getString("plateNo");
					servrIP = jo.getString("servrIP");
					eta = jo.getString("eta");
					
					taxiList[ctr] = plateNo+";"+servrIP+";"+eta;
				}

				handler = new Handler(Looper.getMainLooper());
	  	  handler.post(new Runnable() {				
	  	  	@Override
	  	  	public void run() {
	  				mapController.setTaxiList(taxiList);				
	  	  	}
	  	  });
			} catch (JSONException e) {
				System.out.println("Taxi Info: JSON Exception at NearestTaxiGetter: "+e.getMessage());
			}	  	
    }else{
    	System.out.println("Taxi Log: DB returns 0 result. No Taxi is available as of the moment");
      handler = new Handler(Looper.getMainLooper());
  	  handler.post(new Runnable() {				
  	  	@Override
  	  	public void run() {
  	    	mapController.makeToast("No Available Taxi Found.");
  	    	mapController.cancelRequest();
  	  	}
  	  });
    }		


	}
}
