package abanyu.transphone.client.controller;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import connections.MyConnection;

public class SocketWriter implements Runnable{

	//DATA COMMUNICATION VARIABLES
	private MyConnection conn;
	private Socket serverSocket, taxiSocket;
	private ObjectOutputStream serverIn, taxiIn;
	  
	//LOCAL VARIABLES
	private MapController mapController;
	private boolean isDisconnecting;
	private Timer timer;
	
  public SocketWriter(MapController pMapController, MyConnection pConn, boolean pDisconnect){	
  	mapController = pMapController;
  	conn = pConn;
  	isDisconnecting = pDisconnect;

  	System.out.println("Taxi Log: =");
    System.out.println("Taxi Log: SocketWriter Constructor process finished!");
    System.out.println("Taxi Log: =");		
  }  
  
  @Override
  public void run() {
  	while(conn.getServerIp().equals("")){ //wait for the system to have a server ip to send the request
      System.out.println("Taxi Log: Waiting for the system to get the server ip");
  		mapController.getProgressDialog().setMessage("Please Wait while retrieving Server IP...");
  		mapController.getProgressDialog().show();
  	}
  	
	  try{
	  	System.out.println("Taxi Log: Connecting to the server socket with server ip: "+conn.getServerIp()+" at port: "+conn.getServerPort());
	    serverSocket = new Socket(conn.getServerIp(), conn.getServerPort());
	    serverIn = new ObjectOutputStream(serverSocket.getOutputStream()); // outflow data handler object
	    
	    if(!isDisconnecting){
		    System.out.println("Taxi Log: Sending this passenger instance to the server for registration or information update");
	    	serverIn.writeObject("clientDisconnect:"+mapController.getPassenger());
		    serverIn.flush(); //REQUIRED to successfuly write the object to the socket
		    System.out.println("Taxi Log: Passenger "+mapController.getPassenger().getRequestedTaxi()+" was successfully sent to server!");
	    }else{
		    //unregister object in the taxi
		    System.out.println("Taxi Log: Sending a cancelation request to the assigned taxi");
	      taxiSocket = new Socket(mapController.getTaxi().getIP(), conn.getTaxiPort());
	      taxiIn = new ObjectOutputStream(taxiSocket.getOutputStream());
	  	  taxiIn.writeObject("requestCancelled");
	  	  taxiIn.flush();
	  	  
		    //unregister this object in the  server
		    System.out.println("Taxi Log: Sending a disconnection request to the server");
	    	serverIn.writeObject(mapController.getPassenger().getIp());	//sends the server its ip to determine which server is to unregister    	
		    serverIn.flush(); //REQUIRED to successfuly write the object to the socket
		    System.out.println("Taxi Log: Successfully Sent a disconnection request to the server");

		    //unregister the taxi object in the system
	      mapController.getPassenger().setRequestedTaxi("");

	      System.out.println("Taxi Log: Now closing socket writer connection objects...");
	  	  try {
	  	  	taxiIn.close();
	  	  	serverIn.close();
	  	  	serverSocket.close();
	  	  } catch (Exception e) {
	  	  	System.out.println("Taxi Log: Exception at closing the connection variables in socket writer: "+e.getMessage());
	  	  }
	    }
	  } catch (Exception e) {
	  	System.out.println("Tax Log: Exception at socket writer run: "+e.getMessage());
	  } 
  }
}