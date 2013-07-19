package abanyu.transphone.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser {
	
	static InputStream input = null;
	static JSONObject jObj = null;
	static String json = "";
	
	public JSONParser() {
		
	}
	
	public String getJSONfromURL(String url) {
		
		//Making HTTP Request
		
		try{
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(url);
			
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			input = entity.getContent();
		} catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, "iso-8859-1"), 8);
			StringBuilder string = new StringBuilder();
			String line = null;
			while((line = reader.readLine()) != null)
				string.append(line + "\n");
				
			json = string.toString();
			input.close();
		} catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
		
		return json;
	}
}
