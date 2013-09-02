package com.macaroon.piztor;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.os.Handler;
import android.os.Message;

//       Piztor Transmission Protocol v2.0b beta   //

public class Transam implements Runnable {
	
	static final int Login =0;
	static final int Update =1;
	static final int UserInfo =2;
	static final int Subscription =3;
	static final int Logout =4;
	static final int StartPush =5;
	static final int SendMessage =6;
	static final int SetMarker =7;
	static final int SetPassword =8;
	static final int Checkin =9;
	
	static final int ClosePush = -5;
	
	static final int EConnectedFailedException =101;
	static final int ETimeOutException =102;
	static final int EJavaHostException =103;
	static final int EPushFailedException =104;
	static final int EIOException =105;
	static final int EUnknownHostException =106;
	static final int EStatusFailedException =107;
	static final int ELevelFailedException =108;
	static final int EPasswordFailedException =109;
	static final int ESubscribeFailedException =110;
	static final int ECheckinFailedException =111;
	
	static final int Reconnect =-2;
	static final int Exception =-1;
	static final int TimeOut =0;
	
	static final int RCheckinFailed =7;
	static final int RSubscribeFailed =6; 
	static final int RPasswordFailed =5;
	static final int RServerFetchFailed =4;
	static final int RLevelFailed =3;
	static final int RStatusFailed = 2;
	static final int RTimeOut = 1;
	static final int RSuccess = 0;
	
	Timer timer;
	Timer pushtimer;
	boolean running = false; 
	boolean pushing = false;
	int cnt = 3;				//retry times
	int tcnt;				//current remain retry times
	int rcnt;				//current remain retry times (push)
	int retime = 10000;		//timeout time
	Req req;
	int p;					//port
	String i;				//ip
	Thread thread;
	Handler recall;					//recall
	Handler handler;				//manager
	Queue<Req> reqtask ;			//request task
	
	String itoken = "";
	String iname = "";
	
	Thread Pushthread;
	PushClient push;

	Transam(String ip, int port,Handler Recall) {
		p = port;
		i = ip;
		recall = Recall;
		reqtask = new LinkedList<Req>();
		handler = new Manager(this);
	}
	
	public synchronized void send(Req r){
		if(r.type == Login){
			if(pushing == true) {
				stopPush();
			}
		}
		reqtask.offer(r);	
	}
	
	private void startPush(String token,String name) {
		if(pushing == false) {
			itoken = token;
			iname = name;
			rcnt = cnt;
			pushing = true;
			System.out.println("startpush");
			connectpush();
		}
	}
	
	private void stopPush() {
		try{
			if(push.isClosed() == false) {
				push.closeSocket();
				itoken = null;
				iname = null;
				pushing = false;
				System.out.println("closepush");
			}
		} catch (IOException e) {
			e.printStackTrace();
			Message msg = new Message();
			msg.what = Exception;
			msg.obj = new EIOException(5,0);
			recall.sendMessage(msg);
		}
	}
	
	public void setTimeOutTime(int msec){
		retime = msec;
	}
	
	public void setRetryTimes(int times){
		cnt = times;
	}
	
	
	public void setHandler(Handler Recall){
		recall = Recall;
		if(push != null) {
			push.setPushHandler(Recall);
		}
	}
	


