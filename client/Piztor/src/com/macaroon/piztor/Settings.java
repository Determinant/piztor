package com.macaroon.piztor;

import java.lang.ref.WeakReference;
import java.util.Vector;

import com.baidu.location.LocationClientOption;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup.OnCheckedChangeListener;import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends PiztorAct {
	
	public final int show_by_team = 1;
	public final int show_by_sex = 2;
	
	Button logout, subscribe, account;
	MapInfo mapInfo;
	Transam transam;
	private int currentRate;
	OnCheckedChangeListener colorButtonListener = null;
	OnCheckedChangeListener locateButtonListener = null;
	static RadioGroup colorRadioGroup;
	static RadioGroup locateRadioGroup;
	
	// Event
	final static int logoutButtonPressed = 10;
	final static int logoutFailed = 11;

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
		colorRadioGroup = (RadioGroup)this.findViewById(R.id.colorRadioGroup);
		if (Main.colorMode == Main.show_by_sex) colorRadioGroup.check(R.id.show_by_sex);
		else colorRadioGroup.check(R.id.show_by_team);
        colorButtonListener = new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.show_by_team){
					setColorMode(show_by_team);
				}
				if (checkedId == R.id.show_by_sex){
					setColorMode(show_by_sex);
					Toast.makeText(Settings.this, "蓝色表示同性，红色表示异性", Toast.LENGTH_LONG).show();
				}
			}
		};
		colorRadioGroup.setOnCheckedChangeListener(colorButtonListener);
		
		locateRadioGroup = (RadioGroup)this.findViewById(R.id.locateRadioGroup);
		if (Main.locateMode == LocationClientOption.GpsFirst) locateRadioGroup.check(R.id.gps_first);
		else locateRadioGroup.check(R.id.network_first);
        locateButtonListener = new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.gps_first){
					boolean isGPSEnabled = Main.locationManager
							.isProviderEnabled(Main.locationManager.GPS_PROVIDER);
					if (isGPSEnabled)
						Main.locateMode = LocationClientOption.GpsFirst;
					else {
						AlertMaker alertMaker = new AlertMaker(Settings.this, Main.mapMaker);
						alertMaker.showSettingsAlert();
						isGPSEnabled = Main.locationManager
								.isProviderEnabled(Main.locationManager.GPS_PROVIDER);
						if (! isGPSEnabled) group.check(R.id.network_first);
					}
				}
				if (checkedId == R.id.network_first){
					Main.locateMode = LocationClientOption.NetWorkFirst;
				}
			}
		};
		locateRadioGroup.setOnCheckedChangeListener(locateButtonListener);
		
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
		text1.setText(currentRate + "s一次更新");
		SeekBar bar1 = (SeekBar) Settings.this.findViewById(R.id.settings_GPSrefreshrate_bar);
		bar1.setProgress(currentRate);
		bar1.setOnSeekBarChangeListener(new mySeekBarListener());
	}
	
	private void setColorMode(int colorMode) {
		Main.colorMode = colorMode;
		Log.d("color", "switch to " + colorMode);
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
