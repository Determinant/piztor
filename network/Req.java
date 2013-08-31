package com.macaroon.piztor;

import java.util.Vector;

public class Req{
	int type;		//request type
	String token;		//authentciation
	String uname;       //username
	long time;		//current time
	long alive;		//alive time
	Req(int t,String k,String name,long tm,long av){
		type = t;
		token = k;
		uname = name;
		time = tm;
		alive = av;
	}
}

//--------------------------------------//
//			authentication				//
//--------------------------------------//

class ReqLogin extends Req{
	String user;	//username
	String pass;	//password

	ReqLogin(String u,String p,long time,long alive){
		super(0,"","",time,alive);	//for type 0
		user = u;
		pass = p;
	}
}

//--------------------------------------//
//			Update Location				//
//--------------------------------------//

class ReqUpdate extends Req{
	double latitude;	//latitude
	double longitude; //longitude

	ReqUpdate(String token,String name,double lat,double lot,long time,long alive){
		super(1,token,name,time,alive);	//for type 1
		latitude = lat;
		longitude = lot;
	}
}

//--------------------------------------//
//			Ask user info				//
//--------------------------------------//

class ReqUserInfo extends Req{
	RGroup gid;       //group id


	ReqUserInfo(String token,String name,RGroup id,long time,long alive){
		super(2,token,name,time,alive);	//for type 2
		gid = id;
	}
}

//--------------------------------------//
//			Subscription				//
//--------------------------------------//

class ReqSubscription extends Req{
	int n;			  //number of users you want to subscirbe
	Vector<RGroup> slist;       //list of users' group id


	ReqSubscription(String token,String name,int number,Vector<RGroup> sublist,long time,long alive){
		super(3,token,name,time,alive);	//for type 3
		n = number;
		slist = sublist;
	}
}

//--------------------------------------//
//				Log out      		    //
//--------------------------------------//

class ReqLogout extends Req{

	ReqLogout(String token,String name,long time,long alive){
		super(4,token,name,time,alive);	//for type 4
	}
}

//--------------------------------------//
//			Start push       			//
//--------------------------------------//

class ReqStartPush extends Req{

	ReqStartPush(String token,String name){
		super(5,token,name,(long) 0,(long) 0);	//for type 5
	}
}

//--------------------------------------//
//			Send Message				//
//--------------------------------------//

class ReqSendMessage extends Req{
	String msg;

	ReqSendMessage(String token,String name,String message,long time,long alive){
		super(6,token,name,time,alive);	//for type 6
		msg = message;
	}
}

//--------------------------------------//
//			Set Marker					//
//--------------------------------------//

class ReqSetMarker extends Req{
	double latitude;
	double longitude;
	int deadline;

	ReqSetMarker(String token,String name,double lat,double lot,int dtime,long time,long alive){
		super(7,token,name,time,alive);	//for type 7
		latitude = lat;
		longitude = lot;
		deadline = dtime;
	}
}

//--------------------------------------//
//			Set Password				//
//--------------------------------------//

class ReqSetPassword extends Req{
	String oldpassword;
	String newpassword;

	ReqSetPassword(String token,String name,String oldpass,String newpass,long time,long alive){
	super(8,token,name,time,alive);	//for type 8
	oldpassword = oldpass;
	newpassword = newpass;
	}
}

