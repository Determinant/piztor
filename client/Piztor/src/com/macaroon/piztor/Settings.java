package com.macaroon.piztor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Settings extends PiztorAct {
	Button logout;
	MapInfo mapInfo;

	// Event
	final static int logoutButtonPressed = 1;
	final static int logoutFailed = 2;
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!!settings" +  m.what);
			switch (m.what) {
			case 1:// 上传自己信息成功or失败
				ResUpdate update = (ResUpdate) m.obj;
				if (update.s == 0)
					System.out.println("update success");
				else {
					System.out.println("update failed");
					actMgr.trigger(AppMgr.errorToken);
				}
				break;
			case 2:// 得到别人的信息
				ResLocation location = (ResLocation) m.obj;
				if (location.s == 0) {
					mapInfo.clear();
					for (Rlocation i : location.l) {
						System.out.println(i.i + " : " + i.lat + " " + i.lot);
						UserInfo info = new UserInfo(i.i);
						info.setLocation(i.lat, i.lot);
						mapInfo.addUserInfo(info);
					}
				} else {
					System.out.println("resquest for location failed!");
					actMgr.trigger(AppMgr.errorToken);
				}
				break;
			case 3:// 得到用户信息
				ResUserinfo r = (ResUserinfo) m.obj;
				if (r.s == 0) {
					System.out.println("id : " + r.uid + " sex :  " + r.sex
							+ " group : " + r.gid);
					UserInfo user = mapInfo.getUserInfo(r.uid);
					user.setInfo(r.gid, r.sex);
				} else {
					System.out.println("reqest for userInfo must be wrong!!!");
					actMgr.trigger(AppMgr.errorToken);
				}
				break;
			case 4:// 登出
				ResLogout logout = (ResLogout) m.obj;
				System.out.println("logout status" + logout.s);
				if (logout.s == 0) {
					Infomation.token = null;
					Infomation.myInfo.gid = -1;
					Infomation.myInfo.uid = -1;
					Infomation.username = null;
					actMgr.trigger(AppMgr.logout);
					break;
				} else {
					Toast toast = Toast.makeText(getApplicationContext(),
							"logout failed", Toast.LENGTH_LONG);
					toast.show();
					actMgr.trigger(logoutFailed);
				}
				break;
			default:
				break;
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

	class LogoutStatus extends ActStatus {

		@Override
		void enter(int e) {
			AppMgr.transam.send(new ReqLogout(Infomation.token,
					Infomation.username, System.currentTimeMillis(), 2000));
		}

		@Override
		void leave(int e) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActStatus[] r = new ActStatus[3];
		ActStatus start = r[0] = new StartStatus();
		ActStatus logout = r[2] = new LogoutStatus();
		actMgr = new ActMgr(this, start, r);
		actMgr.add(start, logoutButtonPressed, logout);
		actMgr.add(logout, logoutFailed, start);
		AppMgr.transam.setHandler(handler);
		mapInfo = AppMgr.mapInfo;
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
