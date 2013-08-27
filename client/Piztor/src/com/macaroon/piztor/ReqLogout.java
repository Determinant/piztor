package com.macaroon.piztor;

//--------------------------------------//
//			     Log out      		    //
//--------------------------------------//

public class ReqLogout extends Req{
	
	ReqLogout(String token,String name,long time,long alive){
		super(4,token,name,time,alive);	//for type 4
	}
}