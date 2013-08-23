package com.example.piztor;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;

public class Tracker implements Runnable {

	private static final long TIME_DELTA = 1000 * 10; // 10 seconds	
	public Timer timer;
	private final Context mContext;
	Controller controller;

	public Tracker(Controller newController, Context context) {
		timer = new Timer();
		mContext = context;
		controller = newController;
	}

	public void run() {
		GPSTask t = new GPSTask();
		t.run();
		//timer.s
//		timer.schedule(new GPSTask(), 0, TIME_DELTA);
	}

	class GPSTask extends TimerTask {
		@Override
		public void run() {
			GPSTracker tracker;
			tracker = new GPSTracker(mContext);
			System.out.println("can get location?");
			if(tracker.canGetLocation()) {
				double latitude = tracker.getLatitude();
				double longitude = tracker.getLongitude();
				System.out.println("yes!");
				controller.recieveLocation(latitude, longitude);
			}
		}
	}
}
