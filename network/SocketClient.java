package com.macaroon.piztor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Vector;

import android.os.Handler;
import android.os.Message;

public class SocketClient {
	static Socket client;

	public SocketClient(String site, int port) throws UnknownHostException,
			IOException {
		try {
			client = new Socket(site, port);
		} catch (UnknownHostException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
	}

	public void sendMsg(Req req,Handler recall) throws IOException {
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
				len = 4+1+id.length()+1+pa.length()+1;				
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
				len = 4+1+32+name1.length()+1+8+8;				
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
				len = 4+1+32+name2.length()+1+4;				
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
				len = 4+1+32+name3.length()+1+4;				
				out.writeInt(len);
				out.writeByte(tmp);				
				int usid = rus.uid;
				byte[] b3 = hexStringToBytes(tk3);						
				out.write(b3);
				out.writeBytes(name3);
				out.writeByte(0);
				out.writeInt(usid);
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
				int n = in.readInt();
				Vector<Rlocation> tmpv = new Vector<Rlocation>();
				for (int i = 1; i <= n; i++) {
					int tid = in.readInt();
					double lat = in.readDouble();
					double lot = in.readDouble();
					tmpv.add(new Rlocation(tid,lat,lot));
				}
				ResLocation rlocin = new ResLocation(n,status2,tmpv);
				msg.obj = rlocin;
				msg.what = 2;
				recall.sendMessage(msg);
				break;
			case 3:
				int status3 = in.readUnsignedByte();
				ReqUserinfo rus = (ReqUserinfo) req;
				int u = rus.uid;
				int g = 0,s = 0;
				while(outlen>0){
					int typ = in.readUnsignedByte();
					outlen-=1;
					switch(typ){
					case 0:
						g = in.readInt();
						outlen-=4;
						break;
					case 1:
						s = in.readByte();
						outlen-=1;
						break;
					}
				}
				ResUserinfo resus = new ResUserinfo(status3,u,g,s);
				msg.obj = resus;
				msg.what = 3;
				recall.sendMessage(msg);
				break;
			}

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
	
	private static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase(Locale.CHINA);
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
