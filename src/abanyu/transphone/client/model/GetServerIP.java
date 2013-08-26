package abanyu.transphone.client.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import abanyu.transphone.client.view.ClientMap;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import connections.MyConnection;

public class GetServerIP extends AsyncTask<Void, Void, String>{
  //parameters for onPreExecute, doInBackground, onPostExecute respectively
	
  //Constructor Fetcher Variables	
  private ClientMap clientMapActivity; //the activity context
  private String url; //the url passed
  private MyConnection conn;
    
  //asynctask variables
  private ProgressDialog progressDialog;
	
  //JSON manipulation variables
  InputStream input = null;
  JSONObject jObj = null;
  String json = "";
	
  //CONSTRUCTOR	
  public GetServerIP(ClientMap pActivityContext, String pStringUrl, MyConnection pConn){
    clientMapActivity = pActivityContext;
	url = pStringUrl;
	conn = pConn;
  }
		
  protected void onPreExecute() {
	super.onPreExecute();
	progressDialog = new ProgressDialog(clientMapActivity);
	progressDialog.setMessage("Retrieving Server IP, Please wait...");
	progressDialog.setIndeterminate(true);
	progressDialog.show();
  }
		
  @Override
  protected String doInBackground(Void... arg0) {
    String result=null;

    //retrieve the json as returned by the url
    try{
	  DefaultHttpClient client = new DefaultHttpClient();
	  HttpGet get = new HttpGet(url);
			
	  HttpResponse response = client.execute(get);
      result = EntityUtils.toString(response.getEntity());  
	}catch (UnsupportedEncodingException e) {
		System.out.println(e.getMessage());
	} catch (ClientProtocolException e) {
		System.out.println(e.getMessage());
    } catch (IOException e) {
		System.out.println(e.getMessage());
    } 	
	System.out.println("server ip: " + result);
    
	return result;		
  }
		
  protected void onPostExecute(String result) {
	super.onPostExecute(result);
	progressDialog.hide();
	conn.setServerIp(result);	
  }	
}
