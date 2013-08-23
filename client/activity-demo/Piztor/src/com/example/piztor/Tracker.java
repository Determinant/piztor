package com.example.piztor;

import java.util.Timer;
import java.util.TimerTask;

public class Tracker implements Runnable {

	private static final long TIME_DELTA = 1000 * 60 * 5;	
	public final Context mContext;
	
	Controller controller;

	public Tracker(Controller newController) {
		controller = newController;
	}

	public Timer timer;
	public void run() {
		TimerTask task = new GPSTask();	
		timer.schedule(new GPSTask(), 0, TIME_DELTA);
	}

	class GPSTask extends TimerTask {
		public void run() {
			GPSTracker tracker;
			tracker = new GPSTracker(Tracker.this);

			double latitude = tracker.getLatitude();
			double longitude = tracker.getLongitude();

			controller.recieveLocation(latitude, longitude);
		}
	}
}
