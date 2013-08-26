package com.macaroon.piztor;

import java.util.Vector;

//--------------------------------------//
//			  Location Info			    //
//--------------------------------------//

public class ResLocation extends Res{
	Vector<Rlocation> l;	//vector for location info
	int n;					//number of location info
	
	ResLocation(int num,int status,Vector<Rlocation> locationvec){
		super(2,status);	//for type 2
		l = locationvec;
		n = num;
	}
}