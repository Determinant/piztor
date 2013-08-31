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
	private static final String strKey = "E612ee541946c90d7b9add0347c9e601";
	BMapManager mBMapManager;
	boolean isExiting = false;
	boolean isLogout = false;
	String ip = "202.120.7.4";
//	static String ip = "69.85.86.42";
//	static String ip = "192.168.1.101";
	int port = 2223;
	String token = null;
	String username = null;
	Vector<RGroup> sublist;

	AppMgr appMgr;

	Handler handler;
	Transam transam;
	Thread tTransam;

	MapInfo mapInfo;

	@Override
	public void onCreate() {
		super.onCreate();
		System.out.println("我执行了，好开心~~~~~~~~~~");
		mapInfo = new MapInfo();
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
