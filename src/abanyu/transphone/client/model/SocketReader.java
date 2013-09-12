package abanyu.transphone.client.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import com.google.android.gms.maps.model.LatLng;

import abanyu.transphone.client.controller.MapController;
import abanyu.transphone.client.view.ClientMap;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.Toast;
import actors.*;
import connections.MyConnection;
import data.TaxiStatus;

public class SocketReader extends AsyncTask<Void, Void, Object> {
	
  /*******************************************************************/
  /******************** * VARIABLE DECLARATIONS * ********************/
  /*******************************************************************/
  //DATA COMMUNICATION VARIABLES
  private MyConnection conn;
  private Socket passengerSocket;
  private ObjectInputStream passengerInputStream;
  public static ServerSocket passengerServerSocket;
  
  //LOCAL VARIABLES
  private ClientMap clientMap;
  private MapController mapController;
  private ProgressDialog progressDialog;

  
  /*******************************************************************/
  /************************* * CONSTRUCTOR * *************************/
  /*******************************************************************/
  public SocketReader(ClientMap pClientMap, MapController pMapController, MyConnection pConn){	
    clientMap = pClientMap;
    mapController = pMapController;
    conn = pConn;
 }

  
  /*******************************************************************/
  /************************** * ASYNCTASK * **************************/
  /*******************************************************************/
  protected void onPreExecute(){
    super.onPreExecute();
    progressDialog = new ProgressDialog(clientMap);
    progressDialog.setIndeterminate(true);
    
	if(mapController.myTaxi == null)
	{
	  progressDialog.setMessage("Contacting nearest taxi, please wait...");
      progressDialog.show();
	}
  }
  
  @Override
  protected Object doInBackground(Void... params) {
    Object result = null;
	try {
      passengerServerSocket = new ServerSocket(conn.getPassengerPort());
      passengerSocket = passengerServerSocket.accept();
	  passengerInputStream = new ObjectInputStream(passengerSocket.getInputStream()); // inflow of data handler object
	  result = passengerInputStream.readObject(); 
    }catch (UnknownHostException e) {
    	System.out.println("error: unknown host exception");
    }catch (IOException e) {
    	System.out.println("error: could not connect to server");
    } catch (ClassNotFoundException e) {
    	System.out.println("error: class not found exception");
	}
    return result;  
  }
  
  protected void onPostExecute(Object result) {
    super.onPostExecute(result); 
    
    if(mapController.myTaxi == null)
    {
      if(result != null)
      {
    	if(result instanceof MyTaxi)
    	{
    	  MyTaxi taxiResult = (MyTaxi)result;
    	  progressDialog.setMessage("Contacted a taxi, updating markers...");
    	  clientMap.contactTaxiButton.setVisibility(Button.INVISIBLE);
        clientMap.exitButton.setVisibility(Button.INVISIBLE);
        clientMap.taxiInfoButton.setVisibility(Button.VISIBLE);
        clientMap.disconnectButton.setVisibility(Button.VISIBLE);
    	
          mapController.myTaxi = taxiResult;
          updateTaxiLocation();
    	}
    	
    	else if(result instanceof String)
    		new Thread(new SocketWriter(mapController, conn, "Server")).start();
      }
    
      else if(!passengerServerSocket.isClosed())
      {
        progressDialog.hide();
        Toast.makeText(clientMap, "No available taxi found", Toast.LENGTH_LONG).show();
      }
    }
    
    else
    {
      if(result != null)
      {
        if(result instanceof MyTaxi)
        {
    	  MyTaxi taxiResult = (MyTaxi)result;
    	  
          if(taxiResult.getStatus() == TaxiStatus.requested)
          {
    	    if(mapController.myTaxi.getPlateNumber().equals(taxiResult.getPlateNumber()))
    		  progressDialog.setMessage("Updating the location of the requested taxi..."); 
    	    else
    		  progressDialog.setMessage("Requested taxi is unavailable, updating the location of the new requested taxi...");
          }
        
    	  mapController.myTaxi = taxiResult;
        
          if(mapController.myTaxi.getStatus() == TaxiStatus.occupied)
          {
            progressDialog.setMessage("Updating markers to destination...");
    	    progressDialog.show();
    	    LatLng taxiCoordinates = new LatLng(mapController.myTaxi.getCurLat(), mapController.myTaxi.getCurLng());
            LatLng destinationCoordinates = new LatLng(mapController.myPassenger.getDesLat(), mapController.myPassenger.getDesLng());
            mapController.updateMarker(taxiCoordinates, destinationCoordinates, progressDialog);	  
          }
       
          else if(mapController.myTaxi.getStatus() == TaxiStatus.requested)
          {
      	    progressDialog.show();
            updateTaxiLocation();  
          }
        
          else if(mapController.myTaxi.getStatus() == TaxiStatus.vacant)
          {
            Toast.makeText(clientMap, "Disconnected from taxi", Toast.LENGTH_LONG).show();
            new Thread(new SocketWriter(mapController, conn, "Server")).start();
            mapController.disconnect();
          }
        }
        
        else if(result instanceof String)
        	new Thread(new SocketWriter(mapController, conn, "Server")).start();
      }
      
      else
      {
        Toast.makeText(clientMap, "Requested taxi is unavailable", Toast.LENGTH_LONG).show();
        Toast.makeText(clientMap, "Could not find any other available taxi", Toast.LENGTH_LONG).show();
        new Thread(new SocketWriter(mapController, conn, "Server")).start();
        mapController.disconnect();
      }
    }
    closeConnection();
    
    if(mapController.myTaxi != null)
      new SocketReader(clientMap, mapController, conn).execute();
  }
  
  
  /*******************************************************************/
  /************************ * LOCAL METHODS * ************************/
  /*******************************************************************/
  
  private void updateTaxiLocation(){
    LatLng passengerCoordinates = new LatLng(mapController.myPassenger.getCurLat(), mapController.myPassenger.getCurLng());
    LatLng taxiCoordinates = new LatLng(mapController.myTaxi.getCurLat(), mapController.myTaxi.getCurLng());
    mapController.updateMarker(passengerCoordinates, taxiCoordinates, progressDialog);
  }
  
  private void closeConnection() {
    try {
      if(passengerInputStream != null)
        passengerInputStream.close();
      if(passengerSocket != null)
        passengerSocket.close();
      if(passengerServerSocket != null)
        passengerServerSocket.close();
    } catch (IOException e) {
        e.printStackTrace();
	}
  }
}
