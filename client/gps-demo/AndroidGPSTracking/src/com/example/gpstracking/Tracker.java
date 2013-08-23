package com.example.gpstracking;

import java.util.Timer;
import java.util.TimerTask;

public class Tracker implements Runnable {

	private static final long TIME_DELTA = 1000 * 60 * 5;	

	Controller controller;

	public Tracker(Controller newController) {
		controller = newController;
	}

	public void run() {
		public Timer timer;
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
