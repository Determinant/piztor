package com.macaroon.piztor;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

//       Piztor Transmission Protocol v2.0a beta   //

public class Transam implements Runnable {
	
	static final int Login =0;
	static final int Update =1;
	static final int UserInfo =2;
	static final int Subscription =3;
	static final int Logout =4;
	static final int StartPush =5;
	static final int SendMessage =6;
	static final int SetMarker =7;
	
	static final int ClosePush = -5;
	
	static final int EConnectedFailedException =101;
	static final int ETimeOutException =102;
	static final int EJavaHostException =103;
	static final int EPushFailedException =104;
	static final int EIOException =105;
	static final int EUnknownHostException =106;
	static final int EStatusFailedException =107;
	static final int ELevelFailedException =108;
	
	static final int Reconnect =-2;
	static final int Exception =-1;
	static final int TimeOut =0;
	
	static final int RLevelFailed =3;
	static final int RStatusFailed = 2;
	static final int RTimeOut = 1;
	static final int RSuccess = 0;
	
	private Timer timer;
	private Timer pushtimer;
	private boolean running = false; 
	private int cnt = 5;				//retry times
	private int tcnt;				//current remain retry times
	private int rcnt;				//current remain retry times (push)
	private int retime = 2000;		//timeout time
	private Req req;
	private int p;					//port
	private String i;				//ip
	private Thread thread;
	private Handler recall;					//recall
	private Queue<Req> reqtask ;			//request task
	
	private String itoken;
	private String iname;
	
	private Thread Pushthread;
	private PushClient push;

	Transam(String ip, int port,Handler Recall) {
		p = port;
		i = ip;
		recall = Recall;
		reqtask = new LinkedList<Req>();
	}
	
	public void send(Req r){
		reqtask.offer(r);
		
	}
	
	private void startPush(String token,String name) {
		itoken = token;
		iname = name;
		rcnt = cnt;
		connectpush();
	}
	
	private void stopPush() {
		try{
			if(push.isClosed() == false) {
				push.closeSocket();
				itoken = null;
				iname = null;
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
		reqpush r = new reqpush();
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
				push = new PushClient(i,p,retime);
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
					stopPush();
					Message msg = new Message();
					msg.what = Exception;
					msg.obj = new EStatusFailedException(5,0);
					recall.sendMessage(msg);
				}
				else {
					rcnt = cnt;
					push.listen(recall);
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
				else {
					client.closeSocket();
					Message msg = new Message();
					msg.what = Exception;
					msg.obj = new EStatusFailedException(5,0);
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


	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Exception:
				if (tcnt > 0) {
					tcnt--;
					System.out.println(tcnt);
					timer = new Timer();
					TimerTask task = new tmain();
					timer.schedule(task,retime);
				} else if (tcnt == 0) {
					Message m = new Message();
					m.obj = msg.obj;
					m.what = Exception;
					recall.sendMessage(m);
					running = false;
				}
				break;
			case TimeOut:
				if (tcnt > 0) {
					tcnt--;
					System.out.println(tcnt);
					timer = new Timer();
					TimerTask task = new tmain();
					timer.schedule(task,retime);
				} else if (tcnt == 0) {
					Message m = new Message();
					EConnectFailedException c = new EConnectFailedException(req.type,req.time);
					m.obj = c;
					m.what = Exception;
					recall.sendMessage(m);
					running = false;
				}
				break;
			case Reconnect:
				if (rcnt > 0) {
					rcnt--;
					System.out.println(rcnt);
					pushtimer = new Timer();
					TimerTask task = new pmain();
					pushtimer.schedule(task,retime);
				} else if (rcnt == 0) {
					Message m = new Message();
					EPushFailedException c = new EPushFailedException(5,0);
					//m.obj = msg.obj;
					m.obj = c;
					m.what = Exception;
					recall.sendMessage(m);
				}
				break;
			case StartPush:
				@SuppressWarnings("unchecked")
				Vector<String> s = (Vector<String>) msg.obj;
				startPush(s.get(1),s.get(0));
				System.out.println("startpush");
				break;
			case ClosePush:
				stopPush();
				System.out.println("closepush");
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	
}