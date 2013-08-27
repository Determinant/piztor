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
			case 0:
				ReqLogin rau = (ReqLogin) req;
				String id = rau.user;
				String pa = rau.pass;
				len = IntLength+ByteLength+id.length()+ByteLength+pa.length()+ByteLength;				
				out.writeInt(len);
				out.writeByte(tmp);				
				out.writeBytes(id);
				out.writeByte(0);
				out.writeBytes(pa);
				out.writeByte(0);
				break;
			case 1:
				ReqUpdate rup = (ReqUpdate) req;
				String tk1 = rup.token;
				String name1 = rup.uname;
				len = IntLength+ByteLength+TokenLength+name1.length()+ByteLength+DoubleLength+DoubleLength;				
				out.writeInt(len);				
				out.writeByte(tmp);				
				double slat = rup.lat;
				double slot = rup.lot;
				byte[] b = hexStringToBytes(tk1);						
				out.write(b);
				out.writeBytes(name1);
				out.writeByte(0);
				out.writeDouble(slat);
				out.writeDouble(slot);
				break;
			case 2:
				ReqLocation ras = (ReqLocation) req;
				String tk2 = ras.token;
				String name2 = ras.uname;
				len = IntLength+ByteLength+TokenLength+name2.length()+ByteLength+IntLength;				
				out.writeInt(len);
				out.writeByte(tmp);				
				int gid = ras.gid;
				byte[] b2 = hexStringToBytes(tk2);						
				out.write(b2);
				out.writeBytes(name2);
				out.writeByte(0);
				out.writeInt(gid);
				break;
			case 3:
				ReqUserinfo rus = (ReqUserinfo) req;
				String tk3 = rus.token;
				String name3 = rus.uname;
				len = IntLength+ByteLength+TokenLength+name3.length()+ByteLength+IntLength;				
				out.writeInt(len);
				out.writeByte(tmp);				
				int usid = rus.uid;
				byte[] b3 = hexStringToBytes(tk3);						
				out.write(b3);
				out.writeBytes(name3);
				out.writeByte(0);
				out.writeInt(usid);
				break;
			case 4:
				ReqLogout rlo = (ReqLogout) req;
				String tk4 = rlo.token;
				String name4 = rlo.uname;
				len = IntLength+ByteLength+TokenLength+name4.length()+ByteLength;				
				out.writeInt(len);
				out.writeByte(tmp);				
				byte[] b4 = hexStringToBytes(tk4);						
				out.write(b4);
				out.writeBytes(name4);
				out.writeByte(0);
				break;
			}
			out.flush();
			DataInputStream in = new DataInputStream(client.getInputStream());
			Message msg = new Message();
			int outlen = in.readInt();
			int type = in.readUnsignedByte();
			switch (type) {
			case 0:
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
				msg.what = 0;
				recall.sendMessage(msg);
				break;
			case 1:
				int status1 = in.readUnsignedByte();
				ResUpdate rchkupd = new ResUpdate(status1);
				msg.obj = rchkupd;
				msg.what = 1;
				recall.sendMessage(msg);
				break;
			case 2:
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
				msg.what = 2;
				recall.sendMessage(msg);
				break;
			case 3:
				int status3 = in.readUnsignedByte();
				outlen-=(IntLength+ByteLength+ByteLength);
				ReqUserinfo rus = (ReqUserinfo) req;
				int u = rus.uid;
				int g = 0,s = 0;
				while(outlen > 0) {
					int typ = in.readUnsignedByte();
					outlen-=ByteLength;
					switch(typ){
					case 0:
						g = in.readInt();
						outlen-=IntLength;
						break;
					case 1:
						s = in.readByte();
						outlen-=ByteLength;
						break;
					}
				}
				ResUserinfo resus = new ResUserinfo(status3,u,g,s);
				msg.obj = resus;
				msg.what = 3;
				recall.sendMessage(msg);
				break;
			case 4:
				int status4 = in.readUnsignedByte();
				ResLogout rlogout = new ResLogout(status4);
				msg.obj = rlogout;
				msg.what = 4;
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

}
