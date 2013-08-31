package com.macaroon.piztor;

import com.baidu.mapapi.MKGeneralListener;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class InitAct extends PiztorAct {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		id = "initAct";
		super.onCreate(savedInstanceState);
		app.mBMapManager.init(app.getStrkey(), new MKGeneralListener() {
			@Override
			public void onGetNetworkState(int iError) {
				Log.d("Network", "failure");
			}

			@Override
			public void onGetPermissionState(int iError) {
				Log.d("Permission", "wrong key");
			}
		});
		setContentView(R.layout.activity_init);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (app.token == null)
			app.appMgr.trigger(AppMgr.noToken);
		else {
			System.out.println("has token!!!");
			app.appMgr.trigger(AppMgr.hasToken);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.init, menu);
		return false;
	}
	
	@Override
	public void finishFromChild (Activity child) {
		finish();
	}

}
