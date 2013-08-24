package com.macaroon.piztor;

//--------------------------------------//
//			Update Location				//
//--------------------------------------//

public class ReqUpdate extends Req{
	double lat;	//latitude
	double lot; //longitude
	
	ReqUpdate(int token,double latitude,double longitude,long time,long alive){
		super(2,token,time,alive);	//for type 2
		lat = latitude;
		lot = longitude;
	}
}