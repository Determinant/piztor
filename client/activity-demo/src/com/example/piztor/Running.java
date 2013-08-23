package com.example.piztor;

import java.io.PrintStream;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.Menu;

public class Running extends Activity {
	PrintStream cout = System.out;
	MyView v;
	Bitmap b;
	Canvas c;
	Controller controller;
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
		v.setup(c, b, 31, 121);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.running, menu);
		return true;
	}

}
