package com.example.piztor;

import java.io.PrintStream;
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
	EditText username, password, ip, port;
	Login login;
	Controller c;
	boolean flag = false;
	public final static String SER_KEY = "CONTROL";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		login = new Login(this);
		c = new Controller();
		b = (Button) findViewById(R.id.login);
		
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(flag==false){
				 Vector<Object> r = new Vector<Object>();
				 r.add(0);
				 r.add(username.getText().toString());
				 r.add(password.getText().toString());
				 Transam t = new Transam(ip.getText().toString(), Integer
				 .parseInt(port.getText().toString()), new Myrequest(r),
				 login);
				 new Thread(t).run();
				 flag = true;
				}
				else{
					 Vector<Object> r = new Vector<Object>();
					 r.add(2);
					 r.add(login.tk);
					 double lot = 123.456;
					 double lat = 654.321;
					 r.add(lot);
					 r.add(lat);
					 Transam t = new Transam(ip.getText().toString(), Integer
					 .parseInt(port.getText().toString()), new Myrequest(r),
					 c);
					 new Thread(t).run();
				}
			}
		});
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		ip = (EditText) findViewById(R.id.ip);
		port = (EditText) findViewById(R.id.port);
		cout.println("onCreate!");
	}

	void start() {
		Intent i = new Intent();
		i.setClass(MainActivity.this, Running.class);
		startActivity(i);
	}

	@Override
	protected void onStart() {
		super.onStart();
		ip.setText("192.168.1.101");
		port.setText("9990");
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
