package abanyu.transphone.client.model;

import abanyu.transphone.client.view.ClientMap;

import android.content.Context;
import android.location.Criteria;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;

public class Position {
  //map
  private ClientMap clientMap;

  //LOCATION DATA VARIABLES
  private LocationManager locationManager;
  private LatLng srcCoords, desCoords;
  private String locationProvider;
  private int locationUpdateByTime = 600000; //location will be updated every 10 minutes
  private int locationUpdateByDist = 10; //location will be updated every 10 meters
 
  public Position(ClientMap pClientMap){
	clientMap = pClientMap;
	locationManager = (LocationManager) clientMap.getSystemService(Context.LOCATION_SERVICE); //context here???/ 
	setProviderByCriteria(locationManager);
		
	//NOTE: on obstructed places, GPS may took longer times to have a position fix compared to wireless network
  }
  
  public void setProviderByCriteria(LocationManager pLocationManager){
    Criteria criteria = new Criteria();
	    
    criteria.setAccuracy(Criteria.ACCURACY_FINE);
    criteria.setPowerRequirement(Criteria.POWER_LOW);
	    
	locationProvider=pLocationManager.getBestProvider(criteria, true);
  }

  public void setLocationUpdateByTime(int pTimeInMilliseconds){
	  locationUpdateByTime=pTimeInMilliseconds;
  }
  
  public void setLocationUpdateByDistance(int pDistanceInMeters){
	  locationUpdateByDist=pDistanceInMeters;
  }
  
  public int getLocationUpdateByTime(){
	  return locationUpdateByTime;
  }
  
  public int getLocationUpdateByDistance(){
	  return locationUpdateByDist;
  }
  
  public LocationManager getLocationManager(){
	  return locationManager;
  }
  
  public String getProvider(){
	  return locationProvider;
  }
  
  public boolean providerIsGPS(){
	if(locationProvider.equals("gps"))
	  return true;
	
	return false;
  }
  
  public boolean providerIsNetwork(){
	 if(locationProvider.equals("network"))
	   return true;
	 
    return false;
  }
  
  /*public void setCurrentLocation(double pLat, double pLng){
	  srcCoords = new LatLng(pLat, pLng);
  }
  
  public void setDestinationLocation(double pLat, double pLng){
	  desCoords = new LatLng(pLat, pLng);
  }
  
  public LatLng getCurrentLocation(){
	  return new LatLng(srcCoords.latitude, srcCoords.longitude);
  }
  
  public LatLng getDestinationLocation(){
	  return new LatLng(desCoords.latitude, desCoords.longitude);
  }*/
}
