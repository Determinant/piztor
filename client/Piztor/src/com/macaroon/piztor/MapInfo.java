package com.macaroon.piztor;

import java.util.HashMap;
import java.util.Vector;

import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.location.Location;


public class MapInfo {
	HashMap<Integer, UserInfo> mp;
	Vector<UserInfo> allUsers;
	UserInfo myInfo;

	@SuppressLint("UseSparseArrays")
	MapInfo() {
		mp = new HashMap<Integer, UserInfo>();
		allUsers = new Vector<UserInfo>();
	}

	void clear() {
		mp.clear();
		allUsers.clear();
	}

	void addUserInfo(UserInfo userInfo) {
		allUsers.add(userInfo);
		mp.put(userInfo.uid, userInfo);
	}

	UserInfo getUserInfo(int uid) {
		if (mp.containsKey(uid))
			return mp.get(uid);
		else
			return null;
	}

	public Vector<UserInfo> getVector() {
		return allUsers;
	}
	
	public UserInfo getMyInfo() {
		return myInfo;
	}
}

class UserInfo {
	int uid, sex;
	int company;		//group id
	int section;
	GeoPoint location;
	String nickname;
	UserInfo(int uid) {
		this.uid = uid;
	}

	void setLocation(double lat, double lot) {
		location = new GeoPoint((int)(lat * 1e6), (int)(lot * 1e6));
	}

	void setInfo(int company, int section, int sex, String nickName) {
		this.company = company;
		this.section = section;
		this.sex = sex;
	}
	
	public GeoPoint getLocation(){
		return location;
	}

	public double getLatitude() {
		return location.getLatitudeE6() / 1e6;
	}

	public double getLongitude() {
		return location.getLongitudeE6() / 1e6;
	}
}

interface Style {
	Drawable getDrawable(UserInfo user);
}
