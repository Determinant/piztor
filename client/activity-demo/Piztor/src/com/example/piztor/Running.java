package com.example.piztor;

import java.io.PrintStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.wifi.WifiConfiguration.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;

public class Running extends Activity {
	PrintStream cout = System.out;
	// final String ip = "192.168.1.101";
	// final int port = 9990;
	MyView v;
	Bitmap b;
	Canvas c;
	Controller controller;
	Tracker tr;
	Thread gps;
	Handler handle = new Handler() {
		public void handleMessage(Message msg) {
			flushLocation();
			// v.invalidate();
			// }
			// break;
			// default:{
			v.cnt = msg.what;
			v.invalidate();
			// }
			//
			// }
			super.handleMessage(msg);
		}
	};
	
	Handler bySocket = new Handler() {
		public void handleMessage(Message msg) {
			Myrespond r = (Myrespond)msg.obj;
			if (r.wrong != null) {
				System.out.println(r.wrong);
			} else {
				System.out.println(r.contain.size());
				for (int i = 0; i < r.contain.size(); i++) {
					System.out.println(r.contain.get(i).toString());
				}
				if ((Integer) r.contain.firstElement() == 3) {
					// int n = (Integer) r.contain.get(1);
					// if (n > 0) {
					Rmsg t1 = (Rmsg) r.contain.get(2);
					Rmsg t2 = (Rmsg) r.contain.get(3);
					v.changMyLocation(t1.lot, t1.lat);
					v.changHerLocation(t2.lot, t2.lat);

					// }
				}
				System.out.println("yeal!");
			}
		}
	};
	
	
	void flushLocation() {
		if (tr == null) {
			System.out.println("tr is null");
		} else if (tr.myTracker == null) {
			System.out.println("myTracker is null");
		}
		double x = tr.myTracker.location.getLongitude();
		double y = tr.myTracker.location.getLatitude();
		//v.changMyLocation(x, y);
		new Thread(new Transam(UserStatus.ip, UserStatus.port,
				Myrequest.updateLocation(UserStatus.token, y, x), bySocket))
				.run();
		new Thread(new Transam(UserStatus.ip, UserStatus.port,
				Myrequest.requestLocation(UserStatus.token, 1), bySocket))
				.run();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		b = Bitmap.createBitmap(720, 1280, Bitmap.Config.ARGB_8888);
		c = new Canvas(b);
		controller = new Controller();
		controller.setRun(this);
		setContentView(R.layout.activity_running);
		cout.println("running is created!!!");
	}

	@Override
	protected void onStart() {
		super.onStart();
		v = (MyView) findViewById(R.id.view);
		v.setup(c, b, -1, -1);
		tr = new Tracker(controller, Running.this, handle);
		// runOnUiThread(tr);
		gps = new Thread(tr);
		gps.run();
	}

	@Override
	protected void onStop() {
		super.onStop();
		gps.interrupt();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.running, menu);
		return true;
	}

}
