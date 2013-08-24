package com.macaroon.piztor;


//--------------------------------------//
//	         Respond to login			//
//--------------------------------------//

public class Reslogin extends Res{
	int t;	//user token
	
	Reslogin(int token,int status){
		super(0,status);	//for type 0
		t = token;
	}
}