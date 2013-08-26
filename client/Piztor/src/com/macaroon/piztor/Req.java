package com.macaroon.piztor;

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