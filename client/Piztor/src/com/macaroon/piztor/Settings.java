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
	final static int logoutButtonPressed = 10;
	final static int logoutFailed = 11;
	
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!!settings" +  m.what);
			switch (m.what) {
			case 1:// 上传自己信息成功or失败
				ResUpdate update = (ResUpdate) m.obj;
				if (update.status == 0)
					System.out.println("update success");
				else {
					System.out.println("update failed");
					actMgr.trigger(AppMgr.errorToken);
				}
				break;
			case 2:// 得到别人的信息
				ResLocation location = (ResLocation) m.obj;
				if (location.status == 0) {
					mapInfo.clear();
					for (RLocation i : location.l) {
						System.out.println(i.id + " : " + i.latitude + " " + i.longitude);
						UserInfo info = new UserInfo(i.id);
						info.setLocation(i.latitude, i.longitude);
						mapInfo.addUserInfo(info);
					}
				} else {
					System.out.println("resquest for location failed!");
					actMgr.trigger(AppMgr.errorToken);
				}
				break;
			case 3:// 得到用户信息
				ResUserInfo r = (ResUserInfo) m.obj;
				if (r.status == 0) {
					System.out.println("id : " + r.uid + " sex :  " + r.sex
							+ " group : " + r.section);
					UserInfo user = mapInfo.getUserInfo(r.uid);
					user.setInfo(r.company, r.section, r.sex);
				} else {
					System.out.println("reqest for userInfo must be wrong!!!");
					actMgr.trigger(AppMgr.errorToken);
				}
				break;
			case 4:// 登出
				ResLogout logout = (ResLogout) m.obj;
				System.out.println("logout status" + logout.status);
				if (logout.status == 0) {
					Infomation.token = null;
					Infomation.myInfo.company = -1;
					Infomation.myInfo.section = -1;
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
			System.out.println("!!!!!!!logout info send!!!!!!!!");
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
