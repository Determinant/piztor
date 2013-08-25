package com.macaroon.piztor;

import android.annotation.SuppressLint;
import java.util.HashMap;
import java.util.Vector;

public class Infomation {
	static String ip = "69.85.86.42";
	static int port = 9990;
	static int token = -1;
	static int myId = -1;
	static int myGroup = -1;
	class UserInfo {
		int id;
		double lat, lot;
	}

	class Group {
		int id;
		Vector<UserInfo> v;
	}
	
	static HashMap<Integer, Group> mp;
	
	@SuppressLint("UseSparseArrays")
	static void init() {
		mp = new HashMap<Integer, Group>();
	}
	
}
