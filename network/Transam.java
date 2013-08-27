package com.macaroon.piztor;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

//       Piztor Transmission Protocol v0.4        //

//------------------------------------------------//
//												  //
//				*return msg type*			      //
//				0   for   login					  //
//              1   for   updateLocation		  //
//              2   for   locationRequest		  //
//			    3   for   userinfo				  //
//											      //
//     ----------I'm the division line--------    //
//                                                //
//             -1   for   Exceptions			  //
//                                                //
//    ----------I'm the division line--------     //
//                                                //
//				 *Request form*					  //
//        login -- username & password			  //
//update -- token & username & latitude & longitude//
//	  getlocation -- token & username & groupid   //
//    getuserinfo -- token & userinfo & userid    //
//												  //
//    ----------I'm the division line--------     //
//                                                //
//               *Respond form*					  //
//        login -- status & userid & token        //
//			  update -- status                    //
//     getlocation -- status & entrynumber & data //
//       entry  -- userid & latitude & longitude  //
//                                                //
//  getuserinfo -- status & uid & gid & gender    //
//												  //
//          status -- 0 for success               //
//					  1 for failed/invalid        //				
//												  //
//------------------------------------------------//

public class Transam implements Runnable {
	public Timer timer;
	public boolean running = false; 
	public boolean flag = true;
	public int cnt = 4;				//retry times
	public int tcnt;				//current remain retry times
	public int retime = 10000;		//timeout time
	Res res;
	Req req;
	public int p;					//port
	public String i;				//ip
	Thread thread;
	Handler core;
	Handler recall;					//recall
	Queue<Req> reqtask ;			//request task

	Transam(String ip, int port,Handler Recall) {
		p = port;
		i = ip;
		recall = Recall;
		reqtask = new LinkedList<Req>();
	}
	
	public void send(Req r){
		reqtask.offer(r);
		
	}
	
	public void setTimeOutTime(int msec){
		retime = msec;
	}
	
	public void setRetryTimes(int times){
		cnt = times;
	}
	
	
	public void setHandler(Handler Recall){
		recall = Recall;
		reqtask.clear();
	}

	public void run() {								//start the main thread		
		while(true){
			if(running == false){
				
				if(!reqtask.isEmpty()){				//poll the head request
					req = reqtask.poll();	
					if(req.time + req.alive < System.currentTimeMillis()){		//time out!
						Message ret = new Message();
						TimeOutException t = new TimeOutException();
						ret.obj = t;
						ret.what = -1;
						recall.sendMessage(ret);
					}
					else{	                        //run the request
						running = true;
						tcnt = cnt;
						connect();
					}
				}				
			}
		}
	}
	
	private void connect(){
		final thd t = new thd();
		thread = new Thread(t);
		thread.start();
	}

	class thd implements Runnable {
		public void run() {
			try {
				SocketClient client = new SocketClient(i,p,retime);
				int out = client.sendMsg(req,recall);
				if(out == 0){													
					client.closeSocket();
					running = false;
				}
				else {
					client.closeSocket();
					Message msg = new Message();
					msg.what = 0;
					handler.sendMessage(msg);
				}				
			} catch (UnknownHostException e) {
				e.printStackTrace();
				Message msg = new Message();
				msg.what = -1;
				msg.obj = e;
				handler.sendMessage(msg);
			} catch (IOException e) {
				e.printStackTrace();
				Message msg = new Message();
				msg.what = -1;
				msg.obj = e;
				handler.sendMessage(msg);
			}

		}
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case -1:
				if (tcnt > 0) {
					tcnt--;
					System.out.println(tcnt);
					connect();
				} else if (tcnt == 0) {
					Message m = new Message();
					m.obj = msg.obj;
					m.what = -1;
					recall.sendMessage(m);
					running = false;
				}
				break;
			case 0:
				if (tcnt > 0) {
					tcnt--;
					connect();
				} else if (tcnt == 0) {
					Message m = new Message();
					ConnectFailedException c = new ConnectFailedException();
					m.obj = c;
					m.what = -1;
					recall.sendMessage(m);
					running = false;
				}
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	class ConnectFailedException extends Exception{
		private static final long serialVersionUID = 101L;
		public ConnectFailedException() {  
			super();  
			}		
	}
	
	class TimeOutException extends Exception{
		private static final long serialVersionUID = 102L;
		public TimeOutException() {  
			super();  
			}	
		
	}
	
	class JavaHostException extends Exception{
		private static final long serialVersionUID = 103L;
		public JavaHostException() {  
			super();  
			}	
		
	}
	
}