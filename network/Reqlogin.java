package com.macaroon.piztor;

//--------------------------------------//
//			authentication				//
//--------------------------------------//

public class ReqLogin extends Req{
	String user;	//username
	String pass;	//password
	
	ReqLogin(String u,String p,long time,long alive){
		super(0,"","",time,alive);	//for type 0
		user = u;
		pass = p;
	}
}