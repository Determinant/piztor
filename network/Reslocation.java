package com.macaroon.piztor;

import java.util.Vector;

//--------------------------------------//
//			  Location Info			    //
//--------------------------------------//

public class Reslocation extends Res{
	Vector<Rlocation> l;	//vector for location info
	int n;					//number of location info
	
	Reslocation(int num,Vector<Rlocation> locationvec){
		super(3,255);	//for type 3
		l = locationvec;
		n = num;
	}
}