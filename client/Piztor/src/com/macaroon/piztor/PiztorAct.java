package com.macaroon.piztor;

import android.app.Activity;
import android.os.Bundle;

public class PiztorAct extends Activity {
	String id;
	ActMgr actMgr;
	AppMgr appMgr;
	MapInfo mapInfo;
	Transam transam;
	myApp app;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println(id + " on create");
		app = (myApp) getApplication();
		transam = app.transam;
		appMgr = app.appMgr;
		mapInfo = app.mapInfo;
		appMgr.addAct(this);
		appMgr.setStatus(AppMgr.ActivityStatus.create);
		appMgr.nowAct = this;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		System.out.println(id + " on start");
		appMgr.setStatus(AppMgr.ActivityStatus.start);
		appMgr.nowAct = this;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		System.out.println(id + " on stop");
		appMgr.setStatus(AppMgr.ActivityStatus.stop);
	}

	@Override
	protected void onResume() {
		super.onResume();
		System.out.println(id + " on resume");
		appMgr.setStatus(AppMgr.ActivityStatus.resume);
		appMgr.nowAct = this;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		System.out.println(id + " on pause");
		appMgr.setStatus(AppMgr.ActivityStatus.pause);
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		System.out.println(id + " on restart");
		appMgr.setStatus(AppMgr.ActivityStatus.restart);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
//		appMgr.removeAct(this);
		System.out.println(id + " on destroy");
		appMgr.setStatus(AppMgr.ActivityStatus.destroy);
	}
	
}
