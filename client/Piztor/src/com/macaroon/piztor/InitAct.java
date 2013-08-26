package com.macaroon.piztor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;

public class InitAct extends PiztorAct {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		id = "initAct";
		super.onCreate(savedInstanceState);
		AppMgr.init();
		setContentView(R.layout.activity_init);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (UserInfo.token == null)
			AppMgr.trigger(AppMgr.noToken);
		else {
			//TODO jump to main
			AppMgr.trigger(AppMgr.hasToken);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.init, menu);
		return true;
	}

}
