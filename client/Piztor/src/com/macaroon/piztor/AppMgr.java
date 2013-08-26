package com.macaroon.piztor;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;

@SuppressLint("UseSparseArrays")
public class AppMgr {
	// Status
	public enum ActivityStatus {
		create, start, resume, restart, stop, pause, destroy
	}

	static ActivityStatus status;
	static PiztorAct nowAct;
	// TODO fix
	static Handler fromTransam, fromGPS;
	static Transam transam = null;
	static Tracker tracker = null;
	static Thread tTransam, tGPS;
	// Event

	final static int noToken = 101;
	final static int loginSuccess = 102;
	final static int errorToken = 103;
	final static int hasToken = 104;
	static HashMap<Class<?>, HashMap<Integer, Class<?>>> mp;

	static void setStatus(ActivityStatus st) {
		status = st;
	}

	static void trigger(int event) {
		Intent i = new Intent();
		System.out.println(nowAct.id + " : " + event);
		if (mp.get(nowAct.getClass()) == null)
			System.out.println("first");
		else if (mp.get(nowAct.getClass()) == null)
			System.out.println("second");
		i.setClass(nowAct, mp.get(nowAct.getClass()).get(event));
		if (event == errorToken)
			UserInfo.token = null;
		nowAct.startActivity(i);
	}

	static void add(Class<?> a, Integer event, Class<?> b) {
		if (mp.containsKey(a))
			mp.get(a).put(event, b);
		else {
			HashMap<Integer, Class<?>> h = new HashMap<Integer, Class<?>>();
			h.put(event, b);
			mp.put(a, h);
		}
	}

	static void addTransition(Class<?> a, int i, Class<?> b) {
		if (mp.containsKey(a)) {
			HashMap<Integer, Class<?>> h = mp.get(a);
			h.put(i, b);
			mp.put(a, h);
		} else {
			HashMap<Integer, Class<?>> h = new HashMap<Integer, Class<?>>();
			h.put(i, b);
			mp.put(a, h);
		}
	}

	static void addStatus(Class<?> a) {
		mp.put(a, new HashMap<Integer, Class<?>>());
	}

	static void init() {
		mp = new HashMap<Class<?>, HashMap<Integer, Class<?>>>();
		fromTransam = new Handler();
		transam = new Transam(UserInfo.ip, UserInfo.port, fromTransam);
		fromGPS = new Handler();
		tracker = new Tracker(nowAct.getApplicationContext(), fromGPS);
		tTransam = new Thread(transam);
		tTransam.start();
		tGPS = new Thread(tracker);
		tGPS.start();
		System.out.println("!!!!!!");
		addStatus(InitAct.class);
		addStatus(Login.class);
		addStatus(Main.class);
		addTransition(InitAct.class, noToken, Login.class);
		addTransition(Login.class, loginSuccess, Main.class);
		addTransition(Main.class, errorToken, Login.class);
		addTransition(Settings.class, errorToken, Login.class);
		addTransition(InitAct.class, hasToken, Main.class);
		addTransition(InitAct.class, errorToken, Login.class);
	}

}
