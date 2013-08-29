package com.macaroon.piztor;

//--------------------------------------//
//			Update Location				//
//--------------------------------------//

public class ReqUpdate extends Req{
	double latitude;	//latitude
	double longitude; //longitude
	
	ReqUpdate(String token,String name,double lat,double lot,long time,long alive){
		super(1,token,name,time,alive);	//for type 1
		latitude = lat;
		longitude = lot;
	}
}