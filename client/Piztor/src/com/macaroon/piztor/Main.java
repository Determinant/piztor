package com.macaroon.piztor;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Vector;

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
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapTouchListener;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class Main extends PiztorAct {
	final static int CheckinButtonPress = 1;
	final static int FocuseButtonPress = 3;
	final static int SuccessFetch = 4;
	final static int FailedFetch = 5;
	final static int mapViewtouched = 7;

	static MapMaker mapMaker = null;
	MapView mMapView;
	AlertMaker alertMaker;
	GeoPoint markerPoint = null;
	private MKMapTouchListener mapTouchListener;
	public static int colorMode = 1;
	public static int show_by_team = 1;
	public static int show_by_sex = 2;
	public static int locateMode = LocationClientOption.GpsFirst;
	
	/**
	 * Locating component
	 */
	static LocationManager locationManager;
	boolean isGPSEnabled;
	LocationClient mLocClient;
	LocationData locData = null;
	public MyLocationListener myListener = new MyLocationListener();
	boolean isFirstLocation = true;
	public static int GPSrefreshrate = 20;
	private final double checkinRadius = 10.0;

	ImageButton btnCheckin, btnFetch, btnFocus, btnSettings;

	static class ReCall extends Handler {
		WeakReference<Main> outerClass;
		String id = "main";

		ReCall(Main activity) {
			outerClass = new WeakReference<Main>(activity);
		}

		@Override
		public void handleMessage(Message m) {
			Main out = outerClass.get();
			if (out == null) {
				System.out.println("act被回收了");
			}
			Log.d("marker", "what is " + m.what);
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
				out.flushMap();
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
				out.flushMap();
				break;
			case Res.PushMarker:
				ResPushMarker pushMarker = (ResPushMarker) m.obj;
				MarkerInfo markerInfo = new MarkerInfo();
				markerInfo.level = pushMarker.level;
				markerInfo.markerPoint = new GeoPoint(
						(int) (pushMarker.latitude * 1e6),
						(int) (pushMarker.longitude * 1e6));
				markerInfo.markerTimestamp = pushMarker.deadline;
				Log.d("marker", "Marker received!   " + pushMarker.deadline);
				out.mapMaker.receiveMarker(markerInfo);
				break;
			case -1:
				EException e = (EException) m.obj;
				if (e.Etype == EException.EPushFailedException)
					out.receiveMessage("网络不稳定～");
				else out.actMgr.trigger(AppMgr.logout);
			default:
				break;
			}
		}
	}

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
		flushMap();
	}

	Handler handler = null;

	String cause(int t) {
		switch (t) {
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
			mapMaker.UpdateMap(mapInfo);
		else
			Log.d("exception", "!!!");
	}

	void receiveMessage(String msg) {
		Toast toast = Toast.makeText(getApplicationContext(), msg,
				Toast.LENGTH_LONG);
		toast.show();
	}

	public void updateMyLocation() {
		if (app.token != null) {
			app.mapInfo.myInfo.setLocation(locData.latitude, locData.longitude);
			transam.send(new ReqUpdate(app.token, app.username,
					locData.latitude, locData.longitude, 
					System.currentTimeMillis(), 2000));
		}
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

			GeoPoint point = new GeoPoint((int) (locData.latitude * 1e6),
					(int) (locData.longitude * 1e6));
			if (lastPoint == null || cnt > 5
					|| DistanceUtil.getDistance(point, lastPoint) > 10) {
				if (app.token != null) {
					updateMyLocation();
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
				Toast toast = Toast.makeText(Main.this,
						"Piztor : 由GPS更新 (刷新时间" + GPSrefreshrate
								+ "s)", 2000);
				toast.setGravity(Gravity.TOP, 0, 80);
				toast.show();
			}
			if (TMP == 161) {
				Toast toast = Toast.makeText(Main.this,
						"Piztor : 由网络更新 (刷新时间" + GPSrefreshrate
								+ "s)", 2000);
				toast.setGravity(Gravity.TOP, 0, 80);
				toast.show();
			}
			if (TMP == 65) {
				Toast toast = Toast.makeText(Main.this,
						"Piztor : 由缓存更新 (刷新时间" + GPSrefreshrate + "s)",
						2000);
				toast.setGravity(Gravity.TOP, 0, 80);
				toast.show();
			}
			mapMaker.UpdateLocationOverlay(locData, hasAnimation);

			LocationClientOption option = new LocationClientOption();
			option.setOpenGps(true);
			option.setCoorType("bd09ll");
			option.setPriority(locateMode);
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
				System.out.println(app.token + "  " + app.username + "   "
						+ app.mapInfo.myInfo.uid);
			}
			if (e == SuccessFetch)
			{ }
		}

		@Override
		void leave(int e) {
			System.out.println("leave start status!!!! because" + cause(e));
		}

	}

	void requestUserInfo() {
		mapInfo.clear();
		System.out.println("cleared!!!!");
		for (RGroup i : app.sublist) {
			ReqUserInfo r = new ReqUserInfo(app.token, app.username, i,
					System.currentTimeMillis(), 2000);
			transam.send(r);
		}
		System.out.println("get others infomation!!!");
	}

	void focusOn() {
		mapMaker.mMapController.animateTo(app.mapInfo.myInfo.location);
	}

	public void InitTouchListenr() {

		mapTouchListener = new MKMapTouchListener() {

			@Override
			public void onMapLongClick(GeoPoint arg0) {
				closeBoard(Main.this);
				if (app.mapInfo.myInfo.level != 0) {
					alertMaker.showMarkerAlert(arg0);
				closeBoard(Main.this);
				}
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
		Log.d("checkin", "ok!!!");
		if (mapMaker.getMakerLocation() == null) {
			Toast toast = Toast.makeText(Main.this, "暂无路标", 2000);
			toast.setGravity(Gravity.TOP, 0, 80);
			toast.show();
			return;
		}
		mLocClient.requestLocation();
		GeoPoint curPoint = new GeoPoint((int) (locData.latitude * 1E6),
				(int) (locData.longitude * 1E6));
		double disFromMarker = DistanceUtil.getDistance(curPoint,
				mapMaker.getMakerLocation());
		if (disFromMarker < Math.max(locData.accuracy, (float)checkinRadius) ) {
			alertMaker.showCheckinAlter();
		} else {
			Toast toast = Toast.makeText(Main.this,
					String.format("请靠近路标,现在距离%.2f米",disFromMarker), 2000);
			toast.setGravity(Gravity.TOP, 0, 80);
			toast.show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		id = "Main";
		super.onCreate(savedInstanceState);
		handler = new ReCall(this);
		locationManager = (LocationManager) this
				.getSystemService(LOCATION_SERVICE);
		isGPSEnabled = locationManager
				.isProviderEnabled(locationManager.GPS_PROVIDER);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	    requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_main);
		setProgressBarIndeterminateVisibility(true); 
		setProgressBarVisibility(true);
		
		app.mBMapManager.start();

		mMapView = (MapView) findViewById(R.id.bmapView);
		mapMaker = new MapMaker(mMapView, Main.this, app);
		mapMaker.InitMap();
		alertMaker = new AlertMaker(Main.this, mapMaker);
		if (isGPSEnabled == false)
			alertMaker.showSettingsAlert();
		mapMaker.clearOverlay(mMapView);
		InitTouchListenr();
		mLocClient = new LocationClient(this);
		mLocClient.setAK(myApp.getStrkey());
		locData = new LocationData();
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setPriority(locateMode);
		option.setCoorType("bd09ll");
		option.setScanSpan(GPSrefreshrate * 1000);
		mLocClient.setLocOption(option);
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
		
		btnFetch.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Toast.makeText(Main.this, "正在定位...", Toast.LENGTH_LONG).show();
				mLocClient.requestLocation();
				updateMyLocation();
				focusOn();
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
		if (app.isExiting || app.isLogout)
			finish();
		ActStatus[] r = new ActStatus[1];
		ActStatus startStatus = r[0] = new StartStatus();
		actMgr = new ActMgr(appMgr, this, startStatus, r);
		mMapView.onResume();
		transam.setHandler(handler);
		isFirstLocation = true;
		mLocClient.start();
		if (app.token == null) {
			System.out.println("fuck!!");
		} else
			requestUserInfo();
		flushMap();
		super.onResume();
	}

	@Override
	protected void onPause() {
		// mapMaker.onPause();
		// mapMaker = null;
		mLocClient.stop();
		mMapView.onPause();
		// System.gc();
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
		// mapMaker.mOffline.destroy();
		mMapView.destroy();
		app.mBMapManager.stop();
		// while null?
		// mMapView.destroy();
		//
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			System.out.println("ready to exit!!!");
			//
			app.isExiting = true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
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
