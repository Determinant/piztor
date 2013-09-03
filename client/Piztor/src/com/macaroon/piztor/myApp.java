package com.macaroon.piztor;
import java.util.Vector;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.util.Log;

@SuppressLint("UseSparseArrays")
public class myApp extends Application {
	private static final String strKey = "579bf85175473ea618258a7a3d3ba63b";
	BMapManager mBMapManager;
	boolean isExiting = false;
	boolean isLogout = false;
//	String ip = "202.120.7.4";
	static String ip = "69.85.86.42";
//	static String ip = "192.168.1.171";
	int port = 2224;
	String token = null;
	String username = null;
	Vector<RGroup> sublist;

	AppMgr appMgr;

	Handler handler;
	Transam transam;
	Thread tTransam;

	MapInfo mapInfo;
	boolean gameStarted = false;

	@Override
	public void onCreate() {
		super.onCreate();
		System.out.println("我执行了，好开心~~~~~~~~~~");
		mapInfo = new MapInfo(this);
		mBMapManager = new BMapManager(this);

		appMgr = new AppMgr(this);
		System.out.println("appmgr already!!!");
		transam = new Transam(ip, port, new Handler());
		tTransam = new Thread(transam);
		System.out.println("transam start!!!!");
		tTransam.start();
	}

	public static String getStrkey() {
		return strKey;
	}

}
