package com.macaroon.piztor;


//--------------------------------------//
//	         Respond to login			//
//--------------------------------------//

public class ResLogin extends Res{
	String t;	//user token
	int uid;	//userid
	
	ResLogin(int id,String token,int status){
		super(0,status);	//for type 0
		t = token;
		uid = id;
	}
}