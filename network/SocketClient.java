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



public class SocketClient {
	static Socket client;
	
	static final int ByteLength = 1;
	static final int IntLength = 4;
	static final int DoubleLength = 8;
	static final int TokenLength = 32;

	public final static int Login =0;
	public final static int Update =1;
	public final static int Location =2;
	public final static int UserInfo =3;
	public final static int Logout =4;
	public final static int SendMessage =6;
	
	public final static int GroupID =0;
	public final static int Gender =1;
	
	public SocketClient(String site, int port, int retime) throws UnknownHostException,
			IOException {
		try {
			client = new Socket();
			client.connect(new InetSocketAddress(site,port), retime);
			client.setSoTimeout(retime);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} 
	}

	public int sendMsg(Req req,Handler recall) throws IOException,SocketTimeoutException {
		try {
			DataOutputStream out = new DataOutputStream(
					client.getOutputStream());
			int tmp = req.type;
			int len;
			byte[] b;
			int pos = 0;
			switch (tmp) {
			case Login:
				ReqLogin rau = (ReqLogin) req;
				len = IntLength+ByteLength+rau.user.length()+ByteLength+rau.pass.length()+ByteLength;	
				b = new byte[len];			
				write(b,intToBytes(len),pos);
				pos+=IntLength;
				b[pos] = (byte) tmp;
				pos+=ByteLength;
				write(b,rau.user.getBytes(),pos);
				pos+=rau.user.length();
				b[pos] = 0;
				pos+=ByteLength;
				write(b, rau.pass.getBytes(),pos);
				pos+= rau.pass.length();
				b[pos] = 0;
				pos+=ByteLength;
				out.write(b);
				break;
			case Update:
				ReqUpdate rup = (ReqUpdate) req;
				len = IntLength+ByteLength+TokenLength+(rup.uname).length()+ByteLength+DoubleLength+DoubleLength;				
				b = new byte[len];
				write(b,intToBytes(len),pos);
				pos+=IntLength;
				b[pos] = (byte) tmp;
				pos+=ByteLength;
				write(b,hexStringToBytes(rup.token),pos);
				pos+=TokenLength;
				write(b,(rup.uname).getBytes(),pos);
				pos+=(rup.uname).length();
				b[pos] = 0;
				pos+=ByteLength;
				write(b,doubleToBytes(rup.latitude),pos);
				pos+=DoubleLength;
				write(b,doubleToBytes(rup.longitude),pos);
				pos+=DoubleLength;
				out.write(b);
				break;
			case Location:
				ReqLocation ras = (ReqLocation) req;
				len = IntLength+ByteLength+TokenLength+ras.uname.length()+ByteLength+ByteLength+ByteLength;				
				b = new byte[len];
				write(b,intToBytes(len),pos);
				pos+=IntLength;
				b[pos] = (byte) tmp;
				pos+=ByteLength;
				write(b,hexStringToBytes(ras.token),pos);
				pos+=TokenLength;
				write(b,(ras.uname).getBytes(),pos);
				pos+=(ras.uname).length();
				b[pos] = 0;
				pos+=ByteLength;
				b[pos] = (byte) ras.company;
				pos+=ByteLength;
				b[pos] = (byte) ras.section;
				pos+=ByteLength;				
				out.write(b);
				break;
			case UserInfo:
				ReqUserInfo rus = (ReqUserInfo) req;
				len = IntLength+ByteLength+TokenLength+rus.uname.length()+ByteLength+IntLength;				
				b = new byte[len];
				write(b,intToBytes(len),pos);
				pos+=IntLength;
				b[pos] = (byte) tmp;
				pos+=ByteLength;
				write(b,hexStringToBytes(rus.token),pos);
				pos+=TokenLength;
				write(b,(rus.uname).getBytes(),pos);
				pos+=(rus.uname).length();
				b[pos] = 0;
				pos+=ByteLength;
				write(b,intToBytes(rus.uid),pos);
				pos+=IntLength;
				out.write(b);
				break;
			case Logout:
				ReqLogout rlo = (ReqLogout) req;
				len = IntLength+ByteLength+TokenLength+rlo.uname.length()+ByteLength;				
				b = new byte[len];
				write(b,intToBytes(len),pos);
				pos+=IntLength;
				b[pos] = (byte) tmp;
				pos+=ByteLength;
				write(b,hexStringToBytes(rlo.token),pos);
				pos+=TokenLength;
				write(b,(rlo.uname).getBytes(),pos);
				pos+=(rlo.uname).length();
				b[pos] = 0;
				pos+=ByteLength;
				out.write(b);
				break;
			case SendMessage:
				ReqSendMessage rem = (ReqSendMessage) req;
				len = IntLength+ByteLength+TokenLength+rem.uname.length()+ByteLength+rem.msg.length()+ByteLength;	
				b = new byte[len];
				write(b,intToBytes(len),pos);
				pos+=IntLength;
				b[pos] = (byte) tmp;
				pos+=ByteLength;
				write(b,hexStringToBytes(rem.token),pos);
				pos+=TokenLength;
				write(b,(rem.uname).getBytes(),pos);
				pos+=(rem.uname).length();
				b[pos] = 0;
				pos+=ByteLength;
				write(b,rem.msg.getBytes(),pos);
				pos+=rem.msg.length();
				b[pos] = 0;
				pos+=ByteLength;
				out.write(b);
				break;
			}
			out.flush();
			DataInputStream in = new DataInputStream(client.getInputStream());
			Message msg = new Message();
			int outlen = in.readInt();
			int type = in.readUnsignedByte();
			int status = in.readUnsignedByte();
			switch (type) {
			case Login:
				int id = in.readInt();
				byte[] buffer = new byte[32];
				in.read(buffer);
				String tk = "";
				for (int i = 0; i < buffer.length; i++) {
				   String hex = Integer.toHexString(buffer[i] & 0xFF);
				   if (hex.length() == 1) {
				    hex = '0' + hex;
				   }
				   tk += hex;
				}
				ResLogin rchklogin = new ResLogin(id,tk,status);
				msg.obj = rchklogin;
				msg.what = Login;
				recall.sendMessage(msg);
				break;
			case Update:
				msg.obj = new ResUpdate(status);
				msg.what = Update;
				recall.sendMessage(msg);
				break;
			case Location:
				int n = 0;
				outlen-=(IntLength+ByteLength+ByteLength);
				Vector<RLocation> tmpv = new Vector<RLocation>();
				while(outlen > 0) {
					int tid = in.readInt();
					double lat = in.readDouble();
					double lot = in.readDouble();
					tmpv.add(new RLocation(tid,lat,lot));
					outlen -= (IntLength+DoubleLength+DoubleLength);
					n++;
				}
				msg.obj = new ResLocation(n,status,tmpv);
				msg.what = Location;
				recall.sendMessage(msg);
				break;
			case UserInfo:
				outlen-=(IntLength+ByteLength+ByteLength);
				ReqUserInfo rus = (ReqUserInfo) req;
				int u = rus.uid;
				int com = 0,sec = 0,s = 0;
				while(outlen > 0) {
					int typ = in.readUnsignedByte();
					outlen-=ByteLength;
					switch(typ){
					case GroupID:
						com = in.readUnsignedByte();
						sec = in.readUnsignedByte();
						outlen-=(ByteLength+ByteLength);
						break;
					case Gender:
						s = in.readUnsignedByte();
						outlen-=ByteLength;
						break;
					}
				}
				msg.obj = new ResUserInfo(status,u,com,sec,s);
				msg.what = UserInfo;
				recall.sendMessage(msg);
				break;
			case Logout:
				msg.obj = new ResLogout(status);
				msg.what = Logout;
				recall.sendMessage(msg);
				break;
			case SendMessage:
				msg.obj = new ResSendMessage(status);
				msg.what = SendMessage;
				recall.sendMessage(msg);
				break;
			}
			return 0;

		} catch (SocketTimeoutException e){
			return 1;			
		} catch (IOException e) {
			throw e;
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
	
	public static byte[] doubleToBytes(double d){
		  byte[] b=new byte[8];
		  long l=Double.doubleToLongBits(d);
		  for(int i=0;i<8;i++){
			  b[i] = (byte)(l >>> 8*(7-i));
		  }
		  return b;
	}
	
	private static void write(byte[] s,byte[] w,int l) {
		
		for(int i=0;i<w.length;i++){
			s[i+l] = w[i];
		}
	}

}
