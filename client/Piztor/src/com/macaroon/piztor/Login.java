package com.macaroon.piztor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends PiztorAct {

	Button btnLogin;
	EditText edtUser, edtPass;
	int loginButtonClick = 1, retryButtonClick = 2, loginFailed = 3;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			System.out.println("receive what : " + m.what);
			if (m.what == -1) {
				((Exception) m.obj).printStackTrace();
				actMgr.trigger(loginFailed);
				return;
			}
			if (m.what == 0) {
				ResLogin res = (ResLogin) m.obj;
				Log.d(LogInfo.login, LogInfo.s);
				app.token = res.t;
				app.sublist = res.sublist;
				app.username = res.uinfo.username;
				app.mapInfo.myInfo = new UserInfo(res.uinfo.uid);
				app.mapInfo.myInfo.setInfo(res.uinfo.gid.company,
						res.uinfo.gid.section, res.uinfo.sex,
						res.uinfo.nickname);
				app.mapInfo.myInfo.level = res.uinfo.level;
				app.mapInfo.myInfo.nickname = res.uinfo.nickname;
				System.out.println("login !!!!" + res.sublist.size());
				actMgr.trigger(AppMgr.loginSuccess);
			} else {
				System.out.println("login handler reveive other info   :  "
						+ m.what);
			}
		}
	};

	class StartStatus extends ActStatus {

		@Override
		void enter(int e) {
		}

		@Override
		void leave(int e) {
		}
	}

	class LoginStatus extends ActStatus {

		@Override
		void enter(int e) {
			String user = edtUser.getText().toString();
			String pass = edtPass.getText().toString();
			long nowtime = System.currentTimeMillis();
			System.out.println(user + " : " + pass + "\n");
			transam.send(new ReqLogin(user, pass, nowtime, 5000));
		}

		@Override
		void leave(int e) {
			if (e == loginFailed) {
				Toast toast = Toast.makeText(getApplicationContext(),
						"login failed", Toast.LENGTH_LONG);
				toast.show();
			} else {
				System.out.println("fuck!!!asdfasdfasdf!");
			}
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		id = "login";
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	    requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_login);
		setProgressBarIndeterminateVisibility(true); 
		setProgressBarVisibility(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		btnLogin = (Button) findViewById(R.id.login_btn_login);
		edtUser = (EditText) findViewById(R.id.user_id);
		edtPass = (EditText) findViewById(R.id.user_pass);
		btnLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				actMgr.trigger(loginButtonClick);
			}
		});
	}

	@Override
	protected void onResume() {
		if (app.isExiting)
			finish();
		app.isLogout = false;
		super.onResume();
		ActStatus[] r = new ActStatus[2];
		r[0] = new StartStatus();
		r[1] = new LoginStatus();
		actMgr = new ActMgr(appMgr, this, r[0], r);
		actMgr.add(r[0], loginButtonClick, r[1]);
		actMgr.add(r[1], loginFailed, r[0]);
		transam.setHandler(handler);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			app.isExiting = true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

}
