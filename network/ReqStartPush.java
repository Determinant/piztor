package com.macaroon.piztor;

//--------------------------------------//
//			Start push       			//
//--------------------------------------//

public class ReqStartPush extends Req{
	
	ReqStartPush(String token,String name){
		super(5,token,name,(long) 0,(long) 0);	//for type 5
	}
}