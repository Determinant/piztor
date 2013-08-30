package com.macaroon.piztor;

import android.os.Bundle;
import android.view.Menu;
import android.content.Context;

public class InitAct extends PiztorAct {

	private CopyMap copyMap;
	private String assetDir;
	private String dir;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		id = "initAct";
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_init);
		copyMap = new CopyMap();
		copyMap.testCopy(InitAct.this);
		AppMgr.init(getApplicationContext());
		AppMgr.transam.setTimeOutTime(5000);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (Infomation.token == null)
			AppMgr.trigger(AppMgr.noToken);
		else {
			AppMgr.trigger(AppMgr.hasToken);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.init, menu);
		return false;
	}

}
