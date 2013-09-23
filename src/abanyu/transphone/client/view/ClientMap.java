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
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;
import android.widget.ImageView;

public class ClientMap extends FragmentActivity{
  // needs to extend fragment activity since fragments are needed in order to display map contents
  
  private ImageView providerIconContainer;
  private Button contactTaxiButton, exitButton, taxiInfoButton, disconnectButton;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.client_map);

    System.out.println("Taxi Log: Starting the system..");
    providerIconContainer = (ImageView) findViewById(R.id.provImg); //gps or network
    //manage confirm button click
    contactTaxiButton = (Button)findViewById(R.id.contactTaxiButton);
    //manage exit button click
    exitButton = (Button)findViewById(R.id.exitButton);
    //manage taxi information button click
    taxiInfoButton = (Button)findViewById(R.id.taxiInfoButton);
    //manage disconnect button click
    disconnectButton = (Button)findViewById(R.id.disconnectButton);
      
    //icon that will be shown to indicate the currently used location provider
    providerIconContainer = (ImageView)findViewById(R.id.provImg); //gps or network
    //manage displaying of icons
	  
    System.out.println("Taxi Log: Map View has been initialized! Starting Map Controller...");
    System.out.println("Taxi Log: =");
    System.out.println("Taxi Log: =");
        
    new MapController(this);
  }
  
  public ImageView getProviderIcon(){
    System.out.println("Taxi Log: returning an instance of the provider icon imageview");
  	return providerIconContainer;
  }
  
  public Button getContactButton(){
    System.out.println("Taxi Log: returning an instance of the contact button");
  	return contactTaxiButton;
  }
  
  public Button getExitButton(){
    System.out.println("Taxi Log: returning an instance of the exit button");
  	return exitButton;
  }
  
  public Button getInfoButton(){
    System.out.println("Taxi Log: returning an instance of the taxi info button");
  	return taxiInfoButton;
  }
  
  public Button getDisconnectButton(){
    System.out.println("Taxi Log: returning an instance of the disconnect button");
  	return disconnectButton;
  }
}
