package abanyu.transphone.client.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import com.google.android.gms.maps.model.LatLng;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.view.View;
import android.widget.Toast;
import actors.*;
import connections.MyConnection;
import data.TaxiStatus;

public class SocketReader extends AsyncTask<Void, Void, Void> {	
  //DATA COMMUNICATION VARIABLES
  private MyConnection conn;
  private Socket passengerSocket;
  private ObjectInputStream passengerInputStream;
  private ServerSocket passengerServerSocket;
  private Object receivedObj;
  //LOCAL VARIABLES
  private MapController mapController;
	private LatLng taxiCoordinates;
	private LatLng destinationCoordinates;
	protected LatLng passengerCoordinates;
	private Handler handler;

  public SocketReader(MapController pMapController, MyConnection pConn){	
    mapController = pMapController;
    conn = pConn;
    
    System.out.println("Taxi Log: =");		
    System.out.println("Taxi Log: SocketReader Constructor process finished!");
    System.out.println("Taxi Log: =");
  }
    
  protected void onPreExecute(){
  	super.onPreExecute();
  	
		try {
    	System.out.println("Taxi Log: Creating a Passenger Socket Reader");
			passengerServerSocket = new ServerSocket(conn.getPassengerPort());
			System.out.println("Taxi Log: Passenger Socket Reader created! Setting the passenger server status to be running");
			  	  
    }catch (Exception e) {
    	System.out.println("Taxi Log: Exception on Socket Reader Pre execute: "+e.getMessage());
    }

  }
  
