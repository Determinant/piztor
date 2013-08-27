package com.macaroon.piztor;

import java.util.HashMap;
import java.util.HashSet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;

@SuppressLint("UseSparseArrays")
public class AppMgr {
	
	private static final String strKey = "5ba8abf7b4694ad49706b8b7538c9d6a";
    static BMapManager mBMapManager = null;
	static Context context;
	
	
	// Status
	public enum ActivityStatus {
		create, start, resume, restart, stop, pause, destroy
	}

	static ActivityStatus status;
	static PiztorAct nowAct;
	// TODO fix
	static Handler handler, fromGPS;
	static Transam transam = null;
	static Tracker tracker = null;
	static Thread tTransam, tGPS;
	// Event
	final static int noToken = 101;
	final static int loginSuccess = 102;
	final static int errorToken = 103;
	final static int hasToken = 104;
	final static int toSettings = 105;
	final static int logout = 106;
	
	static MapInfo mapInfo;
	
	static HashMap<Class<?>, HashMap<Integer, Class<?>>> mp;
	static HashSet<PiztorAct> acts;

	static void addAct(PiztorAct act) {
		if (acts == null)
			acts = new HashSet<PiztorAct>();
		acts.add(act);
	}

	static void removeAct(PiztorAct act) {
		if (acts.contains(act))
			acts.remove(act);
		else
			System.out.println("Piztor has a bug!!!!");
	}

	static void exit() {
		for (PiztorAct act : acts) {
			act.finish();
		}
	}

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
			Infomation.token = null;
		if (event == toSettings) {
			if (nowAct.actMgr.nowStatus.getClass() == Main.FetchStatus.class)
				i.putExtra("status", true);
			else i.putExtra("status", false);
		}
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

	static void init(Context context) {
		if (mBMapManager == null) {
			mBMapManager = new BMapManager(context);
			mBMapManager.init(strKey, new MKGeneralListener(){
				@Override
		        public void onGetNetworkState(int iError) {
		            Log.d("Network","failure");
		        }

		        @Override
		        public void onGetPermissionState(int iError) {
		            Log.d("Permission","wrong key");
		        }
			});
		}
		AppMgr.context = context;
		mp = new HashMap<Class<?>, HashMap<Integer, Class<?>>>();
		handler = new Handler();
		transam = new Transam(Infomation.ip, Infomation.port, handler);
		tTransam = new Thread(transam);
		tTransam.start();
		mapInfo = new MapInfo();
		Infomation.myInfo = new UserInfo(-1);
		addStatus(InitAct.class);
		addStatus(Login.class);
		addStatus(Main.class);
		addStatus(Settings.class);
		addTransition(Main.class, logout, Login.class);
		addTransition(InitAct.class, noToken, Login.class);
		addTransition(Login.class, loginSuccess, Main.class);
		addTransition(Main.class, errorToken, Login.class);
		addTransition(Settings.class, errorToken, Login.class);
		addTransition(InitAct.class, hasToken, Main.class);
		addTransition(InitAct.class, errorToken, Login.class);
		addTransition(Main.class, toSettings, Settings.class);
		addTransition(Settings.class, logout, Login.class);
	}

}
