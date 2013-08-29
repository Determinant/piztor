package com.macaroon.piztor;


//--------------------------------------//
//	         Push Message    			//
//--------------------------------------//

public class ResPushMessage extends Res{
	String message;
	
	ResPushMessage(String s){
		super(100,0);	//for type 100
		message = s;
	}
}