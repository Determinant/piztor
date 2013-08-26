package com.macaroon.piztor;

//--------------------------------------//
//			Update Location				//
//--------------------------------------//

public class ReqUpdate extends Req{
	double lat;	//latitude
	double lot; //longitude
	
	ReqUpdate(String token,String name,double latitude,double longitude,long time,long alive){
		super(1,token,name,time,alive);	//for type 1
		lat = latitude;
		lot = longitude;
	}
}