package com.example.piztor;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class Tracker implements Runnable {

	private static final long TIME_DELTA = 1000 * 1; // 1 seconds
	public Timer timer;
	private final Context mContext;
	Controller controller;
	GPSTracker myTracker;
	Handler mHandler;
	Message message;
	int CNT = 0;

	public Tracker(Controller newController, Context context, Handler yHandler) {
		timer = new Timer();
		mContext = context;
		controller = newController;
		myTracker = new GPSTracker(mContext);
		mHandler = yHandler;
	}

	public void run() {
		GPSTask t = new GPSTask();
		timer.schedule(t, 0, TIME_DELTA);
	}

	class GPSTask extends TimerTask {
		@Override
		public void run() {
			++CNT;
			message = new Message();
			message.what = 0;
			//myTracker = new GPSTracker(mContext);
			myTracker.getLocation();
			System.out.println("can get location?");
			if (myTracker.canGetLocation()) {
				double latitude = myTracker.getLatitude();
				double longitude = myTracker.getLongitude();			
				// controller.recieveLocation(latitude, longitude);
				Message message = new Message();
				message.what = CNT;
				mHandler.sendMessage(message);
			} else {
				Message message = new Message();
				message.what = 0;
				mHandler.sendMessage(message);
			}
		}
	}
}