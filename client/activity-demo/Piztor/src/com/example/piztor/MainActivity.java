package com.example.piztor;

import java.io.PrintStream;
import java.net.Socket;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {
	PrintStream cout = System.out;
	Button b;
	EditText username, password;
	Login login;
	public final static String SER_KEY = "CONTROL";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		login = new Login(this);
		b = (Button) findViewById(R.id.login);

		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				cout.println(username.getText().toString());
				cout.println(password.getText().toString());
				Transam t = new Transam(UserStatus.ip, UserStatus.port,
						Myrequest.login(username.getText().toString(), password
								.getText().toString()), login);
				new Thread(t).run();
				// start();
			}
		});
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		cout.println("onCreate!");
	}

	void start() {
		Intent i = new Intent();
		i.setClass(this, Running.class);
		startActivity(i);
	}

	@Override
	protected void onStart() {
		super.onStart();
		username.setText("hello");
		password.setText("world");
		cout.println("onStart!");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		cout.println("onRestart!");
	}

	@Override
	protected void onResume() {
		super.onResume();
		// cout.println("onResume!");
		// v.drawString("!!!!!");
	}

	@Override
	protected void onPause() {
		super.onPause();
		cout.println("onPause!");
	}

	@Override
	protected void onStop() {
		super.onStop();
		cout.println("onStop!");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cout.println("onDestroy!");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
