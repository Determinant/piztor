package com.macaroon.piztor;

//--------------------------------------//
//			authentication				//
//--------------------------------------//

public class Reqlogin extends Req{
	String user;	//username
	String pass;	//password
	
	Reqlogin(String u,String p,long time,long alive){
		super(0,0,time,alive);	//for type 0
		user = u;
		pass = p;
	}
}