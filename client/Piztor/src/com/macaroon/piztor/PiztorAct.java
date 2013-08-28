package com.macaroon.piztor;

import android.app.Activity;
import android.os.Bundle;

public class PiztorAct extends Activity {
	String id;
	ActMgr actMgr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println(id + " on create");
		AppMgr.addAct(this);
		AppMgr.setStatus(AppMgr.ActivityStatus.create);
		AppMgr.nowAct = this;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		System.out.println(id + " on start");
		AppMgr.setStatus(AppMgr.ActivityStatus.start);
		AppMgr.nowAct = this;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		System.out.println(id + " on stop");
		AppMgr.setStatus(AppMgr.ActivityStatus.stop);
	}

	@Override
	protected void onResume() {
		super.onResume();
		System.out.println(id + " on resume");
		AppMgr.setStatus(AppMgr.ActivityStatus.resume);
		AppMgr.nowAct = this;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		System.out.println(id + " on pause");
		AppMgr.setStatus(AppMgr.ActivityStatus.pause);
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		System.out.println(id + " on restart");
		AppMgr.setStatus(AppMgr.ActivityStatus.restart);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppMgr.removeAct(this);
		System.out.println(id + " on destroy");
		AppMgr.setStatus(AppMgr.ActivityStatus.destroy);
	}
	
}
