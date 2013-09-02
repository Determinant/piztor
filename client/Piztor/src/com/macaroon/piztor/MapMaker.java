package com.macaroon.piztor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.DropBoxManager.Entry;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKOLUpdateElement;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class MapMaker {

	// MapView controlling component
	MapView mMapView = null;
	MapController mMapController = null;

	// Default center
	private final static GeoPoint sjtuCenter = new GeoPoint(
			(int) (31.032247 * 1E6), (int) (121.445937 * 1E6));

	// Map layers and items
	MyOverlay mOverlay = null;
	private OverlayItem curItem = null;
	private LocationOverlay mLocationOverlay;
	private ArrayList<OverlayItem> mItems = null;

	// hash from uid to overlay item
	private HashMap<Integer, OverlayItem> userToItem = null;
	private HashMap<OverlayItem, Integer> itemToUser = null;

	// marker layer
	/*
	 * OverlayItem nowMarker = null; static int nowMarkerHour; static int
	 * nowMarkerMinute; static long nowMarkerTimestamp; static int
	 * newMarkerHour; static int newMarkerMinute; static long
	 * newMarkerTimestamp; private int markerIndex; private int nowMarkerLevel;
	 */
	HashMap<Integer, OverlayItem> markerToItem;
	// from mid to item
	HashMap<OverlayItem, Integer> itemToMarker;
	// from item to mid

	// Popup component
	PopupOverlay popLay = null;
	private TextView popupText = null;
	private TextView leftText = null;
	private View viewCache = null;
	private View popupInfo = null;
	private View popupLeft = null;
	private View popupRight = null;

	// misc
	private Context context;
	private LocationManager locationManager = null;
	boolean isGPSEnabled;
	private int[] myIcons;
	private Drawable[] myBM;
	private final int iconNum = 9;

	myApp app;

	/**
	 * Constructor
	 */
	public MapMaker(MapView mapView, Context cc, myApp app) {
		this.app = app;
		mMapView = mapView;
		mMapController = mMapView.getController();

		mMapController.setCenter(sjtuCenter);
		mMapController.setZoom(16);
		mMapController.setRotation(-22);
		mMapController.enableClick(true);

		context = cc;
		mLocationOverlay = null;
		mOverlay = null;
	}

	/**
	 * Layer for my location
	 */
	public class LocationOverlay extends MyLocationOverlay {

		public LocationOverlay(MapView mapView) {
			super(mapView);
		}
	}

	/**
	 * Layer for items(other users)
	 */
	public class MyOverlay extends ItemizedOverlay {

		public MyOverlay(Drawable defaultMaker, MapView mapView) {
			super(defaultMaker, mapView);
		}

		@Override
		public boolean onTap(int index) {
			
			if (app.mapInfo.markerInfo != null
					&& itemToMarker.containsKey(mOverlay.getItem(index))) {
				Log.d("marker", "on tap" + index);
				OverlayItem item = getItem(index);
				MarkerInfo mInfo = app.mapInfo.getMarkerInfo(itemToMarker
						.get(item));
				leftText.setText("哈");
				popupText.setText("分数：" + mInfo.score);
				Bitmap bitmap = BMapUtil.getBitmapFromView(popupInfo);
				popLay.showPopup(bitmap, item.getPoint(), 32);
			} 
			if (itemToUser.containsKey(mOverlay.getItem(index))){
				OverlayItem item = getItem(index);
				UserInfo tmpInfo = app.mapInfo.getUserInfo(itemToUser
						.get(item));
				String itemInfo = tmpInfo.company + "连" + tmpInfo.section
						+ "班 " + tmpInfo.nickname;
				popupText.setText(itemInfo);
				Bitmap bitmap = BMapUtil.getBitmapFromView(popupInfo);
				popLay.showPopup(bitmap, item.getPoint(), 32);
			}
			return true;
		}

		@Override
		public boolean onTap(GeoPoint pt, MapView mapView) {

			if (popLay != null) {
				popLay.hidePop();
			}
			return false;
		}
	}

	/**
	 * Initialize location layer
	 */
	public void InitLocationOverlay() {

		mLocationOverlay = new LocationOverlay(mMapView);
		LocationData locationData = new LocationData();
		mLocationOverlay.setData(locationData);
		mMapView.getOverlays().add(mLocationOverlay);
		mLocationOverlay.enableCompass();
		mMapView.refresh();
	}

	/**
	 * Initialize other users layer
	 */
	public void InitMyOverLay() {

		// TODO
		// ///////////////////////////////////////////////////////////////
		userToItem = new HashMap<Integer, OverlayItem>();
		markerToItem = new HashMap<Integer, OverlayItem>();
		itemToMarker = new HashMap<OverlayItem, Integer>();
		itemToUser = new HashMap<OverlayItem, Integer>();
		
		mOverlay = new MyOverlay(context.getResources().getDrawable(
				R.drawable.circle_red), mMapView);
		mMapView.getOverlays().add(mOverlay);
	}

	/**
	 * Initialize popup
	 */
	public void InitPopup() {

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		viewCache = inflater.inflate(R.layout.custom_text_view, null);
		popupInfo = (View) viewCache.findViewById(R.id.popinfo);
		popupLeft = (View) viewCache.findViewById(R.id.popleft);
		popupRight = (View) viewCache.findViewById(R.id.popright);
		popupText = (TextView) viewCache.findViewById(R.id.textcache);
		leftText = (TextView) viewCache.findViewById(R.id.popleft);

		PopupClickListener popListener = new PopupClickListener() {

			@Override
			public void onClickedPopup(int index) {
				// when the popup is clicked
				if (index == 0) {
					// do nothing
				}
				if (index == 2) {
					// do nothing
				}
			}
		};

		popLay = new PopupOverlay(mMapView, popListener);
	}

	/**
	 * Initialize touch listener
	 */
	// moved to main

	/**
	 * Initialize map
	 */
	public void InitMap() {

		InitLocationOverlay();
		InitMyOverLay();
		InitPopup();
		myBM = new Drawable[20];
		initMyIcons();
		// InitTouchListenr();
	}

	public void initMyIcons() {
		myBM[0] = context.getResources().getDrawable(R.drawable.circle_blue);
		myBM[1] = context.getResources().getDrawable(R.drawable.circle_red);
		myBM[2] = context.getResources().getDrawable(R.drawable.circle_glass);
		myBM[3] = context.getResources().getDrawable(R.drawable.circle_yellow);
		myBM[4] = context.getResources().getDrawable(R.drawable.circle_wood);
		myBM[5] = context.getResources().getDrawable(R.drawable.circle_green);
		myBM[6] = context.getResources().getDrawable(R.drawable.circle_metal);
		myBM[7] = context.getResources().getDrawable(R.drawable.circle_paper);
		myBM[8] = context.getResources().getDrawable(R.drawable.circle_tan);
	}

	public Drawable getGroupIcon(UserInfo userInfo) {
		if (userInfo.section == app.mapInfo.myInfo.section)
			return myBM[0];
		else return myBM[1];
	}

	/**
	 * Update location layer when new location is received
	 */
	public void UpdateLocationOverlay(LocationData locationData, boolean hasAnimation) {
		mLocationOverlay.setData(locationData);
		mMapView.refresh();
		if (hasAnimation) {
			mMapController.animateTo(new GeoPoint(
					(int) (locationData.latitude * 1E6),
					(int) (locationData.longitude * 1E6)));
		}
	}

	boolean isInvalidLocation(GeoPoint point) {
		if (point == null)
			return false;
		if (point.getLatitudeE6() / 1E6 > 180
				|| point.getLatitudeE6() / 1E6 < -180
				|| point.getLongitudeE6() > 180
				|| point.getLongitudeE6() / 1E6 < -180)
			return false;
		return true;
	}

	/**
	 * Update to draw other users
	 */
	public void UpdateMap(MapInfo mapInfo) {

		if (mapInfo == null) {
			if (mOverlay != null && mOverlay.getAllItem().size() != 0) {
				mOverlay.removeAll();
			}
			return;
		}

		// first remove all old users that are not here now

		Vector<Integer> delList = new Vector<Integer>();

		for (java.util.Map.Entry<Integer, OverlayItem> i : userToItem.entrySet()) {
			if (mapInfo.getUserInfo(i.getKey()) == null) {
				delList.add(i.getKey());
			}
		}
		for (int i : delList) {
			mOverlay.removeItem(userToItem.get(i));
			itemToUser.remove(userToItem.get(i));
		}
		for (int i : delList) {
			userToItem.remove(i);
		}
		mMapView.refresh();

		delList = new Vector<Integer>();

		for (java.util.Map.Entry<Integer, OverlayItem> i : markerToItem.entrySet()) {
			if (mapInfo.getMarkerInfo(i.getKey()) == null) {
				delList.add(i.getKey());
			}
		}
		// i is mid
		Log.d("kram", "before del overlay " + mOverlay.getAllItem().size());
		for (int i : delList) {
			mOverlay.removeItem(markerToItem.get(i));
			itemToMarker.remove(markerToItem.get(i));
		}
		// i is mid
		for (int i : delList) {
			Log.d("kram", "remove id " + i);
			markerToItem.remove(i);
			
		}
		for (java.util.Map.Entry<Integer, OverlayItem> i : markerToItem.entrySet()) {
			Log.d("kram", "current marker " + i.getKey());
		}
		for (MarkerInfo i : mapInfo.markerInfo) {
			Log.d("kram", "current makerInfo " + i.markerId);
		}
		
		Log.d("kram", "after del overlay " + mOverlay.getAllItem().size());
		mMapView.refresh();
		
		// then update and add items
		
		Log.d("kram", "before update overlay " + mOverlay.getAllItem().size());
		int cnt = 0;
		for (MarkerInfo i : mapInfo.markerInfo) {
			if (markerToItem.containsKey(i.markerId) == false) {
				Log.d("kram", "add id " + i.markerId);
				cnt ++;
				curItem = new OverlayItem(i.markerPoint, "MARKERNAME_HERE",
						"MARKER_SNIPPET_HERE");
				Log.d("jingdu", "收到的坐标" + i.markerPoint.getLatitudeE6() / 1E6 + "  " + i.markerPoint.getLongitudeE6() / 1E6);
				Drawable d = context.getResources().getDrawable(R.drawable.marker_red);
				//d.mutate().setAlpha(10);
				curItem.setMarker(d);
				mOverlay.addItem(curItem);
				markerToItem.put(i.markerId, curItem);
				itemToMarker.put(curItem, i.markerId);
			}
		}
		Log.d("kram", "added " + cnt);
		Log.d("kram", "after update overlay " + mOverlay.getAllItem().size());

		for (UserInfo i : mapInfo.getVector()) {
			if (i.uid == app.mapInfo.myInfo.uid)
				continue;
			if (userToItem.containsKey(i.uid) == false) {
				if (isInvalidLocation(i.location)) {

				} else {
					curItem = new OverlayItem(i.location, "USERNAME_HERE",
							"USER_SNIPPET_HERE");
					// TODO getDrawable
					// /////////////////////////////
					curItem.setMarker(getGroupIcon(i));
					mOverlay.addItem(curItem);
					userToItem.put(i.uid, curItem);
					itemToUser.put(curItem, i.uid);
				}
			} else {
				if (isInvalidLocation(i.location)) {
					mOverlay.removeItem(userToItem.get(i.uid));
					itemToUser.remove(userToItem.get(i.uid));
					userToItem.remove(i.uid);
				} else {
					curItem = userToItem.get(i.uid);
					curItem.setGeoPoint(i.location);
					mOverlay.updateItem(curItem);
				}
			}
		}
		if (mMapView != null) {
			mMapView.refresh();
		}
	}

	public void receiveMarker(MarkerInfo markerInfo) {
		app.mapInfo.addMarkerInfo(markerInfo);
		UpdateMap(app.mapInfo);
	}

	/**
	 * Remove all other users
	 */
	public void clearOverlay(View view) {

		if (mOverlay != null && mOverlay.getAllItem().size() != 0) {
			mOverlay.removeAll();
			mMapView.refresh();
		}
	}

	/**
	 * Reset other users over lay
	 */
	public void resetOverlay(View view) {

		clearOverlay(null);
		mOverlay.addItem(mItems);
	}
}
