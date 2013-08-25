package com.macaroon.piztor;

import android.content.Context;
import java.util.Timer;
import java.util.TimerTask;

import android.location.Location;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.os.SystemClock;


public class Tracker implements Runnable {

	private static final long TIME_DELTA = 1000 * 3; // 3 second
	public Timer timer;
	private final Context mContext;
	GPSTracker myTracker;
	Handler mHandler;
	Message message;

	public Tracker(Context context, Handler yHandler) {
		timer = new Timer();
		mContext = context;
		myTracker = new GPSTracker(mContext);
		mHandler = yHandler;
	}
	
	void setHandler(Handler hand) {
		mHandler = hand;
	}
	

	public void run() {
		GPSTask myTask = new GPSTask();
		timer.schedule(myTask, 0, TIME_DELTA);
	}	
	
	class GPSTask extends TimerTask {
		@Override
		public void run() {
			Location location = myTracker.getLocation();
			Log.d("Location", "Fetching location.....");
			if (myTracker.canGetLocation()) {
				
//				Log.d("TTTTTTTTTTTTTTTTTTTTTTTime","TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTime");
//				System.out.println("GPSTIME" + myTracker.location.getTime());
//				System.out.println("SYSTIME" + SystemClock.elapsedRealtime());
				
				message = new Message();
				message.what = 0;
				message.obj = location;
				if(myTracker.isGPSFix()) {
					message.what = 1;
				} else {
					message.what = 2;
				}
				mHandler.sendMessage(message);
			} else {
				message = new Message();
				message.what = 0;
				mHandler.sendMessage(message);
			}
		}
	}
}