  @Override
  protected Void doInBackground(Void... params) {
  	while(!mapController.hasTaxiRequest()){
  	}
//	  handler = new Handler(Looper.getMainLooper());
//	  handler.post(new Runnable() {				
//	  	@Override
//	  	public void run() {
//	  		mapController.getProgressDialog().setMessage("Waiting for the requested taxi to accept");
//   			mapController.getProgressDialog().show();
//	  	}
//	  });
	  
  	while(mapController.hasTaxiRequest()){ //accept taxi updates while taxi is open for taxi updates
      try {      	
				passengerSocket = passengerServerSocket.accept();
	      passengerInputStream = new ObjectInputStream(passengerSocket.getInputStream()); // inflow of data handler object
	      receivedObj = passengerInputStream.readObject(); 
	      
	      if(receivedObj != null){ //check is there is something to receive
	        if(receivedObj instanceof MyTaxi){ //check if the object is an instance of a taxi object
	  	      System.out.println("Taxi Log: A new connection from Taxi has been received!");

    	  	  handler = new Handler(Looper.getMainLooper());
    	  	  handler.post(new Runnable() {				
    	  	  	@Override
    	  	  	public void run() {
  	      	  	System.out.println("Taxi Log: Prompting the taxi driver");
	  	        	//manage the visibility of the map buttons
	  	       		System.out.println("Taxi Log: Setting button visibilities");
	  	        	mapController.getView().getContactButton().setVisibility(View.GONE);
	  	        	mapController.getView().getExitButton().setVisibility(View.GONE);
	  	        	mapController.getView().getInfoButton().setVisibility(View.VISIBLE);
	  	        	mapController.getView().getDisconnectButton().setVisibility(View.VISIBLE);
	  	       		System.out.println("Taxi Log: Button visibilities set. OK!");	  	       		
    	  	  	}
    	  	  });

	        	if(mapController.getTaxi() == null)
	       			System.out.println("Taxi Log: Assigning a NEW Taxi Information..");
	        	else
	       			System.out.println("Taxi Log: Updating Taxi Information..");
	        	
	        	MyTaxi taxiObjReceived = (MyTaxi)receivedObj;	       		
       			mapController.setTaxi(taxiObjReceived);
       			
       			System.out.println("Taxi Log: OK!");

       			if(mapController.getTaxi().getStatus() == TaxiStatus.requested){
         			System.out.println("Taxi Log: A new taxi is on its way to fetch you there. Showing route to the taxi.");
              passengerCoordinates = new LatLng(mapController.getPassenger().getCurLat(), mapController.getPassenger().getCurLng());
              taxiCoordinates = new LatLng(mapController.getTaxi().getCurLat(), mapController.getTaxi().getCurLng());

      	  	  handler = new Handler(Looper.getMainLooper());
      	  	  handler.post(new Runnable() {				
      	  	  	@Override
      	  	  	public void run() {
      	  	  		mapController.getProgressDialog().hide();
		         			mapController.updateMarker(passengerCoordinates, taxiCoordinates);
      	  	  	}
      	  	  });
            }else if(mapController.getTaxi().getStatus() == TaxiStatus.occupied){
         			System.out.println("Taxi Log: You have just entered in the taxi. Showing route to your destination");
       				//change display of routes as soon as the passenger has entered the taxi before
              taxiCoordinates = new LatLng(mapController.getTaxi().getCurLat(), mapController.getTaxi().getCurLng());
              destinationCoordinates = new LatLng(mapController.getPassenger().getDesLat(), mapController.getPassenger().getDesLng());

      	  	  handler = new Handler(Looper.getMainLooper());
      	  	  handler.post(new Runnable() {				
      	  	  	@Override
      	  	  	public void run() {
      	  	  		mapController.getProgressDialog().hide();
				          mapController.updateMarker(taxiCoordinates, destinationCoordinates);	  
		            }
		        	});
       			}else if(mapController.getTaxi().getStatus() == TaxiStatus.unavailable){
	            Handler handler = new Handler(Looper.getMainLooper());
	    	  	  handler.post(new Runnable() {				
	    	  	  	@Override
	    	  	  	public void run() {
	    	  	  		mapController.getProgressDialog().hide();
	    	  	  		mapController.makeToast("WARNING! SHOULD NOT BE DONE HERE @@@@@@@@@@@@@");
	    	  	  	}
	    	  	  });
       				
	    	  	  System.out.println("WARNING! SHOULD NOT BE DONE HERE @@@@@@@@@@@@@");
     					mapController.incrementTaxiIndex();
       				if(mapController.getRetrievedTaxiCount()>0 && mapController.getTaxiIndex()<mapController.getRetrievedTaxiCount()){

//       					mapController.initiateRequest();       					
       				}else{      	  	  
       					mapController.setHasTaxiRequest(false); //to stop listening
       					handler = new Handler(Looper.getMainLooper());
	      	  	  handler.post(new Runnable() {				
	      	  	  	@Override
	      	  	  	public void run() {
	      	  	  		mapController.getProgressDialog().hide();
	      	  	  		mapController.makeToast("No Taxi has accepted your request");
	      	  	  	}
	      	  	  });       					
       				}
       			}
	        }else if(receivedObj instanceof String){ //request a resend of data to the assigned taxi in case the data was not sent successfully
	        	if(receivedObj.equals("resendFromServer")){
	        		System.out.println("Taxi Log: Request Resend From Server");
	        	}else if(receivedObj.equals("resendFromTaxi")){
	        		System.out.println("Taxi Log: Request Resend From Taxi");
	        	}else if(receivedObj.equals("exitFromServer")){
	        		System.out.println("Taxi Log: Server permits to exit. Exiting Application..");
	            handler = new Handler(Looper.getMainLooper());
	    	  	  handler.post(new Runnable() {				
	    	  	  	@Override
	    	  	  	public void run() {
	    	  	  		mapController.getView().finish();
	  							Process.killProcess(Process.myPid());
	    	  	  	}
	    	  	  });
	        	}else if(receivedObj.equals("tReject")){
	        		System.out.println("Taxi Log: Checking if "+mapController.getTaxiIndex()+" <"+mapController.getRetrievedTaxiCount());
   						mapController.incrementTaxiIndex();
     					System.out.println("Taxi Log: Incrementing....");
     					if(mapController.getTaxiIndex()<mapController.getRetrievedTaxiCount()){
  	            handler = new Handler(Looper.getMainLooper());
	      	  	  handler.post(new Runnable() {				
	      	  	  	@Override
	      	  	  	public void run() {
	      	  	  		mapController.getProgressDialog().hide();
	      	  	  		mapController.makeToast("Request rejected. Moving on...");
	      	  	  		System.out.println("Taxi Log: Has Still Another taxi to retrieve data");
	      	  	  		System.out.println("Taxi Log: Initiating request....");
	      	  	  		mapController.initiateRequest();
	      	  	  	}
	      	  	  });       					
       				}else{      	  	  
	      	  	  mapController.setHasTaxiRequest(false); //to stop listening
       					handler = new Handler(Looper.getMainLooper());
	      	  	  handler.post(new Runnable() {				
	      	  	  	@Override
	      	  	  	public void run() {
	      	  	  		mapController.getProgressDialog().hide();
	      	  	  		mapController.makeToast("Sorry no more taxi left to accomodate your request. Please Try Again.");
	      	  	  		mapController.cancelRequest();
	      	  	  	}
	      	  	  });       					
       				}
	        	}else if(receivedObj.equals("tFinished")){
      	  	  mapController.setHasTaxiRequest(false); //to stop listening
     					handler = new Handler(Looper.getMainLooper());
      	  	  handler.post(new Runnable() {				
      	  	  	@Override
      	  	  	public void run() {
      	  	  		mapController.getProgressDialog().hide();
      	  	  		mapController.makeToast("Reached at the Destination.");
      	  	  		mapController.cancelRequest();
      	  	  	}
      	  	  });       						        		
	        	}else if(receivedObj.equals("tCancel")){
      	  	  mapController.setHasTaxiRequest(false); //to stop listening
     					handler = new Handler(Looper.getMainLooper());
      	  	  handler.post(new Runnable() {				
      	  	  	@Override
      	  	  	public void run() {
      	  	  		mapController.getProgressDialog().hide();
      	  	  		mapController.makeToast("Taxi Has Cancelled Your Request. Finding new Taxi List");
      	  	  		mapController.cancelRequest();
      	  	  	}
      	  	  });       
      	  	  
      	  	  new Thread(new NearestTaxiGetter(conn, mapController, mapController.getMaxDistance().getText().toString())).start();      	  	  
//     					mapController.incrementTaxiIndex();
//     					System.out.println("The taxi has cancelled the request. Moving on to the next taxi data");
//       				if(mapController.getRetrievedTaxiCount()>0 && mapController.getTaxiIndex()<mapController.getRetrievedTaxiCount()){
//       					mapController.initiateRequest();       					
//       					handler = new Handler(Looper.getMainLooper());
//	      	  	  handler.post(new Runnable() {				
//	      	  	  	@Override
//	      	  	  	public void run() {
//	      	  	  		mapController.getProgressDialog().hide();
//	      	  	  		mapController.makeToast("Request cancelled by the Taxi Driver.");
//	      	  	  		mapController.cancelRequest();
//	      	  	  	}
//	      	  	  });       					
//       				}else{      	  	  
//	      	  	  handler.post(new Runnable() {				
//	      	  	  	@Override
//	      	  	  	public void run() {
//	      	  	  		mapController.getProgressDialog().hide();
//	      	  	  		mapController.makeToast("Request Cancelled. Moving on...");
//	      	  	  	}
//	      	  	  });       					
//
//	      	  	  mapController.setHasTaxiRequest(false); //to stop listening
//       					handler = new Handler(Looper.getMainLooper());
//	      	  	  handler.post(new Runnable() {				
//	      	  	  	@Override
//	      	  	  	public void run() {
//	      	  	  		mapController.getProgressDialog().hide();
//	      	  	  		mapController.makeToast("Sorry no more taxi left to accomodate your request. Please Try Again.");
//	      	  	  		mapController.cancelRequest();
//	      	  	  	}
//	      	  	  });       					
//       				}
	        	}else{
	        		System.out.println("Taxi Log: unhandled received object retrieval in socket reader..");
	            Handler handler = new Handler(Looper.getMainLooper());
	    	  	  handler.post(new Runnable() {				
	    	  	  	@Override
	    	  	  	public void run() {
	  	            Toast.makeText(mapController.getView(), "WARNING! SHOULD NOT BE DONE HERE @@@@@@@@@@@@@ NO ELSE HERE", Toast.LENGTH_LONG).show();
			            mapController.cancelRequest();//calls to send a new request without changing the passenger identification
	    	  	  	}
	    	  	  });
	        	}
	        }
	      }else{
	      	System.out.println("Taxi Log: The received object is null. Please Report this to the developer");
	      }	      
			} catch (Exception e) {
//				System.out.println("Taxi Log: error occured in Socket reader while trying to retrieve an object: "+e.getMessage());
			}      
  	}
  	
  	System.out.println("Taxi Log: Closing the socket reader...");
    try {
    	if(passengerInputStream!=null)
    		passengerInputStream.close();
      if(passengerSocket!=null)
      	passengerSocket.close();
      if(passengerServerSocket!=null)
      	passengerServerSocket.close();
    }catch (IOException e) {
    	System.out.println("Taxi Log: Exception while closing connection in the socket reader: "+e.getMessage());
    	Toast.makeText(mapController.getView(), "Exception while closing connection in the socket reader", Toast.LENGTH_LONG).show();
    }
		return null;    	
  }
  
}
