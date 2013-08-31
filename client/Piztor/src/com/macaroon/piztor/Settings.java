package com.macaroon.piztor;

import java.lang.ref.WeakReference;
import java.util.Vector;

import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends PiztorAct {
	Button logout, subscribe, account;
	MapInfo mapInfo;
	Transam transam;
	// Event
	final static int logoutButtonPressed = 10;
	final static int logoutFailed = 11;
	
	private int currentRate;

	static class ReCall extends Handler {
		WeakReference<Settings> outerClass;

		ReCall(Settings activity) {
			outerClass = new WeakReference<Settings>(activity);
		}

		@Override
		public void handleMessage(Message m) {
			Settings out = outerClass.get();
			if (out == null) {
				System.out.println("act被回收了");
			}
			switch (m.what) {
			case Res.Login:// 上传自己信息成功or失败
				Log.d("update location", "successfull");
				break;
			case Res.UserInfo:// 得到用户信息
				ResUserInfo userInfo = (ResUserInfo) m.obj;
				System.out.println("revieve ........" + userInfo.uinfo.size());
				Vector<RUserInfo> uinfo = userInfo.uinfo;
				for (RUserInfo info : uinfo) {
					System.out
							.println(info.latitude + "     " + info.longitude);
					UserInfo r = out.mapInfo.getUserInfo(info.uid);
					if (r != null) {
						r.setInfo(info.gid.company, info.gid.section, info.sex,
								info.nickname);
						r.setLocation(info.latitude, info.longitude);
					} else {
						r = new UserInfo(info.uid);
						r.setInfo(info.gid.company, info.gid.section, info.sex,
								info.nickname);
						r.setLocation(info.latitude, info.longitude);
						out.mapInfo.addUserInfo(r);
					}
				}
				System.out.println("now has info number : "
						+ out.mapInfo.allUsers.size());
				break;
			case Res.Logout:// 登出
				out.actMgr.trigger(AppMgr.logout);
				break;
			case Res.PushMessage:
				ResPushMessage pushMessage = (ResPushMessage) m.obj;
				out.receiveMessage(pushMessage.message);
				break;
			case Res.SendMessage:
				Log.d(LogInfo.resquest, "send message successfully");
				break;
			case Res.PushLocation:
				ResPushLocation pushLocation = (ResPushLocation) m.obj;
				out.upMapInfo(pushLocation.l);
				break;
			case Res.PushMarker:
				ResPushMarker pushMarker = (ResPushMarker) m.obj;
				MarkerInfo markerInfo = new MarkerInfo();
				markerInfo.level = pushMarker.level;
				markerInfo.markerPoint = new GeoPoint((int)(pushMarker.latitude * 1e6), (int)(pushMarker.longitude * 1e6));
				markerInfo.markerTimestamp = pushMarker.deadline;
				break;
			case -1:
				out.actMgr.trigger(AppMgr.logout);
			default:
				break;
			}
		}
	}
	ReCall handler = new ReCall(this);
	void upMapInfo(Vector<RLocation> l) {
		for (RLocation i : l) {
			UserInfo info = mapInfo.getUserInfo(i.id);
			if (info != null) {
				info.setLocation(i.latitude, i.longitude);
			} else {
				info = new UserInfo(i.id);
				info.setLocation(i.latitude, i.longitude);
				mapInfo.addUserInfo(info);
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
			transam.send(new ReqLogout(app.token, app.username,
					System.currentTimeMillis(), 2000));
		}

		@Override
		void leave(int e) {

		}

	}

	public class mySeekBarListener implements SeekBar.OnSeekBarChangeListener {
		
		private SeekBar seekBar;
		private TextView refreshrate;
	    		
		public mySeekBarListener() {
			Log.d("seek", "new");
	        seekBar = (SeekBar) Settings.this.findViewById(R.id.settings_GPSrefreshrate_bar);
	        refreshrate = (TextView) Settings.this.findViewById(R.id.settings_GPSrefreshrate);
	        seekBar.setOnSeekBarChangeListener(this);
	    }
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (progress == 0) progress = 1;
			refreshrate.setText(progress + "s一次更新");
			currentRate = progress;
			Log.d("seek", "cur " + progress);
		}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (myApp) getApplication();
		appMgr = app.appMgr;
		mapInfo = app.mapInfo;
		transam = app.transam;
		transam.setHandler(handler);
		ActStatus[] r = new ActStatus[3];
		ActStatus start = r[0] = new EmptyStatus();
		ActStatus logout = r[2] = new LogoutStatus();
		actMgr = new ActMgr(appMgr, this, start, r);
		actMgr.add(start, logoutButtonPressed, logout);
		actMgr.add(logout, logoutFailed, start);
		setContentView(R.layout.activity_settings);
	}

	@Override
	protected void onStart() {
		super.onStart();
		logout = (Button) findViewById(R.id.settings_btn_logout);
		subscribe = (Button) findViewById(R.id.settings_btn_subscribe);
		account = (Button) findViewById(R.id.settings_btn_updateinfo);
		initGPSrate();
		logout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				actMgr.trigger(logoutButtonPressed);
			}
		});
		subscribe.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				actMgr.trigger(AppMgr.subscribe);
			}
		});
		account.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				actMgr.trigger(AppMgr.account);
			}
		});
	}

	private void setGPSrate() {
		if (currentRate == 0) currentRate = 1;
		Main.GPSrefreshrate = currentRate;
	}
	
	private void initGPSrate() {
		currentRate = Main.GPSrefreshrate;
		TextView text1 = (TextView) Settings.this.findViewById(R.id.settings_GPSrefreshrate);
		text1.setText(currentRate + "s each update");
		SeekBar bar1 = (SeekBar) Settings.this.findViewById(R.id.settings_GPSrefreshrate_bar);
		bar1.setProgress(currentRate);
		bar1.setOnSeekBarChangeListener(new mySeekBarListener());
	}
	
	@Override
	protected void onDestroy() {
		setGPSrate();
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		setGPSrate();
		super.onPause();
	}
	
	@Override
	protected void onPause() {
		setGPSrate();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
        super.onResume();
        if (app.isExiting || app.isLogout)
            finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setting, menu);
		return true;
	}

}
