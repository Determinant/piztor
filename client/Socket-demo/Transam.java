package com.example.test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

public class Transam implements Runnable {
	public Timer timer;
	public boolean flag = true;
	public int cnt = 4;
	public int port;
	public String ip;
	Thread thread;
	Myrequest req;
	Myrespond res;
	Controller core;
	
	Transam(String i,int p,Myrequest r,Controller c){
		port = p;
		ip = i;
		req = r;
		core = c;
	}

	public void run() {
    	final thd t = new thd();
    	flag = false;
    	thread = new Thread(t);
    	cnt = 4;
    	thread.start();
    	timer = new Timer();
    	TimerTask task = new Timertk();
    	timer.schedule(task,2000,2000);
	}
	
	class thd implements Runnable {
		@Override
		public void run() {
			try{
        	SocketClient client = new SocketClient(ip,port);
        	res = client.sendMsg(req); 
        	core.recieveInfo(res);
        	Message msg = new Message();
         	msg.what = 1;
         	handler.sendMessage(msg); 
			client.closeSocket();	
			}catch (UnknownHostException e){
			}catch (IOException e){
			}
			 
		}
	}
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler(){   
    	public void handleMessage(Message msg) {   
    	switch (msg.what) {   
    	case 1:       	 
    	flag = true;
    	break; 
    	case 2:     
    	res = new Myrespond();
    	res.wrong = msg.obj.toString();    	
     	core.recieveInfo(res);
        break;
    	case 3:
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
    	if(flag==false&&cnt>0){
         	cnt--;
    	}
    	else if(cnt==0) {
    		Message msg = new Message();
        	msg.obj = "connecting failed";
         	msg.what = 2;
         	handler.sendMessage(msg);
    		timer.cancel();
    	}
    	else if(flag==true){
    		timer.cancel();
    	}
    	}   
    };  
}