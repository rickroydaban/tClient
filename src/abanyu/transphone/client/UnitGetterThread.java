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

public class UnitGetterThread extends AsyncTask<Void, Void, String> {
	
  /*******************************************************************/
  /******************** * VARIABLE DECLARATIONS * ********************/
  /*******************************************************************/
  //PARAMETER GETTERS
  private Context clientActivity;
  private LatLng srcLocation, destination;
  private ProgressDialog progressDialog;

  //DATA COMMUNICATION VARIABLES
  private final String SERVERIP = "10.10.10.110"; //server IP Address
  private final int PORT = 8888; //port number
  private Socket socket;
  private ObjectInputStream clientInputSocket;
  private ObjectOutputStream clientOutputSocket;

  
  /*******************************************************************/
  /************************* * CONSTRUCTOR * *************************/
  /*******************************************************************/
  public UnitGetterThread(Context clientActivityContext, LatLng sourceCoordinates, LatLng destinationCoordinates) {
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
  }

  @Override
  protected String doInBackground(Void... params) {
    connect2Server(); //initiate a connection to the server
    return null; //starts the request right after the connection has been made to the server
  }

  protected void onPostExecute(String result) {
    super.onPostExecute(result);
    closeClientConnection(); //close the connection to save power from running unused threads
    progressDialog.hide(); //hides the progress bar
    Intent intent = new Intent(clientActivity, TaxiInfo.class); //notify the client to the the result of his/her request
    intent.putExtra("server_msg", result);//insert the server message
    clientActivity.startActivity(intent);
  }
  
  
  /*******************************************************************/
  /************************ * LOCAL METHODS * ************************/
  /*******************************************************************/
  private void connect2Server() {
//    progressDialog.setMessage("Connecting to the server...");
		
    try {
      // creates a socket for the current connection
      TRANSPHONE_Actors.MyPassenger clientPass = new TRANSPHONE_Actors.MyPassenger(srcLocation.latitude,srcLocation.longitude,
                                                       destination.latitude,destination.latitude, "Stanley");

      System.out.println("#pass data#");
      System.out.println("name: "+clientPass.getClientName());
      System.out.println("src: "+clientPass.getSrcLat()+", "+clientPass.getSrcLng());
      System.out.println("des: "+clientPass.getDesLat()+", "+clientPass.getDesLng());
      socket = new Socket(SERVERIP, PORT);
      
      clientOutputSocket = new ObjectOutputStream(socket.getOutputStream()); // outflow data handler object
      clientInputSocket = new ObjectInputStream(socket.getInputStream()); // inflow of data handler object
      
      clientOutputSocket.writeObject(clientPass);
      clientOutputSocket.flush();

      
    }catch (UnknownHostException e) {
      Toast.makeText(clientActivity, "Error! Cannot identify the host connection", Toast.LENGTH_LONG).show();
    }catch (IOException e) {
      Toast.makeText(clientActivity, "Error! Cannot establish the connection", Toast.LENGTH_LONG).show();
    }
  }
	
  /*private String serverUnitRequest() {
    try {
      // send client id to the server to identify that the request is from a client object
      
      
	   return getServerReply(); //get the server reply
    }catch (IOException e) {
    	Toast.makeText(clientActivity, "Error occured while sending client data to the server", Toast.LENGTH_LONG).show();
    }
    
    return null;
  }*/

  public boolean closeClientConnection() {
    progressDialog.setMessage("Server reply has been recieved successfully. Closing connection...");

    if (socket != null) {
      try {
    	//1. closing data outflow connection
        if(clientOutputSocket != null) {
          try {
            clientOutputSocket.close();
          }catch (IOException e) {
            Toast.makeText(clientActivity, "error occured while closing the output socket.", Toast.LENGTH_LONG).show();
          }
        }
        //2. closing data inflow connection
        if (clientInputSocket != null) {
          try {
		    clientInputSocket.close();
          }catch (IOException e) {
            Toast.makeText(clientActivity, "error occured while closing the input socket.", Toast.LENGTH_LONG).show();
          }          
		}
        //3. close the main socket
    	socket.close();    	  
    	return true;
      }catch (IOException e) {
        Toast.makeText(clientActivity, "error occured while closing the socket.", Toast.LENGTH_LONG).show();
	  }
    }

	return false;
  }
	
  private String getServerReply(){
    progressDialog.setMessage("Request sent successfully! Waiting for server reply...");
		
    // read server message
    try {
      return clientInputSocket.readUTF();
    }catch (IOException e) {
      return null;
    }
  }
}
