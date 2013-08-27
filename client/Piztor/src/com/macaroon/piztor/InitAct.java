package com.macaroon.piztor;

import android.os.Bundle;
import android.view.Menu;

public class InitAct extends PiztorAct {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		id = "initAct";
		super.onCreate(savedInstanceState);
		AppMgr.init();
		AppMgr.transam.setTimeOutTime(5000);
		setContentView(R.layout.activity_init);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (Infomation.token == null)
			AppMgr.trigger(AppMgr.noToken);
		else {
			//TODO jump to main
			AppMgr.trigger(AppMgr.hasToken);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//TODO 减少频率
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.init, menu);
		return false;
	}

}
