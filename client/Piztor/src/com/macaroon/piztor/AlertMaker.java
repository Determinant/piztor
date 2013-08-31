package com.macaroon.piztor;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.Inflater;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class AlertMaker {

	private Calendar calendar;
	private Context context;
	private GeoPoint markerPoint;
	private MapMaker mapMaker;
	private long timestamp;
	 int mHour;
	 int mMinute;
	
	public AlertMaker(Context cc, MapMaker mm) {
		context =cc;
		mapMaker = mm;
	}
		
	public static void closeBoard(Context cc) {
		InputMethodManager imm = (InputMethodManager) cc
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		  if (imm.isActive())
			  imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
		 InputMethodManager.HIDE_NOT_ALWAYS);
		 }

	public void showSettingsAlert() {
		
		closeBoard(context);
		AlertDialog.Builder gpsDialog = new AlertDialog.Builder(context);
		gpsDialog.setTitle("GPS设置");
		gpsDialog.setMessage("GPS未开启，是否前去打开？");
		gpsDialog.setPositiveButton("设置",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						context.startActivity(intent);
					}
				});
		gpsDialog.setNegativeButton("不使用GPS",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		gpsDialog.show();
	}

	public long toTimestamp(int hour, int minute) {
		calendar = Calendar.getInstance();
		int nhour = calendar.get(Calendar.HOUR_OF_DAY);
		int nminute = calendar.get(Calendar.MINUTE);
		int tmp = 0;
		if (hour > nhour) tmp = (hour - nhour) * 60 + minute - nminute;
		else if (minute > nminute) tmp = minute - nminute;
		timestamp = System.currentTimeMillis() + tmp * 60 * 1000;
		return timestamp;
	}

	//TODO
	public void updateMarkerTime(int hour, int minute) {
		Log.d("time", hour + "  " + minute);
		Log.d("time", "    " + toTimestamp(hour, minute));
		mapMaker.newMarkerHour = hour;
		mapMaker.newMarkerMinute = minute;
		mapMaker.newMarkerTimestamp = toTimestamp(hour, minute);
	}
	
	public void showMarkerAlert(GeoPoint point) {
		
		closeBoard(context);
		boolean flag = false;
		if (point == null) return;
		markerPoint = point;
		
		calendar = Calendar.getInstance();
		TimePickerDialog markerDialog = new TimePickerDialog(context
				, new TimePickerDialog.OnTimeSetListener() {
					boolean flag = false;
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						////// at least 2 minutes
						if ( !flag &&
							((hourOfDay >= calendar.get(Calendar.HOUR_OF_DAY) && minute >= calendar.get(Calendar.MINUTE))
							|| hourOfDay > calendar.get(Calendar.HOUR_OF_DAY))) {
							updateMarkerTime(hourOfDay, minute);
							mapMaker.DrawMarker(markerPoint);
							flag = true;
							Log.d("marker", "marker alert calls drawmarker");
							} else if (!flag) {
							Toast toast = Toast.makeText(context,
								"太早了!多给一点时间", Toast.LENGTH_LONG);
							toast.show();
							closeBoard(context);
							showMarkerAlert(markerPoint);
						}
					}
				}
				, calendar.get(Calendar.HOUR_OF_DAY)
				, calendar.get(Calendar.MINUTE), true);
				markerDialog.show();
	}

	public void showCheckinAlter() {
		closeBoard(context);
		final AlertDialog.Builder checkinDialog = new AlertDialog.Builder(context);
		LayoutInflater infaler = LayoutInflater.from(context);
		final LinearLayout layout = (LinearLayout)infaler.inflate(R.layout.checkindialog, null);
		checkinDialog.setView(layout);
		checkinDialog.setNeutralButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(layout.getWindowToken(), 0);
				dialog.cancel();
			}
		});
		final ProgressBar pbar = (ProgressBar)layout.findViewById(R.id.checkin_progress);
		final TextView checkinInfo = (TextView)layout.findViewById(R.id.checkin_info);
		checkinDialog.show();
		closeBoard(context);
		new CountDownTimer(5000, 1000) {

		     public void onTick(long millisUntilFinished) {
		    	 int ttt = (int)millisUntilFinished;
		         pbar.setProgress(ttt);
		     }

		     public void onFinish() {
		    	 //TODO
		    	 pbar.setVisibility(View.GONE);
		    	 mapMaker.removeMarker();
		    	 Toast toast = Toast.makeText(context, "已签到!", 2000);
		    	 toast.setGravity(Gravity.TOP, 0, 80);
		    	 toast.show();
		    	 checkinInfo.setText("成功!");
		     }
		  }.start();
	}

}
