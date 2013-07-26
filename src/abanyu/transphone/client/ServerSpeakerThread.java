package abanyu.transphone.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;

public class ServerSpeakerThread extends AsyncTask<Void, Void, actors.MyTaxi> {
	
  /*******************************************************************/
  /******************** * VARIABLE DECLARATIONS * ********************/
  /*******************************************************************/
  //PARAMETER GETTERS
  private Context clientActivity;
  private LatLng srcLocation, destination;
  private ProgressDialog progressDialog;

  //DATA COMMUNICATION VARIABLES
  private String SERVERIP = connections.MyConnection.serverIp; //server IP Address
  private int PORT = connections.MyConnection.serverPort; //port number
  private Socket clientApp_Client;
  private ObjectInputStream clientApp_ClientInputSocket;
  private ObjectOutputStream clientApp_ClientOutputSocket;

  private actors.MyPassenger clientPass;
  
  /*******************************************************************/
  /************************* * CONSTRUCTOR * *************************/
  /*******************************************************************/
  public ServerSpeakerThread(Context clientActivityContext, LatLng sourceCoordinates, LatLng destinationCoordinates) {
    clientActivity = clientActivityContext;
    srcLocation = sourceCoordinates;
    destination = destinationCoordinates;
 }

  
  /*******************************************************************/
  /************************** * ASYNCTASK * **************************/
  /*******************************************************************/
  protected void onPreExecute() {
    super.onPreExecute();	
    // makes a dialog animation upon starting the connection to show the
    // progress of the task
    progressDialog = new ProgressDialog(clientActivity);
    progressDialog.setMessage("Sending Request. Waiting for Server Reply");
    progressDialog.setIndeterminate(true); //progress bar is not loading by percentage
    progressDialog.show(); //required to show the progress bar

    //creates a passenger object based on the properties exist on this cless
    clientPass = new actors.MyPassenger( srcLocation.latitude,srcLocation.longitude, 
      				                     destination.latitude,destination.latitude, 
      				                     "Stanley" );
  }

  @Override
  protected actors.MyTaxi doInBackground(Void... params) {
	  actors.MyTaxi result = null;
	//creates a connection to the server to send the request
	
    try {
	  clientApp_Client = new Socket(SERVERIP, PORT);
	        
	  clientApp_ClientOutputSocket = new ObjectOutputStream(clientApp_Client.getOutputStream()); // outflow data handler object
	  clientApp_ClientInputSocket = new ObjectInputStream(clientApp_Client.getInputStream()); // inflow of data handler object
	  //writes the object to the socket      
	  clientApp_ClientOutputSocket.writeObject(clientPass);
	  clientApp_ClientOutputSocket.flush(); //REQUIRED to successfuly write the object to the socket
	  
	  result = (actors.MyTaxi)clientApp_ClientInputSocket.readObject();
    }catch (UnknownHostException e) {
	  Toast.makeText(clientActivity, "Error! Cannot identify the host connection", Toast.LENGTH_LONG).show();
    }catch (IOException e) {
      Toast.makeText(clientActivity, "Error! Cannot establish the connection", Toast.LENGTH_LONG).show();
    } catch (ClassNotFoundException e) {
        Toast.makeText(clientActivity, "Error! Class not found", Toast.LENGTH_LONG).show();
	}
    return result;  
  }
  protected void onPostExecute(actors.MyTaxi result) {
    super.onPostExecute(result); 
    
    closeClientConnection(); //close the connection to save power from running unused threads
    progressDialog.hide(); //hides the progress bar
    Intent intent = new Intent(clientActivity, TaxiInfo.class); //notify the client to the the result of his/her request
    intent.putExtra("server_msg", result.plateNo);//insert the server message
    clientActivity.startActivity(intent);
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
            Toast.makeText(clientActivity, "error occured while closing the output socket.", Toast.LENGTH_LONG).show();
          }
        }
        //2. closing data inflow connection
        if (clientApp_ClientInputSocket != null) {
          try {
		    clientApp_ClientInputSocket.close();
          }catch (IOException e) {
            Toast.makeText(clientActivity, "error occured while closing the input socket.", Toast.LENGTH_LONG).show();
          }          
		}
        //3. close the main socket
    	clientApp_Client.close();    	  
    	return true;
      }catch (IOException e) {
        Toast.makeText(clientActivity, "error occured while closing the socket.", Toast.LENGTH_LONG).show();
	  }
    }

	return false;
  }
}
