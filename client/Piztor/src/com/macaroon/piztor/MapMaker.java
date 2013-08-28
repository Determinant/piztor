package com.macaroon.piztor;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import android.util.Log;
import android.util.AttributeSet;
import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.app.Activity;  
import android.content.Context;
import android.content.res.Configuration;  
import android.graphics.drawable.Drawable;
import android.widget.FrameLayout;  
import android.widget.Toast;  
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.BMapManager;  
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MKMapViewListener;  
import com.baidu.mapapi.map.MapController;  
import com.baidu.mapapi.map.MapPoi;  
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint; 

public class MapMaker extends Activity{

	private MapView mMapView = null;
	private MapController mMapController = null;
	private static GeoPoint sjtuCenter = new GeoPoint((int)(31.032247 * 1E6), (int)(121.445937 * 1E6));
	private MyOverlay mOverlay = null;
	private MapView.LayoutParams layoutParam = null;
	private OverlayItem curItem = null;
	private ArrayList<OverlayItem> mItems = null;
	private LocationOverlay mLocationOverlay;
	private Context context;

	public MapMaker(MapView mapView, Context c) {
		mMapView = mapView;
		mMapController = mMapView.getController();
		mMapController.setCenter(sjtuCenter);
		mMapController.setZoom(17);
		mMapController.setRotation(-22);
		mMapController.enableClick(true);
		context = c;
		mLocationOverlay = null;
		mOverlay = null;
	}

	public class LocationOverlay extends MyLocationOverlay {
		public LocationOverlay(MapView mapView) {
			super(mapView);
		}
	}

	public class MyOverlay extends ItemizedOverlay {
		public MyOverlay(Drawable defaultMarker, MapView mapView) {
			super(defaultMarker, mapView);
		}
	}

	public void UpdateLocationOverlay(LocationData locationData, boolean hasAnimation) {
		/**
		 * Update only location overlay
		 */
		mLocationOverlay.setData(locationData);
		mMapView.refresh();
		if (hasAnimation) {
			mMapController.animateTo(new GeoPoint((int)(locationData.latitude * 1E6), (int)(locationData.longitude * 1E6)));
		}
	}
	
	public void InitMap() {
		Log.d("GPS", "init");
		mLocationOverlay = new LocationOverlay(mMapView);
		//mLocationOverlay.setMarker(context.getResources().getDrawable(R.drawable.marker1));
		LocationData locationData = new LocationData();
		mLocationOverlay.setData(locationData);
		mMapView.getOverlays().add(mLocationOverlay);
		mLocationOverlay.enableCompass();
		mMapView.refresh();
		mOverlay = new MyOverlay(context.getResources().getDrawable(R.drawable.circle_red), mMapView);
	}
	
	public void UpdateMap(MapInfo mapInfo) {
		/**
		 * Update location of others
		 */
		if (mOverlay != null && mOverlay.getAllItem().size() != 0) {
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			clearOverlay(mMapView);
			System.out.println("+++++++++++++++++++++++------------------------------------------");
		}
		mOverlay = new MyOverlay(context.getResources().getDrawable(R.drawable.circle_red), mMapView);
		//mMapView.refresh();
		GeoPoint p;
		Vector<UserInfo> allUsers = mapInfo.getVector();
		System.out.println("SSSSSSSSSSSSSSSSize       "+allUsers.size());
		for(int i = 0; i < allUsers.size(); i++) {
			if (allUsers.get(i).uid == Infomation.myInfo.uid) continue;
			p = new GeoPoint((int)(allUsers.get(i).getLatitude() * 1E6)
					,(int)(allUsers.get(i).getLongitude() * 1E6));
			curItem = new OverlayItem(p, "^_^", "");
			curItem.setMarker(context.getResources().getDrawable(R.drawable.circle_red));
			mOverlay.addItem(curItem);
		}
		mItems = new ArrayList<OverlayItem>();
		mItems.addAll(mOverlay.getAllItem());
		if (mMapView != null) {
			if (mMapView.getOverlays() != null) {
				mMapView.getOverlays().add(mOverlay);
				mMapView.refresh();
			}
		}
	}


	public void clearOverlay(View view) {
		mOverlay.removeAll();
		mMapView.refresh();
	}

	public void resetOverlay(View view) {
		clearOverlay(null);
		mOverlay.addItem(mItems);
		mMapView.refresh();
	}
	
	@Override
	protected void onPause() {
		mMapView.onPause();
	}
	
	@Override 
	protected void onResume() {
		mMapView.onResume();
	}
	
	@Override
	protected void onDestroy() {
		mMapView.destroy();
	}
}

