package com.example.piztor;

import java.util.Vector;


public class Myrequest {
	public Vector<Object> contain;
	Myrequest(Vector<Object> info){
		contain = info;
	}
	static Myrequest login(String username, String passworld) {
		Vector<Object> v = new Vector<Object>();
		v.add(0);
		v.add(username);
		v.add(passworld);
		return new Myrequest(v);
	}
	
	static Myrequest updateLocation(int token, double lat, double lng) {
		Vector<Object> v = new Vector<Object>();
		v.add(2);
		v.add(token);
		v.add(lat);
		v.add(lng);
		return new Myrequest(v);
	}

	static Myrequest requestLocation(int token, int gid) {
		Vector<Object> v = new Vector<Object>();
		v.add(3);
		v.add(token);
		v.add(gid);
		return new Myrequest(v);
	}
	
}