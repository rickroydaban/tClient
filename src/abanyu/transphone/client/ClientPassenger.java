package abanyu.transphone.client;

import java.io.Serializable;

public class ClientPassenger implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1;
	double srcLat, srcLng, desLat, desLng;
	String ip, name;
	
	
	public ClientPassenger(double pSrcLat, double pSrcLng, double pDesLat, double pDesLng, String pName){
	  srcLat=pSrcLat;
	  srcLng=pSrcLng;
	  desLat=pDesLat;
	  desLng=pDesLng;
	  name=pName;
	}
	
	public double getSrcLat(){
		return srcLat;
	}

	public double getSrcLng(){
		return srcLng;
	}

	public double getDesLat(){
		return desLat;
	}

	public double getDesLng(){
		return desLng;
	}

	public String getClientName(){
		return name;
	}
	
    public void setIp(String pIp){
    	ip=pIp;
    }
    
    public String getIp(){
    	return ip;
    }
}
