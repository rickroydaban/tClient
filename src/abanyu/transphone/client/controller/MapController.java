package abanyu.transphone.client.controller;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.http.conn.util.InetAddressUtils;

import abanyu.transphone.client.R;
import abanyu.transphone.client.model.Map;
import abanyu.transphone.client.model.Position;
import abanyu.transphone.client.view.ClientMap;
import abanyu.transphone.client.view.RequestedTaxiData;
import actors.MyPassenger;
import actors.MyTaxi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import connections.MyConnection;
import data.TaxiStatus;

public class MapController implements LocationListener, OnClickListener {
	private MapController mapController; //context is needed for progress dialog in getting the nearest taxi
	private MyPassenger myPassenger;
	private MyTaxi myTaxi;
	private Map map;
	private Position position;
	private MyConnection conn;
	private ClientMap clientMap;
	private AlertDialog alertDialog;
	private boolean isPassengerServerRunning;
	private ProgressDialog progressDialog;
	private EditText nameField;
	
	public MapController(ClientMap pClientMap){
    System.out.println("Taxi Log: Initializing map controller. Map controller constructor in progress..");
		clientMap = pClientMap;
    mapController = this;
    conn = new MyConnection();
    System.out.println("Taxi Log: Connection Object is set. OK!");
    position = new Position(clientMap);
    System.out.println("Taxi Log: Position Object is set. OK!");
		map = new Map(clientMap);
    System.out.println("Taxi Log: Map is set. OK!");
		myPassenger = new MyPassenger();
    System.out.println("Taxi Log: Passenger Object is created. OK!");		
    myPassenger.setIp(getIPAddress());
    System.out.println("Taxi Log: Passenger IP is set. OK!");

    progressDialog = new ProgressDialog(clientMap);
  	progressDialog.setIndeterminate(true);

    //attaches listeners to buttons
    clientMap.getContactButton().setOnClickListener(this);
    clientMap.getInfoButton().setOnClickListener(this);
    clientMap.getDisconnectButton().setOnClickListener(this);
    clientMap.getExitButton().setOnClickListener(this);
    System.out.println("Taxi Log: Attaching action listeners to map buttons. OK!");

    //sets unusable buttons to be invisible
    clientMap.getInfoButton().setVisibility(Button.INVISIBLE);
    clientMap.getDisconnectButton().setVisibility(Button.INVISIBLE);
    System.out.println("Taxi Log: Setting initial map button visibilities. OK!");

		if(map!=null){
			//DEFAULT provider is network since it is the fastest
	    if(position.providerIsGPS()){	
		    clientMap.getProviderIcon().setImageResource(R.drawable.gps);
	    }else if(position.providerIsNetwork()){
	    	clientMap.getProviderIcon().setImageResource(R.drawable.network);
	    }      
	    System.out.println("Taxi Log: Location Provider Icon is set. OK!");
			
			focusCurrentPosition(); //map focuses on your current location
	    System.out.println("Taxi Log: Adding Map Click Listener");
			addMapClickListener();
			
	    System.out.println("Taxi Log: Start Reading for Server replies...");			
		  new SocketReader(clientMap, mapController, conn).execute();		
		}else{
			System.out.println("Taxi Log: WARNING. Map is null");
		}
		
    System.out.println("Taxi Log: =");		
    System.out.println("Taxi Log: MapController Constructor process finished!");
    System.out.println("Taxi Log: =");
	}
	
	public void focusCurrentPosition(){
    System.out.println("Taxi Log: Requesting Location Update..");				
		position.getLocationManager().requestLocationUpdates(position.getProvider(),
															 position.getLocationUpdateByTime(), 
															 position.getLocationUpdateByDistance(), 
															 this);		
	}
	
