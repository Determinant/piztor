package com.example.timerdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TimerDemo extends Activity {
	private TextView textTimer;
	private Button startButton;
	long nowTime = 0L;
	long startTime = 0L;
	long endTime = 0L;
	long timeLeft;
	private Handler myHandler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer_demo);

		textTimer = (TextView) findViewById(R.id.textTimer);

		startButton = (Button) findViewById(R.id.btnStart);
		startButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				startTime = SystemClock.uptimeMillis();
				endTime = startTime + 5 * 60 * 1000;
				myHandler.postDelayed(updateTimerMethod, 0);
			}
		});
		
	}
	
	private Runnable updateTimerMethod = new Runnable() {
		
		public void run() {
			nowTime = SystemClock.uptimeMillis();
			timeLeft = endTime - nowTime;
			long seconds = timeLeft / 1000;
			long minutes = seconds / 60;
			seconds = seconds % 60;
			long milliseconds = timeLeft % 1000;
			textTimer.setText("" + minutes + ":"
					+ String.format("%02d", seconds) + ":"
					+ String.format("%03d", milliseconds));
			myHandler.postDelayed(this, 0);
		}
	};

}
