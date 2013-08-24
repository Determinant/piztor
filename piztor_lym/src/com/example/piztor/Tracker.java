package com.example.piztor;

import android.content.Context;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.os.SystemClock;


public class Tracker implements Runnable {

	private static final long TIME_DELTA = 1000 * 1; // 1 second
	public Timer timer;
	private final Context mContext;
	Controller controller;
	GPSTracker myTracker;
	Handler mHandler;
	Message message;

	public Tracker(Controller newController, Context context, Handler yHandler) {
		timer = new Timer();
		mContext = context;
		controller = newController;
		myTracker = new GPSTracker(mContext);
		mHandler = yHandler;
	}

	public void run() {
		GPSTask myTask = new GPSTask();
		timer.schedule(myTask, 0, TIME_DELTA);
	}

	class GPSTask extends TimerTask {
		@Override
		public void run() {
			message = new Message();
			message.what = 0;
			myTracker.getLocation();
			Log.d("Location", "Fetching location.....");
			if (myTracker.canGetLocation()) {
				double latitude = myTracker.getLatitude();
				double longitude = myTracker.getLongitude();	
				
				Log.d("TTTTTTTTTTTTTTTTTTTTTTTime","TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTime");
				System.out.println("GPSTIME" + myTracker.location.getTime());
				System.out.println("SYSTIME" + SystemClock.elapsedRealtime());
				
				if(myTracker.isGPSFix()) {
					message.what = 1;
				} else {
					message.what = 2;
				}
				mHandler.sendMessage(message);
			} else {
				message.what = 0;
				mHandler.sendMessage(message);
			}
		}
	}
}