	@Override
	public void onLocationChanged(Location location) {		
		LatLng currentCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
		System.out.println("Taxi Log: Location has been found! ("+currentCoordinates.latitude+","+currentCoordinates.longitude+")");		
		myPassenger.setCurrentLocation(currentCoordinates.latitude, currentCoordinates.longitude);  

		if(map.getSrcMarkerOptions()==null){
			System.out.println("Taxi Log: Initial Location Update...");
		  map.setSrcMarkerOptions(new MarkerOptions().position(currentCoordinates)
				                                     		.title("Current Location"));
		  
		  System.out.println("Taxi Log: Souce Marker Options set! Zooming in to your location...");
		  map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentCoordinates, 16), 1000);
		  Toast.makeText(clientMap, "Touch the map to mark your destination", Toast.LENGTH_LONG).show();
		}else{
		  if(myTaxi != null){
		    LatLng targetCoordinates;

		    if(myTaxi.getStatus() == TaxiStatus.occupied){
		      targetCoordinates = new LatLng(myPassenger.getDesLat(), myPassenger.getDesLng());		    	
					System.out.println("Taxi Log: Updating Location. You just entered into the assigned taxi");
		    }else{
		    	targetCoordinates = new LatLng(myTaxi.getCurLat(), myTaxi.getCurLng());  
					System.out.println("Taxi Log: Updating Location. An assigned taxi is on its way to fetch you");
		    }
		    
		    updateMarker(currentCoordinates, targetCoordinates);
		  }else
				System.out.println("Taxi Log: Updating Location with no assigned taxi yet");	    
		    updateMarker(currentCoordinates, "YourLocation");
		}
	}
	
	private void updateMarker(LatLng coordinates, String markerStatus) {
		System.out.println("Taxi Log: Resetting Map Overlays...");
		map.getMap().clear();

		if(markerStatus.equals("YourLocation")){
			System.out.println("Taxi Log: Location Updated. Zooming to your Location with no Markers");
		  map.getSrcMarkerOptions().position(coordinates);
		}else if(markerStatus.equals("Destination")){
			System.out.println("Taxi Log: Destination Updated. Zooming to your Destination with a Marker");
		  map.getDesMarkerOptions().position(coordinates);
		  map.addMarker(map.getDesMarkerOptions());
		}else{
			System.out.println("Taxi Log: Warning. updateMarker unHandled condition");
		}
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 16), 1000);
	}

	//maker updater between routes
	public void updateMarker(LatLng currentCoordinates, LatLng targetCoordinates) {
		System.out.println("Taxi Log: Resetting Map Overlays...");
		map.getMap().clear();
		map.getSrcMarkerOptions().position(currentCoordinates);
		map.getDesMarkerOptions().position(targetCoordinates);
		System.out.println("Taxi Log: Adding markers with their updated locations ");
		System.out.println("Taxi Log: Source ("+currentCoordinates.latitude+","+currentCoordinates.longitude+")");
		System.out.println("Taxi Log: Destxn ("+targetCoordinates.latitude+","+targetCoordinates.longitude+")");
		map.addMarker(map.getSrcMarkerOptions());
		map.addMarker(map.getDesMarkerOptions());
		System.out.println("Taxi Log: Updating Route...");		
		String url = makeJsonCompatibleUrlStr(currentCoordinates.latitude, currentCoordinates.longitude, 
											  targetCoordinates.latitude, targetCoordinates.longitude);
		progressDialog.setMessage("Updating Route to the taxi Location");
		progressDialog.show();
		new RouteDrawer(map, url, mapController).execute();
		progressDialog.hide();
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentCoordinates, 16), 2000);
	}

	public void addMapClickListener(){
    System.out.println("Taxi Log: Setting Map Click Listeners");		
		map.getMap().setOnMapClickListener(new OnMapClickListener() {			
			@Override
			public void onMapClick(LatLng coordinates) {
				if(myTaxi == null){
					myPassenger.setDestinationLocation(coordinates.latitude, coordinates.longitude);
				  
					if(map.getDesMarkerOptions()==null){
						map.setDesMarkerOptions(new MarkerOptions().position(coordinates) //instantiates a new marker option for destination marker
		                                                   .title("Destination"));
						System.out.println("Taxi Log: Created destination marker options");
						map.addMarker(map.getDesMarkerOptions());
					}else
						System.out.println("Taxi Log: Updating destination marker options");						
						updateMarker(coordinates, "Destination");
				}else{
					System.out.println("Taxi Log: WARNING. You are setting destination but a taxi exist on your system...");
				}
			}
		});		
	}
	
	@Override
	public void onClick(View v) {
		if(v.equals(clientMap.getContactButton())){
			System.out.println("Taxi Log: Prompting a name input dialog...");
			
			if(map.getDesMarkerOptions()!=null){
				makeDialog(false,R.layout.prompt_name,clientMap.getContactButton());
			}else{
				Toast.makeText(clientMap, "Please mark your destination first", Toast.LENGTH_LONG).show();
			}
		}else if(v.equals(clientMap.getExitButton())){
			System.out.println("Taxi Log: Prompting an exit dialog...");
			
			makeDialog(false, R.layout.confirmexit,clientMap.getExitButton());
			
		}else if(v.equals(clientMap.getInfoButton())){
			System.out.println("Taxi Log: Starting new Intent to show Taxi Information...");
			
			Intent intent = new Intent(clientMap, RequestedTaxiData.class); //notify the client to the the result of his/her request
			intent.putExtra("Taxi Info", myTaxi);//insert the server message
			clientMap.startActivity(intent);
			
		}else if(v.equals(clientMap.getDisconnectButton())){
			System.out.println("Taxi Log: Prompting an exit dialog...");

			makeDialog(false, R.layout.confirmdisconnect,clientMap.getDisconnectButton());
		}
	}	

	public void makeNewRequest(boolean registerTaxi){
		String str;
		if(registerTaxi)
			str = " for updating purposes ";
		else
			str = " for disconnection purposes ";
		
    if(myTaxi!=null){
			System.out.println("Taxi Log: Making new Request that escapes taxi: "+myTaxi.getPlateNumber()+" on the search."+str);
			new Thread(new NearestTaxiGetter(conn, mapController,  myTaxi.getPlateNumber())).start(); //get the nearest taxi from the database
    }else{
			System.out.println("Taxi Log: Making new Request..."+str);
			new Thread(new NearestTaxiGetter(conn, mapController)).start(); //get the nearest taxi from the database
    }
	}
	
	public void makeDialog(final boolean requiresInput, int layoutID, final Button triggerButton){
		//Get prompt_name.xml view
		LayoutInflater li = LayoutInflater.from(clientMap);
		View promptView = li.inflate(layoutID, null);
		
		//Set prompt_name.xml to alertDialogBuilder
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(clientMap);
		alertDialogBuilder.setView(promptView);
		
		if(requiresInput) //a text input is required
			nameField = (EditText) promptView.findViewById(R.id.nameField);
		
		//Set dialog message
		alertDialogBuilder.setCancelable(false).
				setPositiveButton("OK", new DialogInterface.OnClickListener() {		
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(requiresInput)
							myPassenger.setPassengerName(nameField.getText().toString());
					  
							if(triggerButton == clientMap.getContactButton())								
								makeNewRequest(false); //sends a new taxi request to the server
							else if(triggerButton == clientMap.getExitButton()){
								new Thread(new SocketWriter(mapController, conn, true)).start(); //boolean true signifies that it is requesting to disconnect					
							}else if(triggerButton == clientMap.getDisconnectButton()){
								new Thread(new SocketWriter(mapController, conn, true)).start();
							}else{
								System.out.println("Taxi Log: Unhandled if in makedialog");
							}
							
						alertDialog.dismiss();
					}
				});
		
				alertDialogBuilder.setCancelable(false).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {		
					@Override
					public void onClick(DialogInterface dialog, int which) {
					  alertDialog.dismiss();
					}
				});
		
		alertDialog = alertDialogBuilder.create();
		alertDialog.show();
		
	}

	//this is needed for other classes to manipulate this applications progress dialog
	public ProgressDialog getProgressDialog(){
		System.out.println("Taxi Log: Returning an instance of a progress dialog...");
		return progressDialog;
	}
	
	//this will be executed when you decided to cancel your taxi request
	public void cancelRequest(){ //cancels the request to a specified taxi
		System.out.println("Taxi Log: Cancelling Request...");		
		clientMap.getContactButton().setVisibility(Button.VISIBLE);
	  clientMap.getExitButton().setVisibility(Button.VISIBLE);
	  clientMap.getInfoButton().setVisibility(Button.INVISIBLE);
	  clientMap.getDisconnectButton().setVisibility(Button.INVISIBLE);
	  myTaxi = null; //restart the nearest taxi data;

	  updateMarker(new LatLng(myPassenger.getCurLat(), myPassenger.getCurLng()), "Destination");
	  Toast.makeText(clientMap, "Touch the map to mark your destination", Toast.LENGTH_LONG).show();
	}
	
	public void setProgressDialog(ProgressDialog dialog){
		System.out.println("Taxi Log: Setting progress dialog");
		progressDialog = dialog;
	}
	
	public void makeToast(String message){
		System.out.println("Taxi Log: Setting Toast");
	  Toast.makeText(clientMap, message, Toast.LENGTH_LONG).show();		
	}		

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}	

	private String makeJsonCompatibleUrlStr(double srclatt, double srclong, double destlatt, double destlong) {
		System.out.println("Taxi Log: make JSON url to display Route");
		StringBuilder url = new StringBuilder();
		url.append("http://maps.googleapis.com/maps/api/directions/json");
		url.append("?origin=");
		url.append(Double.toString(srclatt));
		url.append(",");
		url.append(Double.toString(srclong));
		url.append("&destination=");
		url.append(Double.toString(destlatt));
		url.append(",");
		url.append(Double.toString(destlong));
		url.append("&sensor=false&mode=driving&alternatives=true");
		return url.toString();
  }
	
  private String getIPAddress() {
		System.out.println("Taxi Log: Retrieving IP...");
  	try {
  		List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
          
  		for(NetworkInterface intf : interfaces) {
  			List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
          
  			for (InetAddress addr : addrs) {
  				if(!addr.isLoopbackAddress()) {
  					String sAddr = addr.getHostAddress().toUpperCase(Locale.ENGLISH);
  					boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                      
  					if (isIPv4) 
  						return sAddr;
  					else
  						System.out.println("Taxi Log: Warning. Not an IPV4");
  				}else{
						System.out.println("Taxi Log: Warning. Address is a LoopBackAddress");  					
  				}
  			}
  		}
  	} catch (Exception e) { 
  		System.out.println("Taxi Log: ERROR. while retrieving IP: "+e.getMessage());
  	} 
  	
  	return null;
  }
  
	public MyPassenger getPassenger(){
		System.out.println("Taxi Log: Returning an instance of passenger");
		return myPassenger;
	}
	
	public MyTaxi getTaxi(){
		System.out.println("Taxi Log: Returning an instance of taxi");
		return myTaxi;
	}
	
	public void setTaxi(MyTaxi pTaxi){
		System.out.println("Taxi Log: Setting an instance of passenger");
		myTaxi = pTaxi;
	}	
	
	public void setMap(Map pMap){
		System.out.println("Taxi Log: Setting an instance of map");
		map = pMap;
	}
	
	public Map getMap(){
		System.out.println("Taxi Log: Returning an instance of map");
		return map;
	}
	
	public Position getPosition(){
		System.out.println("Taxi Log: Returning an instance of position");
		return position;
	}
	
	public boolean isPassengerServerAlive(){
		System.out.println("Taxi Log: Returning passenger server status: "+isPassengerServerRunning);
		return isPassengerServerRunning;
	}
	
	public void setPassengerServerAlive(){
		System.out.println("Taxi Log: Setting passenger server status to alive");
		isPassengerServerRunning = true;
	}
	
	public void killPassengerServer(){
		System.out.println("Taxi Log: Setting passenger server status to dead");
		isPassengerServerRunning = false;
	}
}
