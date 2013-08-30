package com.macaroon.piztor;

import java.util.Vector;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Settings extends PiztorAct {
	Button logout;
	MapInfo mapInfo;
	Transam transam;
	// Event
	final static int logoutButtonPressed = 10;
	final static int logoutFailed = 11;

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			switch (m.what) {
			case Res.Update:// 上传自己信息成功or失败
				Log.d("update location", "successfull");
				break;
			case Res.UserInfo:// 得到用户信息
				ResUserInfo userInfo = (ResUserInfo) m.obj;
				System.out.println("revieve ........" + userInfo.uinfo.size());
				Vector<RUserInfo> uinfo = userInfo.uinfo;
				for (RUserInfo info : uinfo) {
					System.out.println(info.latitude + "     "
							+ info.longitude);
					UserInfo r = mapInfo.getUserInfo(info.uid);
					if (r != null) {
						r.setInfo(info.gid.company, info.gid.section, info.sex,
								info.nickname);
						r.setLocation(info.latitude, info.longitude);
					} else {
						r = new UserInfo(info.uid);
						r.setInfo(info.gid.company, info.gid.section, info.sex,
								info.nickname);
						r.setLocation(info.latitude, info.longitude);
						mapInfo.addUserInfo(r);
					}
				}
				break;
			case Res.Logout:// 登出
				actMgr.trigger(AppMgr.logout);
				break;
			case Res.PushMessage:
				ResPushMessage pushMessage = (ResPushMessage) m.obj;
				receiveMessage(pushMessage.message);
				break;
			case Res.SendMessage:
				Log.d(LogInfo.resquest, "send message successfully");
				break;
			case Res.PushLocation:
				ResPushLocation pushLocation = (ResPushLocation) m.obj;
				upMapInfo(pushLocation.l);
				break;
			default:
				break;
			}
		}
	};
	
	void upMapInfo(Vector<RLocation> l) {
		for (RLocation i : l) {
			UserInfo info = AppMgr.mapInfo.getUserInfo(i.id);
			if (info != null) {
				info.setLocation(i.latitude, i.longitude);
			} else {
				info = new UserInfo(i.id);
				info.setLocation(i.latitude, i.longitude);
				AppMgr.mapInfo.addUserInfo(info);
			}
		}
	}

	void receiveMessage(String msg) {
		Log.d("recieve message", msg);
		Toast toast = Toast.makeText(getApplicationContext(), msg,
				Toast.LENGTH_LONG);
		toast.show();
	}

	class LogoutStatus extends ActStatus {

		@Override
		void enter(int e) {
			System.out.println("!!!!!!!logout info send!!!!!!!!");
			transam.send(new ReqLogout(Infomation.token, Infomation.username,
					System.currentTimeMillis(), 2000));
		}

		@Override
		void leave(int e) {

		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mapInfo = AppMgr.mapInfo;
		transam = AppMgr.transam;
		if (transam == null)
			Log.d(LogInfo.exception, "transam = null");
		transam.setHandler(handler);
		ActStatus[] r = new ActStatus[3];
		ActStatus start = r[0] = new EmptyStatus();
		ActStatus logout = r[2] = new LogoutStatus();
		actMgr = new ActMgr(this, start, r);
		actMgr.add(start, logoutButtonPressed, logout);
		actMgr.add(logout, logoutFailed, start);
		setContentView(R.layout.activity_settings);
	}

	@Override
	protected void onStart() {
		super.onStart();
		logout = (Button) findViewById(R.id.settings_btn_logout);
		logout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				actMgr.trigger(logoutButtonPressed);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setting, menu);
		return true;
	}

}
