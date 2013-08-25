package com.macaroon.piztor;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

public class Transam implements Runnable {
	
	public Timer timer;
	public Timer mtimer;
	public boolean running = false;
	public boolean flag = true;
	public int cnt = 4;
	Res res;
	Req req;
	public int p; // port
	public String i; // ip
	Thread thread;
	Handler core;
	Handler recall; // recall
	Queue<Req> reqtask; // request task

	Transam(String ip, int port, Handler Recall) {
		p = port;
		i = ip;
		recall = Recall;
		reqtask = new LinkedList<Req>();
	}

	public void send(Req r) {
		reqtask.offer(r);

	}
	
	public void setHandler(Handler Recall) {
		recall = Recall;
		reqtask.clear();
	}

	public void run() { // start the main timer
		// TimerTask tmain = new Timertk();
		// mtimer = new Timer();
		// mtimer.schedule(tmain, 100, 100); //check the queue for every 100
		// msec

		while (true) {
			if (running == false) {

				if (!reqtask.isEmpty()) { // poll the head request
					req = reqtask.poll();
					if (req.time + req.alive < System.currentTimeMillis()) { // time
																				// out!
						Message ret = new Message();
						ret.obj = "Time out!";
						ret.what = 100;
						recall.sendMessage(ret);
					} else { // run the request
						final thd t = new thd();
						flag = false;
						thread = new Thread(t);
						cnt = 4;
						running = true;
						thread.start();
						timer = new Timer();
						TimerTask task = new Timertk();
						timer.schedule(task, 2000, 2000);
					}
				}
			}
		}
	}

	class tmain extends TimerTask {
		public void run() {

		}
	};

	class thd implements Runnable {
		public void run() {
			try {
				SocketClient client = new SocketClient(i, p);
				client.sendMsg(req, recall);
				Message msg = new Message();
				msg.what = 1;
				handler.sendMessage(msg);
				client.closeSocket();
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.out.println("UnknownHostException");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("IOException");
			}

		}
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				flag = true;
				break;
			case 2:
				final thd t = new thd();
				thread = new Thread(t);
				thread.start();
				break;
			}
			super.handleMessage(msg);
		}
	};

	class Timertk extends TimerTask {
		public void run() {
			if (flag == false && cnt > 0) {
				cnt--;
			} else if (cnt == 0) {
				Message msg = new Message();
				msg.obj = "connecting failed";
				msg.what = 101;
				recall.sendMessage(msg);
				timer.cancel();
			} else if (flag == true) {
				timer.cancel();
				running = false;
			}
		}
	};
}