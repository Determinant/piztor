package com.macaroon.piztor;

public class RUserInfo {
	int uid;		//userid
	String username;     //username
	String nickname;	 //nickname
	double latitude;	//latitude
	double longitude; //longitude
	RGroup gid;          //gid
	int sex;		//type 0 for female,type 1 for male


	RUserInfo(int u,String user,String nick,double lat,double lot,RGroup g,int s){
		uid = u;
		username = user;
		nickname = nick;
		latitude = lat;
		longitude = lot;
		gid = g;
		sex =s;
	}
}