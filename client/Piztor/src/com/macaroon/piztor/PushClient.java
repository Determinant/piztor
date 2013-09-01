package com.macaroon.piztor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Vector;

import android.os.Handler;
import android.os.Message;



public class PushClient {
	static Socket client;
	static Handler recall;
	
	static final int ByteLength = 1;
	static final int IntLength = 4;
	static final int DoubleLength = 8;
	static final int TokenLength = 32;
	static final int FingerPrintLength = 32;

	static final int StartPush =5;
	
	static final int Message = 0;
	static final int Location = 1;
	static final int Marker = 2;
	static final int PushMessage =100;
	static final int PushLocation =101;
	static final int PushMarker =102;
	
	static final int Reconnect =-2;
	
	static final int StatusFailed = 2;
	static final int TimeOut = 1;
	static final int Success = 0;
	
	private String LastPrint = "";

	
	public PushClient(String site, int port,int retime) throws UnknownHostException,
			IOException {
		try {
			client = new Socket();
			client.connect(new InetSocketAddress(site,port), retime);
			client.setSoTimeout(2000);
			//client.setTcpNoDelay(true);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} 
	}
	
	public void setPushHandler(Handler handler) {
		recall = handler;
	}
	
	public int start(ReqStartPush r) throws IOException,SocketTimeoutException {
		try {			
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			DataInputStream in = new DataInputStream(client.getInputStream());
			int len;
			len = IntLength+ByteLength+TokenLength+(r.uname).length()+ByteLength;	
			byte[] b = new byte[len];
			int pos = 0;
			Convert.write(b,Convert.intToBytes(len),pos);
			pos+=IntLength;
			b[pos] = (byte) 5;
			pos+=ByteLength;
			Convert.write(b,Convert.hexStringToBytes(r.token),pos);
			pos+=TokenLength;
			Convert.write(b,r.uname.getBytes(),pos);
			pos+=r.uname.length();
			b[pos] = 0;
			pos+=ByteLength;
			out.write(b);
			out.flush();
			Message msg = new Message();
			in.readInt();
			in.readUnsignedByte();
		    int status = in.readUnsignedByte();
		    if(status == 1) return StatusFailed;
		    ResStartPush rchk = new ResStartPush();
			msg.obj = rchk;
			msg.what = StartPush;
			recall.sendMessage(msg);
			return Success;
		} catch (SocketTimeoutException e){
			e.printStackTrace();
			return TimeOut;			
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} 
	}
	
	public boolean isClosed() {
		return client.isClosed();
	}

	public void listen(Handler recall) throws IOException{
		client.setSoTimeout(0);
		DataInputStream in = new DataInputStream(client.getInputStream());
		DataOutputStream out = new DataOutputStream(client.getOutputStream());
		
		while(true){
			try {
				int len = in.readInt();
				System.out.println(len);
				int tmp = in.readUnsignedByte();
				byte[] buffer = new byte[32];
				in.read(buffer);
				String p = Convert.byteToHexString(buffer);
				int outlen;
				int pos=0;
				byte[] o = new byte[IntLength+ByteLength+FingerPrintLength];;
				outlen = IntLength+ByteLength+FingerPrintLength;
				switch(tmp) {
				case Message:				
					byte[] b = new byte[len-IntLength-ByteLength-FingerPrintLength-ByteLength];
					in.read(b);
					String m = new String(b);
					in.readUnsignedByte();
					if(LastPrint != p) {
					Message msg = new Message();
					msg.what = PushMessage;
					msg.obj = new ResPushMessage(m);
					recall.sendMessage(msg);
					LastPrint = p;
					}			
					Convert.write(o,Convert.intToBytes(outlen),pos);				//can be folded!
					pos+=IntLength;
					o[pos]=(byte) Message;
					pos+=ByteLength;
					Convert.write(o,Convert.hexStringToBytes(p),pos);
					pos+=FingerPrintLength;
					out.write(o);
					out.flush();
					break;
				case Location:
					len-=(IntLength+ByteLength+FingerPrintLength);
					int n=0;
					Vector<RLocation> tmpv = new Vector<RLocation>();
					while(len > 0) {
						int tid = in.readInt();
						double lat = in.readDouble();
						double lot = in.readDouble();
						tmpv.add(new RLocation(tid,lat,lot));
						len -= (IntLength+DoubleLength+DoubleLength);
						n++;
					}
					if(LastPrint != p) {
						Message msg = new Message();
						msg.obj = new ResPushLocation(n,tmpv);
						msg.what = PushLocation;
						recall.sendMessage(msg);
						LastPrint = p;
					}				
					Convert.write(o,Convert.intToBytes(outlen),pos);
					pos+=IntLength;
					o[pos]=(byte) Location;
					pos+=ByteLength;
					Convert.write(o,Convert.hexStringToBytes(p),pos);
					pos+=FingerPrintLength;
					out.write(o);
					out.flush();
					break;
				case Marker:
					int lv = in.readUnsignedByte();
					double lat = in.readDouble();
					double lot = in.readDouble();
					int dtime = in.readInt(); 
					if(LastPrint != p) {
					Message msg = new Message();
					msg.what = PushMarker;
					msg.obj = new ResPushMarker(lat,lot,dtime,lv);
					recall.sendMessage(msg);
					LastPrint = p;
					}			
					Convert.write(o,Convert.intToBytes(outlen),pos);
					pos+=IntLength;
					o[pos]=(byte) Marker;
					pos+=ByteLength;
					Convert.write(o,Convert.hexStringToBytes(p),pos);
					pos+=FingerPrintLength;
					out.write(o);
					out.flush();
					break;
				}
			
			} catch (IOException e) {
			e.printStackTrace();
			throw e;
			}
		}
	}

	public void closeSocket() throws IOException{
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

}
