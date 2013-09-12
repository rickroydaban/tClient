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

package abanyu.transphone.client.view;

import abanyu.transphone.client.R;
import abanyu.transphone.client.controller.MapController;
import abanyu.transphone.client.model.Map;
import abanyu.transphone.client.model.Position;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;
import android.widget.ImageView;

import connections.MyConnection;

public class ClientMap extends FragmentActivity{
  // needs to extend fragment activity since fragments are needed in order to display map contents
  
  private Map map;
  private Position position;
  private MyConnection conn;
  private MapController mapController;
  private ImageView providerIconContainer;
  
  public Button contactTaxiButton, exitButton, taxiInfoButton, disconnectButton;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.client_map);
    
    conn = new MyConnection();
    position = new Position(this);
    map = new Map(this);
    mapController = new MapController(this, position, map, conn);

    //icon that will be shown to indicate the currently used location provider
    providerIconContainer = (ImageView)findViewById(R.id.provImg); //gps or network
    //manage displaying of icons
    if(position.providerIsGPS()){	
	    providerIconContainer.setImageResource(R.drawable.gps);
    }else if(position.providerIsNetwork()){
	    providerIconContainer.setImageResource(R.drawable.network);
    }      
	
	//manage confirm button click
	contactTaxiButton = (Button) findViewById(R.id.contactTaxiButton);
	mapController.addContactTaxiButtonClickListener(contactTaxiButton);
	//manage exit button click
	exitButton = (Button) findViewById(R.id.exitButton);
	mapController.addExitButtonClickListener(exitButton);
	//manage taxi information button click
	taxiInfoButton = (Button) findViewById(R.id.taxiInfoButton);
	mapController.addTaxiInfoButtonClickListener(taxiInfoButton);
	taxiInfoButton.setVisibility(Button.INVISIBLE);
	//manage disconnect button click
	disconnectButton = (Button) findViewById(R.id.disconnectButton);
	mapController.addDisconnectButtonClickListener(disconnectButton);
	disconnectButton.setVisibility(Button.INVISIBLE);
  }
}
