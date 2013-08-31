package com.macaroon.piztor;

import java.util.Calendar;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapTouchListener;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class Main extends PiztorAct {
	final static int CheckinButtonPress = 1;
	final static int FocuseButtonPress = 3;
	final static int SuccessFetch = 4;
	final static int FailedFetch = 5;
	final static int mapViewtouched = 7;

	MapMaker mapMaker = null;
	MapView mMapView;
	AlertMaker alertMaker;
	private Calendar calendar;
	GeoPoint markerPoint = null;
	private MKMapTouchListener mapTouchListener;
	private final int checkinRadius = 10;

	/**
	 * Locating component
	 */
	LocationManager locationManager;
	boolean isGPSEnabled;
	LocationClient mLocClient;
	LocationData locData = null;
	public MyLocationListener myListener = new MyLocationListener();
	boolean isFirstLocation = true;
	public static int GPSrefreshrate = 5;

	ImageButton btnCheckin, btnFetch, btnFocus, btnSettings;
	MapInfo mapInfo;
	
	Transam transam;
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			switch (m.what) {
			case Res.Login:// 上传自己信息成功or失败
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
				System.out.println("now has info number : " + mapInfo.allUsers.size());
				flushMap();
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
				flushMap();
				break;
			case -1:
				actMgr.trigger(AppMgr.logout);
			default:
				break;
			}
		}

		void upMapInfo(Vector<RLocation> l) {
			System.out.println("hahaha" + "        " + l.size());
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
			flushMap();
		}
	};

	String cause(int t) {
		switch (t) {
		case CheckinButtonPress:
			return "Checkin Button Press";
		case FocuseButtonPress:
			return "Focuse Button Press";
		case SuccessFetch:
			return "Success Fetch";
		case FailedFetch:
			return "Failed Fetch";
		default:
			return "Fuck!!!";
		}
	}

	// TODO flush map view
	void flushMap() {
		if (mapMaker != null)
			mapMaker.UpdateMap(AppMgr.mapInfo);
		else
			Log.d("exception", "!!!");
	}

	void receiveMessage(String msg) {
		System.out.println("receiveed push message!!!!!");
		System.out.println(msg);
		Toast toast = Toast.makeText(getApplicationContext(), msg,
				Toast.LENGTH_LONG);
		toast.show();
	}

	public class MyLocationListener implements BDLocationListener {
		int cnt = 0;
		GeoPoint lastPoint = null;
		@Override
		public void onReceiveLocation(BDLocation location) {
			Log.d("GPS", "Gotten");
			cnt++;
			if (location == null) {
				return;
			}
			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();
			locData.accuracy = location.getRadius();
			locData.direction = location.getDerect();
			
			GeoPoint point = new GeoPoint((int)(locData.latitude * 1e6), (int)(locData.longitude * 1e6));
			if (lastPoint == null || cnt > 5 || DistanceUtil.getDistance(point, lastPoint) > 10 ) {
				if (Infomation.token != null) {
					Infomation.myInfo.setLocation(locData.latitude,
							locData.longitude);
					AppMgr.transam.send(new ReqUpdate(Infomation.token,
							Infomation.username, locData.latitude,
							locData.longitude, System.currentTimeMillis(), 2000));
					lastPoint = point;
					cnt = 0;
				}
			}
			boolean hasAnimation = false;
			if (isFirstLocation) {
				hasAnimation = true;
				isFirstLocation = false;
			}
			int TMP = location.getLocType();
			if (TMP == 61) {
				Toast toast = Toast.makeText(Main.this, "Piztor : Update from GPS result (" + GPSrefreshrate + "s)", 2000);
				toast.setGravity(Gravity.TOP, 0, 80);
				toast.show();
			}
			if (TMP == 161) {
				Toast toast = Toast.makeText(Main.this, "Piztor : Update from Network (" + GPSrefreshrate + "s)", 2000);
				toast.setGravity(Gravity.TOP, 0, 80);
				toast.show();
			}
			if (TMP == 65) {
				Toast toast = Toast.makeText(Main.this, "Piztor : Update from Cache (" + GPSrefreshrate + "s)", 2000);
				toast.setGravity(Gravity.TOP, 0, 80);
				toast.show();
			}
			mapMaker.UpdateLocationOverlay(locData, hasAnimation);
			
			LocationClientOption option = new LocationClientOption();
			option.setOpenGps(true);
			option.setCoorType("bd09ll");
			option.setScanSpan(GPSrefreshrate * 1000);
			mLocClient.setLocOption(option);
		}

		@Override
		public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null) {
				return;
			}
		}
	}

	class StartStatus extends ActStatus {

		@Override
		void enter(int e) {
			System.out.println("enter start status!!!!");
			if (e == ActMgr.Create) {
				System.out.println(Infomation.token + "  " + Infomation.username + "   " + Infomation.myInfo.uid);
			}
			if (e == SuccessFetch)
				flushMap();
		}

		@Override
		void leave(int e) {
			System.out.println("leave start status!!!! because" + cause(e));
		}

	}

	void requestUserInfo() {
		for (RGroup i : Infomation.sublist) {
			ReqUserInfo r = new ReqUserInfo(Infomation.token,
					Infomation.username, i, System.currentTimeMillis(), 2000);
			transam.send(r);
		}
		System.out.println("get others infomation!!!");
	}

	void focusOn() {
		mapMaker.mMapController.animateTo(Infomation.myInfo.location);
	}

	public void InitTouchListenr() {

		mapTouchListener = new MKMapTouchListener() {

			@Override
			public void onMapLongClick(GeoPoint arg0) {
				closeBoard(Main.this);
				alertMaker.showMarkerAlert(arg0);
			}

			@Override
			public void onMapDoubleClick(GeoPoint arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMapClick(GeoPoint arg0) {
				// TODO Auto-generated method stub

			}
		};
		mMapView.regMapTouchListner(mapTouchListener);
	}

	public void markerCheckin() {
		if (mapMaker.getMakerLocation() == null) {
			Toast toast = Toast.makeText(Main.this, "No marker now!", 2000);
			toast.setGravity(Gravity.TOP, 0, 80);
			toast.show();
			return;
		}
		GeoPoint curPoint = new GeoPoint((int)(locData.latitude * 1E6), (int)(locData.longitude * 1E6));
		double disFromMarker = DistanceUtil.getDistance(curPoint, mapMaker.getMakerLocation());
		if (disFromMarker < locData.accuracy) {
			mapMaker.removeMarker();
			Toast toast = Toast.makeText(Main.this, "Marker checked!", 2000);
			toast.setGravity(Gravity.TOP, 0, 80);
			toast.show();
		} else {
			Toast toast = Toast.makeText(Main.this, "Please get closer to the marker!", 2000);
			toast.setGravity(Gravity.TOP, 0, 80);
			toast.show();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		id = "Main";
		super.onCreate(savedInstanceState);

		locationManager = (LocationManager) this
				.getSystemService(LOCATION_SERVICE);
		isGPSEnabled = locationManager
				.isProviderEnabled(locationManager.GPS_PROVIDER);
		transam = AppMgr.transam;
		mapInfo = AppMgr.mapInfo;
		ActStatus[] r = new ActStatus[1];
		ActStatus startStatus = r[0] = new StartStatus();
		if (transam == null)
			Log.d(LogInfo.exception, "transam = null");
		transam.setHandler(handler);
		actMgr = new ActMgr(this, startStatus, r);
		setContentView(R.layout.activity_main);

		mMapView = (MapView) findViewById(R.id.bmapView);
		mapMaker = new MapMaker(mMapView, getApplicationContext());
		alertMaker = new AlertMaker(Main.this, mapMaker);
		if (isGPSEnabled == false) alertMaker.showSettingsAlert();
		mapMaker.clearOverlay(mMapView);
		mapMaker.InitMap();
		InitTouchListenr();
		mLocClient = new LocationClient(this);
		mLocClient.setAK(AppMgr.strKey);
		locData = new LocationData();
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setCoorType("bd09ll");
		option.setScanSpan(GPSrefreshrate * 1000);
		mLocClient.setLocOption(option);
		mLocClient.start();
		mapMaker.UpdateLocationOverlay(locData, false);
	}

	public static void closeBoard(Context cc) {
		InputMethodManager imm = (InputMethodManager) cc
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive())
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
					InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	protected void onStart() {
		super.onStart();
		btnFetch = (ImageButton) findViewById(R.id.footbar_btn_fetch);
		btnFocus = (ImageButton) findViewById(R.id.footbar_btn_focus);
		btnCheckin = (ImageButton) findViewById(R.id.footbar_btn_checkin);
		btnSettings = (ImageButton) findViewById(R.id.footbar_btn_settings);
		
		btnCheckin.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				markerCheckin();
			}
		});
		
		btnFetch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

			}
		});

		btnFocus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				focusOn();
			}
		});

		btnSettings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				actMgr.trigger(AppMgr.toSettings);
			}
		});

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		transam.setHandler(handler);
	}

	@Override
	protected void onResume() {
		isFirstLocation = true;
		requestUserInfo();
		mapMaker.onResume();
		flushMap();
		super.onResume();
	}

	@Override
	protected void onPause() {
		mapMaker.onPause();
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (mLocClient != null) {
			mLocClient.stop();
		}
		mapMaker.onDestroy();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AppMgr.exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mMapView.onRestoreInstanceState(savedInstanceState);
	}

}
