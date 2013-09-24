package abanyu.transphone.client.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import com.google.android.gms.maps.model.LatLng;
import abanyu.transphone.client.view.ClientMap;
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
  private ClientMap clientMap;
  private MapController mapController;
	private LatLng taxiCoordinates;
	private LatLng destinationCoordinates;
	protected LatLng passengerCoordinates;
	private Handler handler;

  public SocketReader(ClientMap pClientMap, MapController pMapController, MyConnection pConn){	
    clientMap = pClientMap;
    mapController = pMapController;
    conn = pConn;
    
    System.out.println("Taxi Log: =");		
    System.out.println("Taxi Log: SocketReader Constructor process finished!");
    System.out.println("Taxi Log: =");
 }
  
  protected void onPreExecute(){
    super.onPreExecute();
    if(mapController.getTaxi() == null){ //if the passenger has successfully retrieved the nearest taxi from the database
      try {
      	System.out.println("Taxi Log: Creating a Passenger Socket Reader");
				passengerServerSocket = new ServerSocket(conn.getPassengerPort());
				System.out.println("Taxi Log: Passenger Socket Reader created! Setting the passenger server status to be running");
				
      	if(mapController.getTaxi()==null){
					handler = new Handler(Looper.getMainLooper());
		  	  handler.post(new Runnable() {				
		  	  	@Override
		  	  	public void run() {
		  	    	mapController.getProgressDialog().setMessage("Waiting for taxi with plate number: "+mapController.getPassenger().getRequestedTaxi()+" to respond");
		  	    	mapController.getProgressDialog().show();
		  	  	}
		  	  });
      	}
      }catch (Exception e) {
      	System.out.println("Taxi Log: Exception on Socket Reader Pre execute: "+e.getMessage());
      }
    }
  }
  
  @Override
  protected Void doInBackground(Void... params) {
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
	  	        	clientMap.getContactButton().setVisibility(View.GONE);
	  	       		clientMap.getExitButton().setVisibility(View.GONE);
	  	       		clientMap.getInfoButton().setVisibility(View.VISIBLE);
	  	       		clientMap.getDisconnectButton().setVisibility(View.VISIBLE);
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
	    	  	  		mapController.makeToast("WARNING! SHOULD NOT BE DONE HERE @@@@@@@@@@@@@");
	    	  	  	}
	    	  	  });
       				
       				if(mapController.getRetrievedTaxiCount()>0 && mapController.getTaxiIndex()<mapController.getRetrievedTaxiCount()){
       					mapController.incrementTaxiIndex();

       					mapController.intitiateRequest(mapController.getTaxiList().get(mapController.getTaxiIndex()).split(";"));       					
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
	            Handler handler = new Handler(Looper.getMainLooper());
	    	  	  handler.post(new Runnable() {				
	    	  	  	@Override
	    	  	  	public void run() {
	  	        		clientMap.finish();
	  							Process.killProcess(Process.myPid());
	    	  	  	}
	    	  	  });
	        	}else if(receivedObj.equals("tReject")){
       				if(mapController.getRetrievedTaxiCount()>0 && mapController.getTaxiIndex()<mapController.getRetrievedTaxiCount()){
       					mapController.incrementTaxiIndex();

       					mapController.intitiateRequest(mapController.getTaxiList().get(mapController.getTaxiIndex()).split(";"));       					
       				}else{      	  	  
	      	  	  handler.post(new Runnable() {				
	      	  	  	@Override
	      	  	  	public void run() {
	      	  	  		mapController.getProgressDialog().hide();
	      	  	  		mapController.makeToast("Request rejected. Moving on...");
	      	  	  	}
	      	  	  });       					

	      	  	  mapController.setHasTaxiRequest(false); //to stop listening
       					handler = new Handler(Looper.getMainLooper());
	      	  	  handler.post(new Runnable() {				
	      	  	  	@Override
	      	  	  	public void run() {
	      	  	  		mapController.getProgressDialog().hide();
	      	  	  		mapController.makeToast("Sorry no more taxi left to accomodate your request. Please Try Again.");
	      	  	  	}
	      	  	  });       					
       				}
	        	}else if(receivedObj.equals("tCancel")){
       				if(mapController.getRetrievedTaxiCount()>0 && mapController.getTaxiIndex()<mapController.getRetrievedTaxiCount()){
       					mapController.incrementTaxiIndex();

       					mapController.intitiateRequest(mapController.getTaxiList().get(mapController.getTaxiIndex()).split(";"));       					
       				}else{      	  	  
	      	  	  handler.post(new Runnable() {				
	      	  	  	@Override
	      	  	  	public void run() {
	      	  	  		mapController.getProgressDialog().hide();
	      	  	  		mapController.makeToast("Request Cancelled. Moving on...");
	      	  	  	}
	      	  	  });       					

	      	  	  mapController.setHasTaxiRequest(false); //to stop listening
       					handler = new Handler(Looper.getMainLooper());
	      	  	  handler.post(new Runnable() {				
	      	  	  	@Override
	      	  	  	public void run() {
	      	  	  		mapController.getProgressDialog().hide();
	      	  	  		mapController.makeToast("Sorry no more taxi left to accomodate your request. Please Try Again.");
	      	  	  	}
	      	  	  });       					
       				}
	        	}else{
	        		System.out.println("Taxi Log: unhandled received object retrieval in socket reader..");
	            Handler handler = new Handler(Looper.getMainLooper());
	    	  	  handler.post(new Runnable() {				
	    	  	  	@Override
	    	  	  	public void run() {
	  	            Toast.makeText(clientMap, "WARNING! SHOULD NOT BE DONE HERE @@@@@@@@@@@@@ NO ELSE HERE", Toast.LENGTH_LONG).show();
			            mapController.cancelRequest();//calls to send a new request without changing the passenger identification
	    	  	  	}
	    	  	  });
	        	}
	        }
	      }else{
	      	System.out.println("Taxi Log: The received object is null. Please Report this to the developer");
	      }	      
			} catch (Exception e) {
				System.out.println("Taxi Log: error occured in Socket reader while trying to retrieve an object: "+e.getMessage());
			}      
  	}
  	
  	System.out.println("Taxi Log: Socket reader has been stopped but not yet closed");
		return null;    	
  }
  
  public void closeConnection() {
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
    	Toast.makeText(clientMap, "Exception while closing connection in the socket reader", Toast.LENGTH_LONG).show();
    }
  }
}
