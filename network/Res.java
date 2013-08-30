package com.macaroon.piztor;

import java.util.Vector;

public class Res{
	
	static final int Login =0;
	static final int Update =1;
	static final int UserInfo =2;
	static final int Subscription =3;
	static final int Logout =4;
	static final int StartPush =5;
	static final int SendMessage =6;
	
	static final int PushMessage =100;
	static final int PushLocation =101;
	
	int type;
	Res(int t){
		type = t;
	}
}

//--------------------------------------//
//			Respond to login			//
//--------------------------------------//

class ResLogin extends Res{
	String t;	//user token
	RUserInfo uinfo;      //userinfo
	Vector<RGroup> sublist;   //list of users subscribed
	int subscribeNumber;   //number of users subscribed

	ResLogin(String token,RUserInfo rui,Vector<RGroup> slist,int subn){
		super(0);	//for type 0
		t = token;
		uinfo = rui;
		sublist = slist;
		subscribeNumber = subn;
	}
}

//--------------------------------------//
//		Respond to update location		//
//--------------------------------------//

class ResUpdate extends Res{

	ResUpdate(){
		super(1);	//for type 1
	}
}

//--------------------------------------//
//			Respond to User Info	    //
//--------------------------------------//

class ResUserInfo extends Res{
	int number;        //number of users
	Vector<RUserInfo> uinfo;

	ResUserInfo(int n,Vector<RUserInfo> rui){
		super(2);	//for type 2
		number = n;
		uinfo = rui;
	}
}

//--------------------------------------//
//	Respond to Update Subscription		//
//--------------------------------------//

class ResSubscription extends Res{

	ResSubscription(){
		super(3);	//for type 3
	}
}

//--------------------------------------//
//			Respond to logout			//
//--------------------------------------//

class ResLogout extends Res{

	ResLogout(){
		super(4);	//for type 4
	}
}

//--------------------------------------//
//		Respond to start push     		//
//--------------------------------------//

class ResStartPush extends Res{

	ResStartPush(){
		super(5);	//for type 5
	}
}

//--------------------------------------//
//			Respond to send Message    	//
//--------------------------------------//

class ResSendMessage extends Res{

	ResSendMessage(){
		super(6);	//for type 6
	}
}

//---------------------------------------------------------------------------------------------------//



//---------------------------------------------------------------------------------------------------//



//--------------------------------------//
//			Push Message    			//
//--------------------------------------//

class ResPushMessage extends Res{
	String message;

	ResPushMessage(String s){
		super(100);	//for type 100
		message = s;
	}
}

//--------------------------------------//
//			Push Location			    //
//--------------------------------------//

class ResPushLocation extends Res{
	Vector<RLocation> l;	//vector for location info
	int n;					//number of location info

	ResPushLocation(int num,Vector<RLocation> locationvec){
		super(101);	//for type 101
		l = locationvec;
		n = num;
	}
}