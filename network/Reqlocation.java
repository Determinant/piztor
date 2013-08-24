package com.macaroon.piztor;

//--------------------------------------//
//			Ask Location				//
//--------------------------------------//

public class Reqlocation extends Req{
	int gid;	//group id;
	
	Reqlocation(int token,int groupid,long time,long alive){
		super(3,token,time,alive);	//for type 3
		gid = groupid;
	}
}