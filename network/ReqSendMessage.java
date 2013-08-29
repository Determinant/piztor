package com.macaroon.piztor;

//--------------------------------------//
//			Send Message				//
//--------------------------------------//

public class ReqSendMessage extends Req{
	String msg;
	
	ReqSendMessage(String token,String name,String message,long time,long alive){
		super(6,token,name,time,alive);	//for type 6
		msg = message;
	}
}