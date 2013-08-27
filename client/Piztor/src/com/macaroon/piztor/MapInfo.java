package com.macaroon.piztor;

import java.util.HashMap;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;

import com.baidu.platform.comapi.basestruct.GeoPoint;

public class MapInfo {
	HashMap<Integer, UserInfo> mp;
	Vector<UserInfo> allUsers;
	UserInfo myInfo;
	Style layout;

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

	void setStyle(Style layout) {
		this.layout = layout;
	}

	UserInfo getUserInfo(int uid) {
		if (mp.containsKey(uid))
			return mp.get(uid);
		else
			return null;
	}

}

class UserInfo {
	int uid, gid, sex;
	GeoPoint p;

	UserInfo(int uid) {
		this.uid = uid;
	}

	void setLocation(double lat, double lot) {
		p = new GeoPoint((int) (lat * 10e6), (int) (lot * 10e6));
	}

	void setInfo(int gid, int sex) {
		this.gid = gid;
		this.sex = sex;
	}

}

interface Style {
	Drawable getDrawable(UserInfo user);
}