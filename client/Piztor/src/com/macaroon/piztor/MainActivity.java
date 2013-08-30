package com.macaroon.piztor;

import java.util.Vector;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	public String token = "";
	public String uname = "";
	public int com;
	public int sec;
	public int step = 0;
	Transam t;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t = new Transam("192.168.1.171",2223,handler);
		Thread thread = new Thread(t);
		thread.start();
		
    }

    Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				token = ((ResLogin) msg.obj ).t;
				uname = ((ResLogin) msg.obj ).uinfo.username;
				com = ((ResLogin) msg.obj ).uinfo.gid.company;
				sec = ((ResLogin) msg.obj ).uinfo.gid.section;
				System.out.println(com);
				System.out.println(sec);
				System.out.println(((ResLogin) msg.obj ).uinfo.sex);
				for(int i=0;i<((ResLogin) msg.obj ).subscribeNumber;i++){
					System.out.println(((ResLogin) msg.obj ).sublist.get(i).company);	
					System.out.println(((ResLogin) msg.obj ).sublist.get(i).section);
				}
				System.out.println(token);			
				break;
			case 2:
				ResUserInfo r = (ResUserInfo) msg.obj ;
				System.out.println(r.number);
				for(int i=0;i<r.number;i++){
					System.out.println(r.uinfo.get(i).nickname);	
					System.out.println(r.uinfo.get(i).latitude);
					System.out.println(r.uinfo.get(i).longitude);
				}
				break;
			case 100:
				System.out.println(((ResPushMessage) msg.obj).message);
				break;
			case 101:
				int nn = ((ResPushLocation)msg.obj).n;
				Vector<RLocation> ll = ((ResPushLocation)msg.obj).l;
				for(int i=0;i<nn;i++){
					System.out.println(ll.get(i).latitude + " " + ll.get(i).longitude);
				}
				break;
			case -1:
				System.out.println(-1);
			}
			super.handleMessage(msg);
		}
	};
	
	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    
   
	
    
    public void sendMessage(View view) {
    	
    		if(step == 0){
    		long time = System.currentTimeMillis();
    		ReqLogin r = new ReqLogin("hello","world",time,2000);   		
    		t.send(r);
    		step++;
    		}
    		else if(step == 1){
    			long time = System.currentTimeMillis();
    			ReqUpdate r = new ReqUpdate (token,uname,123.456,654.321,time,2000);  	
        		t.send(r);
        		step++;
    		}
    		else if(step == 2){
    			long time = System.currentTimeMillis();
    			ReqSendMessage r = new ReqSendMessage (token,uname,"wurusai",time,2000); 
        		t.send(r);
        		step++;
    		}
    		else if(step == 3){
    			long time = System.currentTimeMillis();
    			ReqLogout r = new ReqLogout(token,uname,time,2000);
        		t.send(r);
        		step++;
    		}

    	
    	
    }
    
    
	
	
    
    
    
      
   
    
    
}






