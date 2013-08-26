package com.macaroon.piztor;


//--------------------------------------//
//			Respond to User Info	    //
//--------------------------------------//

public class ResUserinfo extends Res{
	int uid;		//userid
	int gid;		//groupid
	int sex;		//type 0 for female,type 1 for male
	
	
	ResUserinfo(int status,int u,int g,int s){
		super(3,status);	//for type 3
		uid = u;
		gid = g;
		sex =s;
	}
}