package abanyu.transphone.client.controller;

import java.io.IOException;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import connections.MyConnection;
import abanyu.transphone.client.model.GetServerIP;
import abanyu.transphone.client.model.Map;
import abanyu.transphone.client.model.Position;
import abanyu.transphone.client.model.SocketReader;
import abanyu.transphone.client.model.SocketWriter;
import abanyu.transphone.client.view.ClientMap;
import abanyu.transphone.client.view.RequestedTaxiData;
import abanyu.transphone.client.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Process;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import actors.*;
import data.TaxiStatus;

public class MapController implements LocationListener {
	
	public MyPassenger myPassenger;
	public MyTaxi myTaxi;
	
	private Map map;
	private Position position;
	private MyConnection conn;
	private ClientMap clientMap;

	public MapController(ClientMap pClientMap, Position pPosition, Map pMap, MyConnection pConn){
		clientMap = pClientMap;
		position = pPosition;
		map = pMap;
		conn = pConn;

		myPassenger = new MyPassenger();
		new GetServerIP(clientMap, "http://transphone.freetzi.com/thesis/dbmanager.php?fname=getServerIP", conn, this).execute();
		
		if(map!=null)
		{
			markCurrentPosition();
			addMapClickListener();
		}
	}

	public void markCurrentPosition(){
		position.getLocationManager().requestLocationUpdates(position.getProvider(),
															 position.getLocationUpdateByTime(), 
															 position.getLocationUpdateByDistance(), 
															 this);
	}

	public void promptPassengerName(){
		//Get prompt_name.xml view
		LayoutInflater li = LayoutInflater.from(clientMap);
		View promptView = li.inflate(R.layout.prompt_name, null);
		
		//Set prompt_name.xml to alertDialogBuilder
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(clientMap);
		alertDialogBuilder.setView(promptView);
		final EditText nameField = (EditText) promptView.findViewById(R.id.nameField);
		
		//Set dialog message
		alertDialogBuilder.setCancelable(false)
			.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					  myPassenger.setPassengerName(nameField.getText().toString());
					  Toast.makeText(clientMap, "Touch the map to mark your destination", Toast.LENGTH_LONG).show();
					}
				});
		
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
	
	public void addContactTaxiButtonClickListener(Button confirmButton){
		  confirmButton.setOnClickListener(new OnClickListener() {
			    @Override
				public void onClick(View v) {
				  if (map.getDesMarkerOptions()!=null)
				  {
				    new Thread(new SocketWriter(MapController.this, conn, "Server")).start();
				    new SocketReader(clientMap, MapController.this, conn).execute();
				  }
				  
				  else
					  Toast.makeText(clientMap, "Please mark your destination first", Toast.LENGTH_LONG).show();
				}
			  });
	}
	
	public void addExitButtonClickListener(Button exitButton){
		  exitButton.setOnClickListener(new OnClickListener() {

			    @Override
			    public void onClick(View v) {
			    	clientMap.finish();
			      Process.killProcess(Process.myPid());
			    }
			  });
	}
	
	public void addTaxiInfoButtonClickListener(Button taxiInfoButton){
		  taxiInfoButton.setOnClickListener(new OnClickListener() {
			    @Override
				public void onClick(View v) {
			    	Intent intent = new Intent(clientMap, RequestedTaxiData.class); //notify the client to the the result of his/her request
			        intent.putExtra("Taxi Info", myTaxi);//insert the server message
			        clientMap.startActivity(intent);
				}
			  });
	}
	
	public void addDisconnectButtonClickListener(Button disconnectButton){
		  disconnectButton.setOnClickListener(new OnClickListener() {
		    @Override
			public void onClick(View v) {
		      new Thread(new SocketWriter(MapController.this, conn, "Taxi")).start();
			}
		  });
	}
	
	public void disconnect(){
	  clientMap.contactTaxiButton.setVisibility(Button.VISIBLE);
	  clientMap.exitButton.setVisibility(Button.VISIBLE);
	  clientMap.taxiInfoButton.setVisibility(Button.INVISIBLE);
	  clientMap.disconnectButton.setVisibility(Button.INVISIBLE);
	  
	  myTaxi = null;
	  map.getMap().clear();
	  map.setDesMarkerOptions(null);
	  LatLng currentCoordinates = new LatLng(myPassenger.getCurLat(), myPassenger.getCurLng());
	  map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentCoordinates, 16), 2000);
	  Toast.makeText(clientMap, "Touch the map to mark your destination", Toast.LENGTH_LONG).show();
	  
	  try{
	    if(SocketReader.passengerServerSocket != null)
	      SocketReader.passengerServerSocket.close();
	  } catch (IOException e){
		  e.printStackTrace();
	  }
	}
	
	public void addMapClickListener(){
		map.getMap().setOnMapClickListener(new OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng coordinates) {
				if(myTaxi == null)
				{
			      myPassenger.setDestinationLocation(coordinates.latitude, coordinates.longitude);
				  
			      if(map.getDesMarkerOptions()==null){
			        map.setDesMarkerOptions(new MarkerOptions().position(coordinates)
		                                                             .title("Destination Location"));
			        map.addMarker(map.getDesMarkerOptions());
			      }
				  else
			        updateMarker(coordinates, "Destination");
				}
			}
		});		
	}
	
	@Override
	public void onLocationChanged(Location location) {		
		LatLng currentCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
		myPassenger.setCurrentLocation(currentCoordinates.latitude, currentCoordinates.longitude);  

		if(map.getSrcMarkerOptions()==null)
		{
		  map.setSrcMarkerOptions(new MarkerOptions().position(currentCoordinates)
				                                     .title("Current Location"));
		  map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentCoordinates, 16), 2000);
		}
		
		else
		{
		  if(myTaxi != null)
		  {
		    LatLng targetCoordinates;

		    if(myTaxi.getStatus() != TaxiStatus.occupied)
			  targetCoordinates = new LatLng(myTaxi.getCurLat(), myTaxi.getCurLng());  
		    else
		      targetCoordinates = new LatLng(myPassenger.getDesLat(), myPassenger.getDesLng());
		    
			updateMarker(currentCoordinates, targetCoordinates, null);
		  }
		  
		  else
		    updateMarker(currentCoordinates, "Current");
		}
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
	
	public void updateMarker(LatLng currentCoordinates, LatLng targetCoordinates, ProgressDialog progressDialog) {
		map.getMap().clear();
		map.getSrcMarkerOptions().position(currentCoordinates);
		map.getDesMarkerOptions().position(targetCoordinates);
		map.addMarker(map.getSrcMarkerOptions());
		map.addMarker(map.getDesMarkerOptions());
		String url = makeJsonCompatibleUrlStr(currentCoordinates.latitude, currentCoordinates.longitude, 
											  targetCoordinates.latitude, targetCoordinates.longitude);
		new RouteDrawer(map, url, progressDialog).execute();
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentCoordinates, 16), 2000);
	}
	
	private void updateMarker(LatLng coordinates, String markerStatus) {
		if(markerStatus.equals("Current"))
		  map.getSrcMarkerOptions().position(coordinates);
		
		else if(markerStatus.equals("Destination"))
		{
	      map.getMap().clear();
		  map.getDesMarkerOptions().position(coordinates);
		  map.addMarker(map.getDesMarkerOptions());
		}
	}

	private String makeJsonCompatibleUrlStr(double srclatt, double srclong,
			double destlatt, double destlong) {
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
}
