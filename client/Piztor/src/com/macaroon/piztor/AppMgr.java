package com.macaroon.piztor;

import java.util.HashMap;
import java.util.Stack;

import android.annotation.SuppressLint;
import android.content.Intent;

@SuppressLint("UseSparseArrays")
public class AppMgr {
	// Event
	final static int noToken = 101;
	final static int loginSuccess = 102;
	final static int errorToken = 103;
	final static int hasToken = 104;
	final static int toSettings = 105;
	final static int logout = 106;
	final static int subscribe = 107;
	final static int account = 108;
	final static int finish = 109;

	public enum ActivityStatus {
		create, start, resume, restart, stop, pause, destroy
	}

	myApp app;
	HashMap<Class<?>, HashMap<Integer, Class<?>>> mp;
	Stack<PiztorAct> acts;
	ActivityStatus status;
	PiztorAct nowAct;

	void addAct(PiztorAct act) {
		if (acts == null)
			acts = new Stack<PiztorAct>();
		acts.push(act);
	}

	/*
	 * void removeAct(PiztorAct act) { if (acts.contains(act)) acts.remove(act);
	 * else System.out.println("Piztor has a bug!!!!"); }
	 */

	void exit() {
		while (!acts.isEmpty()) {
			acts.peek().finish();
			acts.pop();
		}
		app.token = null;
		app.mBMapManager.destroy();
	}

	void setStatus(ActivityStatus st) {
		status = st;
	}

	void trigger(int event) {
		if (event == finish) {
			nowAct.finish();
			return;
		}
		if (event == errorToken)
			app.token = null;
		if (event == loginSuccess || event == hasToken) {
			app.mBMapManager.start();
			app.mapInfo.clear();
		}
		if (event == logout) {
			System.out.println("我来停一发！！！！");
			app.isLogout = true;
			app.mBMapManager.stop();
			nowAct.finish();
			return;
		}
		Intent i = new Intent();
		i.setClass(nowAct, mp.get(nowAct.getClass()).get(event));
		nowAct.startActivity(i);
	}

	void add(Class<?> a, Integer event, Class<?> b) {
		if (mp.containsKey(a))
			mp.get(a).put(event, b);
		else {
			HashMap<Integer, Class<?>> h = new HashMap<Integer, Class<?>>();
			h.put(event, b);
			mp.put(a, h);
		}
	}

	void addTransition(Class<?> a, int i, Class<?> b) {
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

	void addStatus(Class<?> a) {
		mp.put(a, new HashMap<Integer, Class<?>>());
	}

	public AppMgr(myApp app) {
		mp = new HashMap<Class<?>, HashMap<Integer, Class<?>>>();
		this.app = app;
		addStatus(InitAct.class);
		addStatus(Login.class);
		addStatus(Main.class);
		addStatus(Settings.class);
		addTransition(UpdateInfo.class, logout, Login.class);
		addTransition(Settings.class, subscribe, SubscribeSettings.class);
		addTransition(SubscribeSettings.class, logout, Login.class);
		addTransition(Settings.class, account, UpdateInfo.class);
		addTransition(InitAct.class, noToken, Login.class);
		addTransition(InitAct.class, hasToken, Main.class);
		addTransition(InitAct.class, errorToken, Login.class);
		addTransition(Main.class, logout, Login.class);
		addTransition(Main.class, toSettings, Settings.class);
		addTransition(Main.class, errorToken, Login.class);
		addTransition(Login.class, loginSuccess, Main.class);
		addTransition(Settings.class, logout, Login.class);
		addTransition(Settings.class, errorToken, Login.class);
	}

}
