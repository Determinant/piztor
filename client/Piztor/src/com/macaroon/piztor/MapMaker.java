package com.macaroon.piztor;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import android.util.Log;
import android.util.AttributeSet;
import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;  
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;  
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.FrameLayout;  
import android.widget.Toast;  
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.provider.Settings;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.BMapManager;  
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MKMapTouchListener;
import com.baidu.mapapi.map.MKMapViewListener;  
import com.baidu.mapapi.map.MapController;  
import com.baidu.mapapi.map.MapPoi;  
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.platform.comapi.basestruct.GeoPoint; 

public class MapMaker extends Activity {
	
	// MapView controlling component
	private MapView mMapView = null;
	private MapController mMapController = null;
	
	// Default center
	private final static GeoPoint sjtuCenter = new GeoPoint((int)(31.032247 * 1E6), (int)(121.445937 * 1E6));

	// Map layers and items
	private MyOverlay mOverlay = null;
	private OverlayItem curItem = null;
	private LocationOverlay mLocationOverlay;
	private ArrayList<OverlayItem> mItems = null;
	private MapInfo preMapInfo = null;

	// marker layer
	private MyOverlay markerOverlay;
	private OverlayItem nowMarker = null;

	// Popup component
	private PopupOverlay popLay = null;
	private TextView popupText = null;
	private View viewCache = null;
	private View popupInfo = null;

	//misc
	private Context context;
	private LocationManager locationManager = null;
	boolean isGPSEnabled;

	/**
	 * Constructor
	 */
	public MapMaker(MapView mapView, Context cc) {
		
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
		
			OverlayItem item = getItem(index);
			//TODO
			/////////////////////////////////////////////////////////
			popupText.setText("^_^");
			Bitmap bitmap = BMapUtil.getBitmapFromView(popupInfo);
			popLay.showPopup(bitmap, item.getPoint(),32);

			return true;
		}

		@Override
		public boolean onTap(GeoPoint pt, MapView mapView) {
		
			if(popLay != null) {
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
		
		//TODO
		/////////////////////////////////////////////////////////////////
		mOverlay = new MyOverlay(context.getResources().getDrawable(R.drawable.circle_red), mMapView);
		markerOverlay = new MyOverlay(context.getResources().getDrawable(R.drawable.marker1), mMapView);
	}
	
	/**
	 * Initialize popup
	 */
	public void InitPopup() {
	
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		viewCache = inflater.inflate(R.layout.custom_text_view, null);
		popupInfo = (View)viewCache.findViewById(R.id.popinfo);
		popupText = (TextView)viewCache.findViewById(R.id.textcache);

		PopupClickListener popListener = new PopupClickListener() {
		
			@Override
			public void onClickedPopup(int index) {
				// when the popup is clicked
				if (index == 0) {
					// do nothing
				}
				if (index == 1) {
					// this is a marker and remove it
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
		//InitTouchListenr();
	}

	/**
	 * Update location layer when new location is received
	 */
	public void UpdateLocationOverlay(LocationData locationData, boolean hasAnimation) {
	
		mLocationOverlay.setData(locationData);
		mMapView.refresh();

		if (hasAnimation) {
			mMapController.animateTo(new GeoPoint((int)(locationData.latitude * 1E6)
						, (int)(locationData.longitude * 1E6)));
		}
	}

	/**
	 * Update to draw other users
	 */
	public void UpdateMap(MapInfo mapInfo) {
	
		if (mapInfo != null) {
			preMapInfo = mapInfo;
			if (mOverlay != null && mOverlay.getAllItem().size() != 0) {
				mOverlay.removeAll();
			}
			mOverlay = new MyOverlay(context.getResources().getDrawable(R.drawable.circle_red), mMapView);
			GeoPoint p;
			Vector<UserInfo> allUsers = mapInfo.getVector();
			boolean flag = false;
			for (int i =1; i < allUsers.size(); i++) {
				// it's me!
				if (allUsers.get(i).uid == Infomation.myInfo.uid) continue;
				p = new GeoPoint((int)(allUsers.get(i).getLatitude() * 1E6), 
								(int)(allUsers.get(i).getLongitude()*1E6));
				curItem = new OverlayItem(p, "USERNAME HERE!!!!!", "");
				//TODO
				////////////////////////////////////////////////////////////
				if (flag == false){
					flag = true;
					curItem.setMarker(context.getResources().getDrawable(R.drawable.circle_green));
				} else {
					curItem.setMarker(context.getResources().getDrawable(R.drawable.circle_red));
				}
				mOverlay.addItem(curItem);
			}
			mItems = new ArrayList<OverlayItem>();
			mItems.addAll(mOverlay.getAllItem());
		}
		if (nowMarker != null) {
			Log.d("marker", "now marker is not null");
			mOverlay.addItem(nowMarker);
		}
		if (mMapView != null) {
			if (mMapView.getOverlays() != null) {
				mMapView.getOverlays().add(mOverlay);
				mMapView.refresh();
			}
		}
	}
	
	/**
	 * Update marker
	 */ 
	public void UpdateMarker() {
		
		mOverlay.addItem(nowMarker);
		if (mMapView != null) {
			mMapView.getOverlays().add(mOverlay);
			mMapView.refresh();
		}
	}
	
	/**
	 * Draw a marker
	 */
	public void DrawMarker(GeoPoint markerPoint) {
	
		nowMarker = new OverlayItem(markerPoint, "THIS IS A MARKER", "");
		nowMarker.setMarker(context.getResources().getDrawable(R.drawable.marker_red));
		Log.d("marker", "new marker created");		
		UpdateMap(preMapInfo);
		mMapController.animateTo(markerPoint);
	}

	/**
	 * Remove all other users
	 */
	public void clearOverlay(View view) {
		
		if(mOverlay != null && mOverlay.getAllItem().size() != 0) {
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
