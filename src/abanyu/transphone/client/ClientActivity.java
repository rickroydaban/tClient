/*ClientActivity.java the main activity of the application that will be installed on the clients of this package.
 * This will manage the processes that will be done by the client part.
 * 
 * The client will just start the application, then this application will automatically retrieve the client's location using GPS.
 * Once the location has been retrieved, a Google Map will be shown with a marker indicating the client's current location.
 * The client will then edit the map and put a marker on it to input his/her desired destination
 * After the source and the destination has been inputted, this activity will then create a connection to the server and request a 
 * unit to it using the data of this activity.
 * The client will just have to wait to the server's reply.
 * The client will be notified if the request has been granted or declined or an error occured during the request process.
 */


/*
* MUST HANDLE LOCATION PROVIDERS. what if the user forgot to turn on his/her wifi and GPS?
 * 
 */

package abanyu.transphone.client;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

public class ClientActivity extends FragmentActivity implements LocationListener {
  // needs to extend fragment activity since fragments are needed in order to display map contents

  
  /*******************************************************************/
  /******************** * VARIABLE DECLARATIONS * ********************/
  /*******************************************************************/

  //MAP CONTENT VARIABLES
  public static GoogleMap map;
  public static Polyline line;
  private MarkerOptions srcMarkerOptions;
  private MarkerOptions desMarkerOptions;
  
  //LOCATION DATA VARIABLES
  private LocationManager locationManager;
  private LatLng srcCoords, desCoords;
  private String locationProvider;
  private int locationUpdateByTime = 600000; //location will be updated every 10 minutes
  private int locationUpdateByDist = 10; //location will be updated every 10 meters
  
  //LOCAL CONSTANTS
//  private String GPS = "GPS";
//  private String NETWORK = "Network";
  private String Source = "Source";
  private String Destination = "Destination";
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE); //removes title bar
    setContentView(R.layout.client_map);

//    enableLocationProvider(GPS);
    
    //icon that will be shown to indicate the currently used location provider
    ImageView provImg = (ImageView)findViewById(R.id.provImg); //gps or network

    //get a Google Map
    map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
    if (map != null){
      //create a new criteria for getting a location provider of preference when a map is available
      Criteria criteria = new Criteria();
      criteria.setAccuracy(Criteria.ACCURACY_FINE);
      criteria.setPowerRequirement(Criteria.POWER_LOW);
      
	  locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);       
	  locationProvider = locationManager.getBestProvider(criteria, true);
	  //NOTE: on obstructed places, GPS may took longer times to have a position fix compared to wireless network
          
	  //manage displaying of icons
      if(locationProvider.equals("gps")){	
	    provImg.setImageResource(R.drawable.gps);
//	    enableLocationProvider(GPS);
      }else if(locationProvider.equals("network")){
	    provImg.setImageResource(R.drawable.network);
//	    enableLocationProvider(NETWORK);
      }
       
      
	  //apply location change listeners to the location manager
      locationManager.requestLocationUpdates(locationProvider, locationUpdateByTime, locationUpdateByDist, this); 

      map.setOnMapClickListener(new OnMapClickListener() {
	    @Override
        public void onMapClick(LatLng location) {
	      //when the map is clicked, the destination will be set to the touched area
	      
	      if(srcMarkerOptions!=null){
	        desCoords = location;
		  
	        if(desMarkerOptions==null)
	          markDestination(location);
		    else
			  updateMarker(srcMarkerOptions, desMarkerOptions, Destination, desCoords);
		   
		    String url = makeJsonCompatibleUrlStr(srcCoords.latitude, srcCoords.longitude, desCoords.latitude, desCoords.longitude);
		    new DrawPathAsyncTask(ClientActivity.this, url).execute();
	      }
	    }
	  });
      
	
	  //manage confirm button click
	  Button confirmButton = (Button) findViewById(R.id.confirmButton);
	  confirmButton.setOnClickListener(new OnClickListener() {

	    @Override
		public void onClick(View v) {
		  if (desCoords!=null)
			//requests a unit to the server
		    new ServerSpeakerThread(ClientActivity.this, srcCoords, desCoords).execute();
		}
	  });

	  //manage exit button click
	  Button exitButton = (Button) findViewById(R.id.exitButton);
	  exitButton.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
	      ClientActivity.this.finish();
	    }
	  });
	}
  }
      
	@Override
	public void onLocationChanged(Location location) {      
	  srcCoords = new LatLng(location.getLatitude(), location.getLongitude());
	  
	  if(srcMarkerOptions==null)
		  markSourceLocation(srcCoords);
	  else
		  updateMarker(srcMarkerOptions, desMarkerOptions, Source, srcCoords);
	}

	@Override
	public void onProviderDisabled(String provider) {
	  Toast.makeText(this, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	private void markSourceLocation(LatLng sourceLocation){
		srcMarkerOptions = new MarkerOptions().position(sourceLocation)
					  					      .title("Source");

		
		map.addMarker(srcMarkerOptions);
		
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(srcCoords, 16),
				2000, null);
	}
	
	private void markDestination(LatLng destination){
		desMarkerOptions = new MarkerOptions().position(destination)
					                          .title("Destination");
		
		map.addMarker(desMarkerOptions);
	}
	
	private void updateMarker(MarkerOptions srcMO, MarkerOptions desMO, String updatedMarker, LatLng updatedLocation) {
		if (line != null)
			map.clear();
		
		if(updatedMarker.equals(Source))
		  srcMO.position(updatedLocation);
		else if(updatedMarker.equals(Destination))
		  desMO.position(updatedLocation);
		
        map.addMarker(srcMO);
        map.addMarker(desMO);
        
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
	/*
	private void enableLocationProvider(String locationProviderCriteria){
	  String mLocationProvider = "";
	  
	  if(locationProviderCriteria.equals(GPS))
	    mLocationProvider = LocationManager.GPS_PROVIDER;
	  else if(locationProviderCriteria.equals(NETWORK))
		mLocationProvider = LocationManager.NETWORK_PROVIDER;
		
	  //Get Location Manager and check for GPS & Network location services
	  if(!locationManager.isProviderEnabled(mLocationProvider)) {
	    //Build the alert dialog let the user decide whether to continue enabling his location providers or not
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Location Services Not Active");
	    builder.setMessage("Please enable Location Services and " + locationProviderCriteria);
	    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialogInterface, int i) {
	        //Show location settings when the user acknowledges the alert dialog
	        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	        startActivity(intent);
	      }
	    });
	        
	    Dialog alertDialog = builder.create();
	    alertDialog.setCanceledOnTouchOutside(false);
	    alertDialog.show();        
	}
  }*/
}

