package com.macaroon.piztor;

import java.util.Vector;

//--------------------------------------//
//			  Push Location			    //
//--------------------------------------//

public class ResPushLocation extends Res{
	Vector<RLocation> l;	//vector for location info
	int n;					//number of location info
	
	ResPushLocation(int num,Vector<RLocation> locationvec){
		super(101,0);	//for type 101
		l = locationvec;
		n = num;
	}
}