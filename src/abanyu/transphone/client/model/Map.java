package abanyu.transphone.client.model;

import abanyu.transphone.client.R;
import abanyu.transphone.client.view.ClientMap;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

public class Map {
	ClientMap clientMapActivity;  
	
	//MAP CONTENT VARIABLES
	  private GoogleMap map;
	  private Polyline line;
	  private MarkerOptions srcMarkerOptions;
	  private MarkerOptions desMarkerOptions;
	  
	  public Map(ClientMap pClientMapActivity){
		  clientMapActivity=pClientMapActivity;
		  map = ((SupportMapFragment) clientMapActivity.getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
	  }
	  
	  public GoogleMap getMap(){
		  return map;
	  }
	  
	  public void addMarker(MarkerOptions pMarkerOptions){
		  map.addMarker(pMarkerOptions);
	  }
	  
	  public void animateCamera(CameraUpdate pCameraUpdate, int pSpeed){
		  map.animateCamera(pCameraUpdate, pSpeed, null);
	  }
	  
	  public MarkerOptions getSrcMarkerOptions(){
		return srcMarkerOptions;  
	  }
	  
	  public MarkerOptions getDesMarkerOptions(){
		return desMarkerOptions;
	  }
	  
	  public void setSrcMarkerOptions(MarkerOptions pMarkerOptions){
		srcMarkerOptions = pMarkerOptions;
	  }
	  
	  public void setDesMarkerOptions(MarkerOptions pMarkerOptions){
		desMarkerOptions = pMarkerOptions;
	  }
  
	  public Polyline getRoute(){
		  return line;
	  }
	  
	  public void setRoute(Polyline pLine){
		  line = pLine;
	  }
}
