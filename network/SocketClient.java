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
	
	public final static int GroupID =0;
	public final static int Gender =1;
	
	public SocketClient(String site, int port, int retime) throws UnknownHostException,
			IOException {
		try {
			client = new Socket();
			client.connect(new InetSocketAddress(site,port), 5000);
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
			switch (tmp) {
			case Login:
				ReqLogin rau = (ReqLogin) req;
				len = IntLength+ByteLength+rau.user.length()+ByteLength+rau.pass.length()+ByteLength;	
				byte[] b = new byte[len];
				int pos = 0;
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
				byte[] b1 = new byte[len];
				int pos1 = 0;
				write(b1,intToBytes(len),pos1);
				pos1+=IntLength;
				b1[pos1] = (byte) tmp;
				pos1+=ByteLength;
				write(b1,hexStringToBytes(rup.token),pos1);
				pos1+=TokenLength;
				write(b1,(rup.uname).getBytes(),pos1);
				pos1+=(rup.uname).length();
				b1[pos1] = 0;
				pos1+=ByteLength;
				write(b1,doubleToBytes(rup.latitude),pos1);
				pos1+=DoubleLength;
				write(b1,doubleToBytes(rup.longitude),pos1);
				pos1+=DoubleLength;
				out.write(b1);
				break;
			case Location:
				ReqLocation ras = (ReqLocation) req;
				len = IntLength+ByteLength+TokenLength+ras.uname.length()+ByteLength+ByteLength+ByteLength;				
				byte[] b2 = new byte[len];
				int pos2 = 0;
				write(b2,intToBytes(len),pos2);
				pos2+=IntLength;
				b2[pos2] = (byte) tmp;
				pos2+=ByteLength;
				write(b2,hexStringToBytes(ras.token),pos2);
				pos2+=TokenLength;
				write(b2,(ras.uname).getBytes(),pos2);
				pos2+=(ras.uname).length();
				b2[pos2] = 0;
				pos2+=ByteLength;
				b2[pos2] = (byte) ras.company;
				pos2+=ByteLength;
				b2[pos2] = (byte) ras.section;
				pos2+=ByteLength;				
				out.write(b2);
				break;
			case UserInfo:
				ReqUserInfo rus = (ReqUserInfo) req;
				len = IntLength+ByteLength+TokenLength+rus.uname.length()+ByteLength+IntLength;				
				byte[] b3 = new byte[len];
				int pos3 = 0;
				write(b3,intToBytes(len),pos3);
				pos3+=IntLength;
				b3[pos3] = (byte) tmp;
				pos3+=ByteLength;
				write(b3,hexStringToBytes(rus.token),pos3);
				pos3+=TokenLength;
				write(b3,(rus.uname).getBytes(),pos3);
				pos3+=(rus.uname).length();
				b3[pos3] = 0;
				pos3+=ByteLength;
				write(b3,intToBytes(rus.uid),pos3);
				pos3+=IntLength;
				out.write(b3);
				break;
			case Logout:
				ReqLogout rlo = (ReqLogout) req;
				len = IntLength+ByteLength+TokenLength+rlo.uname.length()+ByteLength;				
				byte[] b4 = new byte[len];
				int pos4 = 0;
				write(b4,intToBytes(len),pos4);
				pos4+=IntLength;
				b4[pos4] = (byte) tmp;
				pos4+=ByteLength;
				write(b4,hexStringToBytes(rlo.token),pos4);
				pos4+=TokenLength;
				write(b4,(rlo.uname).getBytes(),pos4);
				pos4+=(rlo.uname).length();
				b4[pos4] = 0;
				pos4+=ByteLength;
				out.write(b4);
				break;
			}
			out.flush();
			DataInputStream in = new DataInputStream(client.getInputStream());
			Message msg = new Message();
			int outlen = in.readInt();
			int type = in.readUnsignedByte();
			switch (type) {
			case Login:
				int status = in.readUnsignedByte();
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
				int status1 = in.readUnsignedByte();
				ResUpdate rchkupd = new ResUpdate(status1);
				msg.obj = rchkupd;
				msg.what = Update;
				recall.sendMessage(msg);
				break;
			case Location:
				int status2 = in.readUnsignedByte();
				int n = 0;
				outlen-=(IntLength+ByteLength+ByteLength);
				Vector<Rlocation> tmpv = new Vector<Rlocation>();
				while(outlen > 0) {
					int tid = in.readInt();
					double lat = in.readDouble();
					double lot = in.readDouble();
					tmpv.add(new Rlocation(tid,lat,lot));
					outlen -= (IntLength+DoubleLength+DoubleLength);
					n++;
				}
				ResLocation rlocin = new ResLocation(n,status2,tmpv);
				msg.obj = rlocin;
				msg.what = Location;
				recall.sendMessage(msg);
				break;
			case UserInfo:
				int status3 = in.readUnsignedByte();
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
				ResUserInfo resus = new ResUserInfo(status3,u,com,sec,s);
				msg.obj = resus;
				msg.what = UserInfo;
				recall.sendMessage(msg);
				break;
			case Logout:
				int status4 = in.readUnsignedByte();
				ResLogout rlogout = new ResLogout(status4);
				msg.obj = rlogout;
				msg.what = Logout;
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
	
	private static byte[] doubleToBytes(double d) {
		byte[] b = new byte[8];
		String str = Double.toHexString(d);
		b = hexStringToBytes(str);
		return b;
	}
	
	private static void write(byte[] s,byte[] w,int l) {
		
		for(int i=0;i<w.length;i++){
			s[i+l] = w[i];
		}
	}

}
