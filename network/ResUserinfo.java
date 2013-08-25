package com.macaroon.piztor;

import java.util.Vector;

//--------------------------------------//
//			Respond to User Info	    //
//--------------------------------------//

public class ResUserinfo extends Res{
	Vector<RUserinfo> l;	//user info
	
	ResUserinfo(int status,Vector<RUserinfo> userinfo){
		super(3,status);	//for type 3
		l = userinfo;
	}
}