package com.macaroon.piztor;

public class Req{
	int type;		//request type
	String token;		//authentciation
	long time;		//current time
	long alive;		//alive time
	Req(int t,String k,long tm,long av){
		type = t;
		token = k;
		time = tm;
		alive = av;
	}
}