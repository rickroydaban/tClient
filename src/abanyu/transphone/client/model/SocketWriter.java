package abanyu.transphone.client.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import abanyu.transphone.client.view.ClientMap;
import abanyu.transphone.client.view.RequestedTaxiData;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;

import actors.*;
import connections.MyConnection;

public class SocketWriter extends AsyncTask<Void, Void, MyTaxi> {
	
  /*******************************************************************/
  /******************** * VARIABLE DECLARATIONS * ********************/
  /*******************************************************************/
  //PARAMETER GETTERS
  private ClientMap clientMap;
  private LatLng srcLocation, destination;
  private ProgressDialog progressDialog;

  //DATA COMMUNICATION VARIABLES
  private String SERVERIP; //server IP Address
  private int PORT; //port number
  private Socket clientApp_Client;
  private ObjectInputStream clientApp_ClientInputSocket;
  private ObjectOutputStream clientApp_ClientOutputSocket;

  private MyPassenger clientPass;
  
  /*******************************************************************/
  /************************* * CONSTRUCTOR * *************************/
  /*******************************************************************/
  public SocketWriter(ClientMap pClientMapView, LatLng sourceCoordinates, LatLng destinationCoordinates, MyConnection pConn){	
    clientMap = pClientMapView;
    srcLocation = sourceCoordinates;
    destination = destinationCoordinates;
    SERVERIP = pConn.getServerIp();
    PORT = pConn.getServerPort();
 }

  
  /*******************************************************************/
  /************************** * ASYNCTASK * **************************/
  /*******************************************************************/
  protected void onPreExecute() {
    super.onPreExecute();	
    // makes a dialog animation upon starting the connection to show the
    // progress of the task
    progressDialog = new ProgressDialog(clientMap);
    progressDialog.setMessage("Sending Request. Waiting for Server Reply");
    progressDialog.setIndeterminate(true); //progress bar is not loading by percentage
    progressDialog.show(); //required to show the progress bar

    //creates a passenger object based on the properties exist on this cless
    clientPass = new MyPassenger( srcLocation.latitude,srcLocation.longitude, 
      				                     destination.latitude,destination.latitude, 
      				                     "Stanley" );
  }

  @Override
  protected MyTaxi doInBackground(Void... params) {
	  MyTaxi result = null;
	//creates a connection to the server to send the request
	
    try {
	  clientApp_Client = new Socket(SERVERIP, PORT);
	        
	  clientApp_ClientOutputSocket = new ObjectOutputStream(clientApp_Client.getOutputStream()); // outflow data handler object
	  clientApp_ClientInputSocket = new ObjectInputStream(clientApp_Client.getInputStream()); // inflow of data handler object
	  //writes the object to the socket      
	  clientApp_ClientOutputSocket.writeObject(clientPass);
	  clientApp_ClientOutputSocket.flush(); //REQUIRED to successfuly write the object to the socket
	  
	  result = (MyTaxi)clientApp_ClientInputSocket.readObject();
    }catch (UnknownHostException e) {
    	System.out.println("error: unknown host exception");
    }catch (IOException e) {
    	System.out.println("error: could not connect to server");
    } catch (ClassNotFoundException e) {
    	System.out.println("error: class not found exception");
	}
    return result;  
  }
  protected void onPostExecute(MyTaxi result) {
    super.onPostExecute(result); 
    
    closeClientConnection(); //close the connection to save power from running unused threads
    progressDialog.hide(); //hides the progress bar
    Intent intent = new Intent(clientMap, RequestedTaxiData.class); //notify the client to the the result of his/her request
    intent.putExtra("server_msg", result);//insert the server message
    clientMap.startActivity(intent);
  }
  
  
  /*******************************************************************/
  /************************ * LOCAL METHODS * ************************/
  /*******************************************************************/

  public boolean closeClientConnection() {
    progressDialog.setMessage("Server reply has been recieved successfully. Closing connection...");

    if (clientApp_Client != null) {
      try {
    	//1. closing data outflow connection
        if(clientApp_ClientOutputSocket != null) {
          try {
            clientApp_ClientOutputSocket.close();
          }catch (IOException e) {
            Toast.makeText(clientMap, "error occured while closing the output socket.", Toast.LENGTH_LONG).show();
          }
        }
        //2. closing data inflow connection
        if (clientApp_ClientInputSocket != null) {
          try {
		    clientApp_ClientInputSocket.close();
          }catch (IOException e) {
            Toast.makeText(clientMap, "error occured while closing the input socket.", Toast.LENGTH_LONG).show();
          }          
		}
        //3. close the main socket
    	clientApp_Client.close();    	  
    	return true;
      }catch (IOException e) {
        Toast.makeText(clientMap, "error occured while closing the socket.", Toast.LENGTH_LONG).show();
	  }
    }

	return false;
  }
}
