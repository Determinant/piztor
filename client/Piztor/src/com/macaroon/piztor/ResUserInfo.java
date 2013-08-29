package com.macaroon.piztor;


//--------------------------------------//
//			Respond to User Info	    //
//--------------------------------------//

public class ResUserInfo extends Res{
	int uid;		//userid
	int company;		//group id
	int section;
	int sex;		//type 0 for female,type 1 for male
	
	
	ResUserInfo(int status,int u,int com,int sec,int s){
		super(3,status);	//for type 3
		uid = u;
		company = com;
		section = sec;
		sex =s;
	}
}