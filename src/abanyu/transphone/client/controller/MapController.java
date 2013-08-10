package abanyu.transphone.client.controller;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import connections.MyConnection;

import abanyu.transphone.client.model.Map;
import abanyu.transphone.client.model.Position;
import abanyu.transphone.client.model.RouteDrawer;
import abanyu.transphone.client.model.SocketWriter;
import abanyu.transphone.client.view.ClientMap;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MapController implements LocationListener {
	ClientMap clientMap;
	Position position;
	Map map;
	MyConnection conn;

	  private String Source = "Source";
	  private String Destination = "Destination";
	
	
	public MapController(ClientMap pClientMap, Position pPosition, Map pMap, MyConnection pConn){
		clientMap = pClientMap;
		conn = pConn;
		position = pPosition;
		map = pMap;
		
		if(map!=null){
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

	@Override
	public void onLocationChanged(Location location) {
		position.setCurrentLocation(location.getLatitude(), location.getLongitude());  
		  
		if(map.getSrcMarkerOptions()==null){
		  map.setSrcMarkerOptions(new MarkerOptions().position(position.getCurrentLocation())
				                                     .title("Source"));


		  map.addMarker(map.getSrcMarkerOptions());
		  map.animateCamera(CameraUpdateFactory.newLatLngZoom(position.getCurrentLocation(), 16), 2000);
			
			
		}else
		  updateMarker(map.getSrcMarkerOptions(), map.getDesMarkerOptions(), Source, position.getCurrentLocation());
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
	
	private void updateMarker(MarkerOptions srcMO, MarkerOptions desMO, String updatedMarker, LatLng updatedLocation) {
		if (map.getRoute() != null)
			map.getMap().clear();
		
		if(updatedMarker.equals(Source))
		  srcMO.position(updatedLocation);
		else if(updatedMarker.equals(Destination))
		  desMO.position(updatedLocation);
		
        map.addMarker(srcMO);
        map.addMarker(desMO);
	}
	
	public void addConfirmButtonClickListener(Button confirmButton){
		  confirmButton.setOnClickListener(new OnClickListener() {
			    @Override
				public void onClick(View v) {
				  if (position.getDestinationLocation()!=null)
					//requests a unit to the server
				    new SocketWriter(clientMap, position.getCurrentLocation(), position.getDestinationLocation(),conn).execute();
				}
			  });

	}
	
	public void addExitButtonClickListener(Button exitButton){
		  exitButton.setOnClickListener(new OnClickListener() {

			    @Override
			    public void onClick(View v) {
			      clientMap.finish();
			    }
			  });

	}
	
	public void addMapClickListener(){
		map.getMap().setOnMapClickListener(new OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng location) {
				//when the map is clicked, the destination will be set to the touched area
			      
				if(map.getSrcMarkerOptions()!=null){
			        position.setDestinationLocation(location.latitude, location.longitude);
				  
			        if(map.getDesMarkerOptions()==null){
			    		map.setDesMarkerOptions(new MarkerOptions().position(location)
		                                                           .title("Destination"));

			    		map.addMarker(map.getDesMarkerOptions());
			        }
				    else
					  updateMarker(map.getSrcMarkerOptions(), map.getDesMarkerOptions(), Destination, location);
				   
				    String url = makeJsonCompatibleUrlStr(position.getCurrentLocation().latitude, 
				    									  position.getCurrentLocation().longitude, 
				    									  location.latitude, 
				    									  location.longitude);
				    new RouteDrawer(clientMap, map, url).execute();
			      }
			    }
			  });		
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
