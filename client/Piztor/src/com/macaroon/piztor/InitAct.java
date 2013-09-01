package com.macaroon.piztor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.baidu.mapapi.MKGeneralListener;

public class InitAct extends PiztorAct {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		id = "initAct";
		super.onCreate(savedInstanceState);
		app.isExiting = false;
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
		if (app.isExiting)
			finish();
		super.onResume();
		if (app.token == null || app.isLogout) {
			app.appMgr.trigger(AppMgr.noToken);
		}
		else {
			app.appMgr.trigger(AppMgr.hasToken);
			System.out.println("has token!!!");
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
