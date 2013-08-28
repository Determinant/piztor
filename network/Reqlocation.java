package com.macaroon.piztor;

//--------------------------------------//
//			Ask Location				//
//--------------------------------------//

public class ReqLocation extends Req{
	int company;	//group id;
	int section;
	
	ReqLocation(String token,String name,int com,int sec,long time,long alive){
		super(2,token,name,time,alive);	//for type 2
		company = com;
		section = sec;
	}
}