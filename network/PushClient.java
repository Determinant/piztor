package com.macaroon.piztor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Vector;

import android.annotation.SuppressLint;
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

	public final static int StartPush =5;
	
	public final static int Message = 0;
	public final static int Location = 1;
	public final static int PushMessage =100;
	public final static int PushLocation =101;
	
	public final static int Reconnect =-2;
	
	public final static int Failed = 2;
	public final static int TimeOut = 1;
	public final static int Success = 0;
	
	private String LastPrint = "";

	
	public PushClient(String site, int port,int retime) throws UnknownHostException,
			IOException {
		try {
			client = new Socket();
			client.connect(new InetSocketAddress(site,port), retime);
			client.setSoTimeout(2000);
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
			write(b,intToBytes(len),pos);
			pos+=IntLength;
			b[pos] = (byte) 5;
			pos+=ByteLength;
			write(b,hexStringToBytes(r.token),pos);
			pos+=TokenLength;
			write(b,r.uname.getBytes(),pos);
			pos+=r.uname.length();
			b[pos] = 0;
			pos+=ByteLength;
			out.write(b);
			out.flush();
			Message msg = new Message();
			in.readInt();
			in.readUnsignedByte();
		    int status = in.readUnsignedByte();
		    ResStartPush rchk = new ResStartPush(status);
			msg.obj = rchk;
			msg.what = StartPush;
			recall.sendMessage(msg);
			if(status == 1) {
				return Failed;
			}
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

	public void listen(Handler recall,Handler h) throws IOException{
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
				String p = byteToHexString(buffer);
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
					write(o,intToBytes(outlen),pos);				//can be folded!
					pos+=IntLength;
					o[pos]=(byte) Message;
					pos+=ByteLength;
					write(o,hexStringToBytes(p),pos);
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
					write(o,intToBytes(outlen),pos);
					pos+=IntLength;
					o[pos]=(byte) Location;
					pos+=ByteLength;
					write(o,hexStringToBytes(p),pos);
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
	
	@SuppressLint("DefaultLocale")
	private static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
				int pos = i * 2;
				d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}
	
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
	
	private static byte[] intToBytes(int i) {   
		  byte[] d = new byte[4];   
		  d[0] = (byte)((i >> 24) & 0xFF);
		  d[1] = (byte)((i >> 16) & 0xFF);
		  d[2] = (byte)((i >> 8) & 0xFF); 
		  d[3] = (byte)(i & 0xFF);
		  return d;
	}
	
	@SuppressLint("UseValueOf")
	public static byte[] doubleToBytes(double d){
		  byte[] b=new byte[8];
		  long l=Double.doubleToLongBits(d);
		  for(int i=0;i < 8;i++){
		   b[i]=new Long(l).byteValue();
		   l=l>>8;
		  }
		  return b;
	}
	
	private static void write(byte[] s,byte[] w,int l) {
		
		for(int i=0;i<w.length;i++){
			s[i+l] = w[i];
		}
	}
	
	private static String byteToHexString(byte[] buffer){
		String p ="";
		for (int i = 0; i < buffer.length; i++) {
			   String hex = Integer.toHexString(buffer[i] & 0xFF);
			   if (hex.length() == 1) {
			    hex = '0' + hex;
			   }
			   p += hex;
		}
		return p;
	}

}
