package com.macaroon.piztor;

class RMarker{
	double latitude;
	double longitude;
	int deadline;
	int level;
	int markerID;
	int score;
	
	RMarker(double lat,double lot,int dtime,int lv,int markerid,int s){
		latitude = lat;
		longitude = lot;
		deadline = dtime;
		level = lv;
		markerID = markerid;
		score = s;
	}
}