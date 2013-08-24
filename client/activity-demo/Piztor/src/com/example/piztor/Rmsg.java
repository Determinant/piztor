package com.example.piztor;

class Rmsg{
	int id;
	double lat;
	double lot;
	Rmsg(int i,double l,double ll){
		id = i;
		lat = l;
		lot = ll;
	}
	public String toString() { 
		return id + "  " + lat + " " + lot;
	}
}

