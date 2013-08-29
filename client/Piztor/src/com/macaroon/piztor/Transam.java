package com.macaroon.piztor;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

//       Piztor Transmission Protocol v0.4a       //

//------------------------------------------------//
//												  //
//				*return msg type*			      //
//				0   for   login					  //
//              1   for   updateLocation		  //
//              2   for   locationRequest		  //
//			    3   for   userinfo				  //
//              4   for   logout                  //
//              5   for   requestpush             //
//              6   for   sendmessage             //
//												  //	
//            100   for   pushmessage	          //
//											      //
//     ----------I'm the division line--------    //
//                                                //
//             -1   for   Exceptions			  //
//       Exception (req type , exception type)    //
//                                                //
//    ----------I'm the division line--------     //
//                                                //
//				 *Request form*					  //
//        login -- username & password			  //
//update -- token & username & latitude & longitude//
//getlocation -- token & username & company & section//
//    getuserinfo -- token & userinfo & userid    //
//       logout -- token & username               //
//    send message -- token & username & message  //
//												  //
//    ----------I'm the division line--------     //
//                                                //
//               *Respond form*					  //
//        login -- status & userid & token        //
//			  update -- status                    //
//     getlocation -- status & entrynumber & data //
//       entry  -- userid & latitude & longitude  //
//                                                //
//getuserinfo -- status & uid & company & section & gender//
//                logout -- status                //
//			request push -- status				  //
//          send message -- status                //
//												  //
//          status -- 0 for success               //
//					  1 for failed/invalid        //				
//												  //
//          push message -- message               //
//                                                //
//------------------------------------------------//



public class Transam implements Runnable {
	
	public final static int Login =0;
	public final static int Update =1;
	public final static int Location =2;
	public final static int UserInfo =3;
	public final static int Logout =4;
	public final static int StartPush =5;
	public final static int SendMessage =6;
	
	public final static int PushMessage =100;
	public final static int PushLocation =101;
	
	public final static int GroupID =0;
	public final static int Gender =1;
	
	public final static int EConnectedFailedException =101;
	public final static int ETimeOutException =102;
	public final static int EJavaHostException =103;
	public final static int EPushFailedException =104;
	public final static int EIOException =105;
	public final static int EUnknownHostException =106;
	
	public Timer timer;
	public boolean running = false; 
	public boolean flag = true;
	public int cnt = 4;				//retry times
	public int tcnt;				//current remain retry times
	public int rcnt;				//current remain retry times (push)
	public int retime = 2000;		//timeout time
	Res res;
	Req req;
	public int p;					//port
	public String i;				//ip
	Thread thread;
	Handler core;
	Handler recall;					//recall
	Queue<Req> reqtask ;			//request task
	
	public String itoken;
	public String iname;
	
	Thread Pushthread;
	PushClient push;
	
	
	public final static int Reconnect =-2;
	public final static int Exception =-1;
	public final static int TimeOut =0;

	Transam(String ip, int port,Handler Recall) {
		p = port;
		i = ip;
		recall = Recall;
		reqtask = new LinkedList<Req>();
	}
	
	public void send(Req r){
		reqtask.offer(r);
		
	}
	
	public void startPush(String token,String name) {
		itoken = token;
		iname = name;
		rcnt = cnt;
		connectpush();
	}
	
	public void stopPush() {
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
		reqtask.clear();
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
	
	class reqpush implements Runnable {
		public void run() {
			try {
				if(itoken == null || iname == null) return;
				push = new PushClient(i,p,retime);
				push.setPushHandler(recall);
				int out = push.start(new ReqStartPush(itoken,iname));
				if(out == 1) {
					push.closeSocket();
					Message msg = new Message();
					msg.what = Reconnect;
					msg.obj = new ETimeOutException(5,0);
					handler.sendMessage(msg);
				}
				else if (out == 2){
					stopPush();
				}
				else {
					push.listen(recall,handler);
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
					Message m = new Message();
					EConnectFailedException c = new EConnectFailedException(req.type,req.time);
					m.obj = c;
					m.what = Exception;
					handler.sendMessage(m);
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
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Exception:
				if (tcnt > 0) {
					tcnt--;
					System.out.println(tcnt);
					connect();
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
					connect();
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
					connectpush();
				} else if (rcnt == 0) {
					Message m = new Message();
					//EPushFailedException c = new EPushFailedException(req.type);
					m.obj = msg.obj;
					m.what = Exception;
					recall.sendMessage(m);
				}
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	class EException extends Exception {
		private static final long serialVersionUID = 100L;
		int Rtype;
		int Etype;
		long time;
		public EException(int e,int r,long timep) {  
			super();
			Rtype = r;
			Etype = e;
			time = timep;
			}
	}
	
	class EConnectFailedException extends EException{
		private static final long serialVersionUID = 101L;
		public EConnectFailedException(int t,long timep) {  
			super(101,t,timep);
			}		
	}
	
	class ETimeOutException extends EException{
		private static final long serialVersionUID = 102L;
		public ETimeOutException(int t,long timep) {  
			super(102,t,timep);
			}		
	}
	
	class EJavaHostException extends EException{
		private static final long serialVersionUID = 103L;
		public EJavaHostException(int t,long timep) {  
			super(103,t,timep);
		}		
	}
	
	class EPushFailedException extends EException{
		private static final long serialVersionUID = 104L;
		public EPushFailedException(int t,long timep) {  
			super(104,t,timep);
		}		
	}
	
	class EIOException extends EException{
		private static final long serialVersionUID = 105L;
		public EIOException(int t,long timep) {  
			super(105,t,timep);
		}		
	}	
	
	class EUnknownHostException extends EException{
		private static final long serialVersionUID = 106L;
		public EUnknownHostException(int t,long timep) {  
			super(106,t,timep);
		}		
	}	
}