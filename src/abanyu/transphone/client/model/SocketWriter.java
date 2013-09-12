package abanyu.transphone.client.model;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import abanyu.transphone.client.controller.MapController;
import android.os.Handler;
import android.os.Looper;
import connections.MyConnection;

public class SocketWriter implements Runnable{

	//DATA COMMUNICATION VARIABLES
	private MyConnection conn;
	private Socket passengerSocket;
	private ObjectOutputStream passengerOutputStream;
	  
	//LOCAL VARIABLES
	private String recipient;
	private MapController mapController;
	
  public SocketWriter(MapController pMapController, MyConnection pConn, String pRecipient){	
	mapController = pMapController;
	conn = pConn;
	recipient = pRecipient;
  }
  
  @Override
  public void run() {
	  try{
	    passengerSocket = new Socket(conn.getServerIp(), conn.getServerPort());
		mapController.myPassenger.setIp(getIPAddress());
	    passengerOutputStream = new ObjectOutputStream(passengerSocket.getOutputStream()); // outflow data handler object
		passengerOutputStream.writeObject(mapController.myPassenger);
		passengerOutputStream.flush(); //REQUIRED to successfuly write the object to the socket
		  
		if(recipient.equals("Taxi"))
	    {
	      passengerSocket = new Socket(mapController.myTaxi.getIP(), conn.getTaxiPort());
	      mapController.myPassenger.setIp("");
	      passengerOutputStream = new ObjectOutputStream(passengerSocket.getOutputStream());
	  	  passengerOutputStream.writeObject(mapController.myPassenger);
	  	  passengerOutputStream.flush();
	  	  
	  	Handler handler = new Handler(Looper.getMainLooper());
		  handler.post(new Runnable() {				
		    @Override
			public void run() {
		    	mapController.disconnect();
			}
		  });
	    }
	  } catch (UnknownHostException e) {
	      System.out.println("error: unknown host exception");
	  } catch (IOException e) {
	      System.out.println("error: could not connect to server");
	  } finally{
	      closeConnection();
	    }
      }
  
  private String getIPAddress() {
      try 
      {
          List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
          
          for (NetworkInterface intf : interfaces) 
          {
              List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
          
              for (InetAddress addr : addrs) 
              {
                  if (!addr.isLoopbackAddress()) 
                  {
                      String sAddr = addr.getHostAddress().toUpperCase();
                      boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                      
                      if (isIPv4) 
                      	return sAddr;
                  }
              }
          }
      } catch (Exception e) { 
      	e.printStackTrace();
      } 
      return null;
  }
  
  private void closeConnection() {
    try {
      if(passengerOutputStream != null)
	    passengerOutputStream.close();
      if(passengerSocket != null)
		passengerSocket.close();
    } catch (IOException e) {
	    e.printStackTrace();
	}
  }
}