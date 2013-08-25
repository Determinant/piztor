package com.macaroon.piztor;


//--------------------------------------//
//	         Respond to login			//
//--------------------------------------//

public class ResLogin extends Res{
	int t;	//user token
	
	ResLogin(int token,int status){
		super(0,status);	//for type 0
		t = token;
	}
}