	public void run() {								//start the main thread		
		while(true){
			if(running == false){
				
				if(!reqtask.isEmpty()){				//poll the head request
					req = reqtask.poll();	
					if(req.time + req.alive < System.currentTimeMillis()){		//time out!
						Message ret = new Message();
						ETimeOutException t = new ETimeOutException(req.type,req.time);
						ret.obj = t;
						ret.what = Exception;
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
	
	
	private void connectpush() {
		final reqpush r = new reqpush();
		Pushthread = new Thread(r);
		Pushthread.start();
	}
	
	class tmain extends TimerTask {
		public void run() {
			connect();
		}
	};
	
	class pmain extends TimerTask {
		public void run() {
			connectpush();
		}
	};
	
	private class reqpush implements Runnable {
		public void run() {
			try {
				if(itoken == null || iname == null) return;
				push = new PushClient(i,p,retime,recall);
				push.setPushHandler(recall);
				int out = push.start(new ReqStartPush(itoken,iname));
				if(out == RTimeOut) {
					push.closeSocket();
					Message msg = new Message();
					msg.what = Reconnect;
					msg.obj = new ETimeOutException(5,0);
					handler.sendMessage(msg);
				}
				else if (out == RStatusFailed){
					pushing = false;
					stopPush();
					Message msg = new Message();
					msg.what = Exception;
					msg.obj = new EStatusFailedException(5,0);
					recall.sendMessage(msg);
				}
				else if (out == RSuccess){
					rcnt = cnt;
					push.listen();
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
				Message msg = new Message();
				msg.what = Reconnect;
				msg.obj = new EUnknownHostException(5,0);
				handler.sendMessage(msg);
			} catch (IOException e) {
				e.printStackTrace();
				Message msg = new Message();
				msg.what = Reconnect;
				msg.obj = new EIOException(5,0);
				handler.sendMessage(msg);
			}

		}
	}

	private class thd implements Runnable {
		public void run() {
			try {
				SocketClient client = new SocketClient(i,p,retime);
				int out = client.sendMsg(req,recall,handler);
				if(out == RSuccess){													
					client.closeSocket();
					running = false;
				}
				else if (out == RTimeOut){
					client.closeSocket();
					Message m = new Message();					
					m.obj = new ETimeOutException(req.type,req.time);
					m.what = TimeOut;
					handler.sendMessage(m);
				}
				else if (out == RLevelFailed){
					client.closeSocket();
					Message m = new Message();					
					m.obj = new ELevelFailedException(req.type,req.time);
					m.what = Exception;
					recall.sendMessage(m);
					running = false;
				}
				else if (out == RStatusFailed){
					client.closeSocket();
					Message msg = new Message();
					msg.what = Exception;
					msg.obj = new EStatusFailedException(req.type,req.time);
					recall.sendMessage(msg);
					running = false;
				}
				else if (out == RPasswordFailed){
					client.closeSocket();
					Message msg = new Message();
					msg.what = Exception;
					msg.obj = new EPasswordFailedException(req.type,req.time);
					recall.sendMessage(msg);
					running = false;
				}
				else if (out == RSubscribeFailed){
					client.closeSocket();
					Message msg = new Message();
					msg.what = Exception;
					msg.obj = new ESubscribeFailedException(req.type,req.time);
					recall.sendMessage(msg);
					running = false;
				}
				else if (out == RCheckinFailed){
					client.closeSocket();
					Message msg = new Message();
					msg.what = Exception;
					msg.obj = new ECheckinFailedException(req.type,req.time);
					recall.sendMessage(msg);
					running = false;
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
				Message msg = new Message();
				msg.what = Exception;
				msg.obj = new EUnknownHostException(req.type,req.time);
				handler.sendMessage(msg);
			} catch (IOException e) {
				e.printStackTrace();
				Message msg = new Message();
				msg.what = Exception;
				msg.obj = new EIOException(req.type,req.time);
				handler.sendMessage(msg);
			}

		}
	}


	static class Manager extends Handler {
    	WeakReference<Transam> outerClass;
    	Manager(Transam t) {
    		outerClass = new WeakReference<Transam>(t);
    	}
    	@Override
    	public void handleMessage(Message msg) {
    		Transam out = outerClass.get();
			switch (msg.what) {
			case Exception:
				if (out.tcnt > 0) {
					out.tcnt--;
					System.out.println(out.tcnt);
					out.timer = new Timer();
					TimerTask task = out.new tmain();
					out.timer.schedule(task,out.retime);
				} else if (out.tcnt == 0) {
					out.tcnt = out.cnt;
					Message m = new Message();
					m.obj = msg.obj;
					m.what = Exception;
					out.recall.sendMessage(m);
					out.timer = new Timer();
					TimerTask task = out.new tmain();
					out.timer.schedule(task,out.retime);
				}
				break;
			case TimeOut:
				if (out.tcnt > 0) {
					out.tcnt--;
					System.out.println(out.tcnt);
					out.timer = new Timer();
					TimerTask task = out.new tmain();
					out.timer.schedule(task,out.retime);
				} else if (out.tcnt == 0) {
					out.tcnt = out.cnt;
					Message m = new Message();
					EConnectFailedException c = new EConnectFailedException(out.req.type,out.req.time);
					m.obj = c;
					m.what = Exception;
					out.recall.sendMessage(m);
					out.timer = new Timer();
					TimerTask task = out.new tmain();
					out.timer.schedule(task,out.retime);
				}
				break;
			case Reconnect:
				if (out.rcnt > 0) {
					out.rcnt--;
					System.out.println(out.rcnt);
					out.pushtimer = new Timer();
					TimerTask task = out.new pmain();
					out.pushtimer.schedule(task,out.retime);
				} else if (out.rcnt == 0) {
					out.rcnt = out.cnt;
					Message m = new Message();
					EPushFailedException c = new EPushFailedException(5,0);
					//m.obj = msg.obj;
					m.obj = c;
					m.what = Exception;
					out.recall.sendMessage(m);
					out.pushtimer = new Timer();
					TimerTask task = out.new pmain();
					out.pushtimer.schedule(task,out.retime);
				}
				break;
			case StartPush:
				@SuppressWarnings("unchecked")
				Vector<String> s = (Vector<String>) msg.obj;
				out.startPush(s.get(1),s.get(0));
				break;
			case ClosePush:
				out.stopPush();
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	
}