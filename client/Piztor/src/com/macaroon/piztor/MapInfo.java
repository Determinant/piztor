package com.macaroon.piztor;

import java.io.Flushable;
import java.util.HashMap;
import java.util.Vector;

import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.location.Location;


public class MapInfo {
	HashMap<Integer, UserInfo> mp;
	HashMap<Integer, MarkerInfo> pm;
	Vector<UserInfo> allUsers;
	Vector<MarkerInfo> markerInfo;
	UserInfo myInfo;
	myApp app;
	int myScore, otherScore;
	
	@SuppressLint("UseSparseArrays")
	MapInfo(myApp ap) {
		mp = new HashMap<Integer, UserInfo>();
		allUsers = new Vector<UserInfo>();
		markerInfo = new Vector<MarkerInfo>();
		pm = new HashMap<Integer, MarkerInfo>();
		app  = ap;
		myScore = otherScore = 0;
	}

	void clear() {
		mp.clear();
		allUsers.clear();
	}

	void addUserInfo(UserInfo userInfo) {
		allUsers.add(userInfo);
		mp.put(userInfo.uid, userInfo);
	}
	
	void addMarkerInfo(MarkerInfo mInfo) {
		markerInfo.add(mInfo);
		pm.put(mInfo.markerId, mInfo);
	}

	UserInfo getUserInfo(int uid) {
		if (mp.containsKey(uid))
			return mp.get(uid);
		else
			return null;
	}

	MarkerInfo getMarkerInfo(int mid) {
		if (pm.containsKey(mid))
			return pm.get(mid);
		else 
			return null;
	}

	void sendCheckin(int mid) {
		if (pm.containsKey(mid)) {
			ReqCheckin req = new ReqCheckin(app.token, app.username, mid, System.currentTimeMillis(), 10 * 1000);
			app.transam.send(req);
		}
	}
	
	void removeMarker(int mid) {
		if (pm.containsKey(mid)) {
			markerInfo.remove(pm.get(mid));
			pm.remove(mid);
		}
	}
	
	public Vector<UserInfo> getVector() {
		return allUsers;
	}
	
	public UserInfo getMyInfo() {
		return myInfo;
	}
}

class MarkerInfo {
	GeoPoint markerPoint;
	long markerTimestamp;
	int score;
	int markerId;
	int level;
	
	MarkerInfo copy() {
		MarkerInfo res = new MarkerInfo();
		res.level = level;
		res.markerPoint = new GeoPoint(markerPoint.getLatitudeE6(), markerPoint.getLongitudeE6());
		res.markerTimestamp = markerTimestamp;
		return res;
	}
	
}

class UserInfo {
	int uid, sex;
	int company;		//group id
	int section;
	int level;
	GeoPoint location;
	String nickname;
	
	UserInfo copy() {
		UserInfo res = new UserInfo(uid);
		res.location = location;
		res.nickname = nickname;
		res.section = section;
		res.company = company;
		res.level = level;
		return res;
	}
	
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
		this.nickname = nickName;
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
