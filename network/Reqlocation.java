package com.macaroon.piztor;

//--------------------------------------//
//			Ask Location				//
//--------------------------------------//

public class ReqLocation extends Req{
	int gid;	//group id;
	
	ReqLocation(String token,String name,int groupid,long time,long alive){
		super(2,token,name,time,alive);	//for type 2
		gid = groupid;
	}
}