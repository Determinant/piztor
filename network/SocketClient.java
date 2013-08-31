package com.macaroon.piztor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Vector;

import android.os.Handler;
import android.os.Message;



public class SocketClient {
	static Socket client;
	
	static final int ByteLength = 1;
	static final int IntLength = 4;
	static final int DoubleLength = 8;
	static final int TokenLength = 32;

	static final int Login =0;
	static final int Update =1;
	static final int UserInfo =2;
	static final int Subscription =3;
	static final int Logout =4;
	static final int StartPush =5;
	static final int SendMessage =6;
	static final int SetMarker =7;
	static final int SetPassword =8;
	
	static final int ClosePush =-5;
	
	static final int UID =1;
	static final int Uname =2;
	static final int Nname =3;
	static final int Gender =4;
	static final int GroupID =5;
	static final int Latitude =6;
	static final int Longitude =7;
	static final int Level =8;
	
	static final int PasswordFailed =5;
	static final int ServerFetchFailed =4;
	static final int LevelFailed =3;
	static final int StatusFailed = 2;
	static final int TimeOut = 1;
	static final int Success = 0;
	
	public SocketClient(String site, int port, int retime) throws UnknownHostException,
			IOException {
		try {
			client = new Socket();
			client.connect(new InetSocketAddress(site,port), retime);
			client.setSoTimeout(retime);
			//client.setTcpNoDelay(true);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} 
	}

	public int sendMsg(Req req,Handler recall,Handler h) throws IOException,SocketTimeoutException {
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
				Convert.write(b,Convert.intToBytes(len),pos);
				pos+=IntLength;
				b[pos] = (byte) tmp;
				pos+=ByteLength;
				Convert.write(b,rau.user.getBytes(),pos);
				pos+=rau.user.length();
				b[pos] = 0;
				pos+=ByteLength;
				Convert.write(b, rau.pass.getBytes(),pos);
				pos+= rau.pass.length();
				b[pos] = 0;
				pos+=ByteLength;
				out.write(b);
				break;
			case Update:
				ReqUpdate rup = (ReqUpdate) req;
				len = IntLength+ByteLength+TokenLength+(rup.uname).length()+ByteLength+DoubleLength+DoubleLength;				
				b = new byte[len];
				Convert.write(b,Convert.intToBytes(len),pos);
				pos+=IntLength;
				b[pos] = (byte) tmp;
				pos+=ByteLength;
				Convert.write(b,Convert.hexStringToBytes(rup.token),pos);
				pos+=TokenLength;
				Convert.write(b,(rup.uname).getBytes(),pos);
				pos+=(rup.uname).length();
				b[pos] = 0;
				pos+=ByteLength;
				Convert.write(b,Convert.doubleToBytes(rup.latitude),pos);
				pos+=DoubleLength;
				Convert.write(b,Convert.doubleToBytes(rup.longitude),pos);
				pos+=DoubleLength;
				out.write(b);
				break;
			case UserInfo:
				ReqUserInfo rus = (ReqUserInfo) req;
				len = IntLength+ByteLength+TokenLength+rus.uname.length()+ByteLength+ByteLength+ByteLength;				
				b = new byte[len];
				Convert.write(b,Convert.intToBytes(len),pos);
				pos+=IntLength;
				b[pos] = (byte) tmp;
				pos+=ByteLength;
				Convert.write(b,Convert.hexStringToBytes(rus.token),pos);
				pos+=TokenLength;
				Convert.write(b,(rus.uname).getBytes(),pos);
				pos+=(rus.uname).length();
				b[pos] = 0;
				pos+=ByteLength;
				b[pos] = (byte) rus.gid.company;
				pos+=ByteLength;
				b[pos] = (byte) rus.gid.section;
				pos+=ByteLength;
				out.write(b);
				break;
			case Subscription:
				ReqSubscription rsu = (ReqSubscription) req;
				int number = rsu.n;
				len = IntLength+ByteLength+TokenLength+rsu.uname.length()+ByteLength+2*ByteLength*number+ByteLength;				
				b = new byte[len];
				Convert.write(b,Convert.intToBytes(len),pos);
				pos+=IntLength;
				b[pos] = (byte) tmp;
				pos+=ByteLength;
				Convert.write(b,Convert.hexStringToBytes(rsu.token),pos);
				pos+=TokenLength;
				Convert.write(b,(rsu.uname).getBytes(),pos);
				pos+=(rsu.uname).length();
				b[pos] = 0;
				pos+=ByteLength;
				for(int i=0;i<number;i++) {
					b[pos] = (byte) rsu.slist.get(i).company;
					pos+=ByteLength;
					b[pos] = (byte) rsu.slist.get(i).section;
					pos+=ByteLength;
				}	
				b[pos] = 0;
				pos+=ByteLength;
				out.write(b);
				break;
			case Logout:
				ReqLogout rlo = (ReqLogout) req;
				len = IntLength+ByteLength+TokenLength+rlo.uname.length()+ByteLength;				
				b = new byte[len];
				Convert.write(b,Convert.intToBytes(len),pos);
				pos+=IntLength;
				b[pos] = (byte) tmp;
				pos+=ByteLength;
				Convert.write(b,Convert.hexStringToBytes(rlo.token),pos);
				pos+=TokenLength;
				Convert.write(b,(rlo.uname).getBytes(),pos);
				pos+=(rlo.uname).length();
				b[pos] = 0;
				pos+=ByteLength;
				out.write(b);
				break;
			case SendMessage:
				ReqSendMessage rem = (ReqSendMessage) req;
				len = IntLength+ByteLength+TokenLength+rem.uname.length()+ByteLength+rem.msg.length()+ByteLength;	
				b = new byte[len];
				Convert.write(b,Convert.intToBytes(len),pos);
				pos+=IntLength;
				b[pos] = (byte) tmp;
				pos+=ByteLength;
				Convert.write(b,Convert.hexStringToBytes(rem.token),pos);
				pos+=TokenLength;
				Convert.write(b,(rem.uname).getBytes(),pos);
				pos+=(rem.uname).length();
				b[pos] = 0;
				pos+=ByteLength;
				Convert.write(b,rem.msg.getBytes(),pos);
				pos+=rem.msg.length();
				b[pos] = 0;
				pos+=ByteLength;
				out.write(b);
				break;
			case SetMarker:
				ReqSetMarker rsm = (ReqSetMarker) req;
				len = IntLength+ByteLength+TokenLength+(rsm.uname).length()+ByteLength+DoubleLength+DoubleLength+IntLength;				
				b = new byte[len];
				Convert.write(b,Convert.intToBytes(len),pos);
				pos+=IntLength;
				b[pos] = (byte) tmp;
				pos+=ByteLength;
				Convert.write(b,Convert.hexStringToBytes(rsm.token),pos);
				pos+=TokenLength;
				Convert.write(b,(rsm.uname).getBytes(),pos);
				pos+=(rsm.uname).length();
				b[pos] = 0;
				pos+=ByteLength;
				Convert.write(b,Convert.doubleToBytes(rsm.latitude),pos);
				pos+=DoubleLength;
				Convert.write(b,Convert.doubleToBytes(rsm.longitude),pos);
				pos+=DoubleLength;
				Convert.write(b,Convert.intToBytes(rsm.deadline),pos);
				pos+=IntLength;
				out.write(b);
				break;
			case SetPassword:
				ReqSetPassword rsp = (ReqSetPassword) req;
				len = IntLength+ByteLength+TokenLength+(rsp.uname).length()+ByteLength+(rsp.oldpassword).length()+ByteLength+(rsp.newpassword).length()+ByteLength;				
				b = new byte[len];
				Convert.write(b,Convert.intToBytes(len),pos);
				pos+=IntLength;
				b[pos] = (byte) tmp;
				pos+=ByteLength;
				Convert.write(b,Convert.hexStringToBytes(rsp.token),pos);
				pos+=TokenLength;
				Convert.write(b,(rsp.uname).getBytes(),pos);
				pos+=(rsp.uname).length();
				b[pos] = 0;
				pos+=ByteLength;
				Convert.write(b,(rsp.oldpassword).getBytes(),pos);
				pos+=(rsp.oldpassword).length();
				b[pos] = 0;
				pos+=ByteLength;
				Convert.write(b,(rsp.newpassword).getBytes(),pos);
				pos+=(rsp.newpassword).length();
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
			if(status == 1) return StatusFailed;
			if(status == 2) return LevelFailed;
			if(status == 3) return PasswordFailed;
			switch (type) {
			case Login:
				byte[] buffer = new byte[32];
				in.read(buffer);
				String tk = Convert.byteToHexString(buffer);
				outlen-=(IntLength+ByteLength+TokenLength+ByteLength);
				int cnt = 0;
				int uid =0,s =0,l =0;
				String uname ="",nname ="";
				double lat =0.0,lot =0.0;
				RGroup rg = null;
				int i = 0;
				byte[] bu = new byte[200];
				while(cnt < 8) {
					int typ = in.readUnsignedByte();
					outlen-=ByteLength;
					switch(typ){
					case UID:
						uid = in.readInt();
						outlen-=IntLength;
						break;
					case Uname:						
						i = 0;
						while(true) {
							bu[i] = in.readByte();
							if(bu[i] == 0)break;
							i++;
						}
						byte[] wr = new byte[i];
						for(int j=0;j<i;j++){
							wr[j] = bu[j];
						}
						uname = new String(wr);
						outlen-=ByteLength*(i+1);
						break;
					case Nname:
						i = 0;
						while(true) {
							bu[i] = in.readByte();
							if(bu[i] == 0)break;
							i++;
						}
						byte[] wt = new byte[i];
						for(int j=0;j<i;j++){
							wt[j] = bu[j];
						}
						nname = new String(wt);
						outlen-=ByteLength*(i+1);
						break;
					case Gender:
						s = in.readUnsignedByte();
						outlen-=ByteLength;
						break;
					case GroupID:
						int com = in.readUnsignedByte();
						int sec = in.readUnsignedByte();
						rg = new RGroup(com,sec);
						outlen-=ByteLength*2;
						break;
					case Latitude:
						lat = in.readDouble();
						outlen-=DoubleLength;
						break;
					case Longitude:
						lot = in.readDouble();
						outlen-=DoubleLength;
						break;
					case Level:
						l = in.readUnsignedByte();
						outlen-=ByteLength;
						break;
					}
					cnt++;
				}
				RUserInfo r = new RUserInfo(uid,uname,nname,lat,lot,rg,s,l);
				in.readUnsignedByte();
				outlen-=ByteLength;
				int number =0;
				System.out.println("read   "+ outlen);
				Vector<RGroup> vrg = new Vector<RGroup>();
				while(outlen > 1) {
					int com = in.readUnsignedByte();
					int sec = in.readUnsignedByte();
					vrg.add(new RGroup(com,sec));
					outlen-=ByteLength*2;
					number++;
				}
				in.readUnsignedByte();
				msg.obj = new ResLogin(tk, r, vrg, number);
				msg.what = Login;
				recall.sendMessage(msg);
				Vector<String> vs = new Vector<String>();
				vs.add(uname);
				vs.add(tk);
				Message m = new Message();
				m.obj = vs;
				m.what = StartPush;
				h.sendMessage(m);
				break;
			case Update:
				msg.obj = new ResUpdate();
				msg.what = Update;
				recall.sendMessage(msg);
				break;
			case UserInfo:
				outlen-=(IntLength+ByteLength+ByteLength);
				int uid1 =0,s1 =0,l1 =0;
				String uname1 ="",nname1 ="";
				double lat1 =0.0,lot1 =0.0;
				RGroup rg1 = null;
				int k = 0;
				byte[] bn = new byte[200];
				Vector<RUserInfo> v = new Vector<RUserInfo>();
				int n = 0;
				while(outlen > 0) {
					int tmpcnt =0;
					while(tmpcnt < 8) {
						int typ = in.readUnsignedByte();
						outlen-=ByteLength;
						switch(typ){
						case UID:
							uid1 = in.readInt();
							outlen-=IntLength;
							break;
						case Uname:						
							k = 0;
							while(true) {
								bn[k] = in.readByte();
								if(bn[k] == 0)break;
								k++;
							}
							byte[] wr = new byte[k];
							for(int j=0;j<k;j++){
								wr[j] = bn[j];
							}
							uname1 = new String(wr);
							outlen-=ByteLength*(k+1);
							break;
						case Nname:
							k = 0;
							while(true) {
								bn[k] = in.readByte();
								if(bn[k] == 0)break;
								k++;
							}
							byte[] wt = new byte[k];
							for(int j=0;j<k;j++){
								wt[j] = bn[j];
							}
							nname1 = new String(wt);
							outlen-=ByteLength*(k+1);
							break;
						case Gender:
							s1 = in.readUnsignedByte();
							outlen-=ByteLength;
							break;
						case GroupID:
							int com = in.readUnsignedByte();
							int sec = in.readUnsignedByte();
							rg1 = new RGroup(com,sec);
							outlen-=ByteLength*2;
							break;
						case Latitude:
							lat1 = in.readDouble();
							outlen-=DoubleLength;
							break;
						case Longitude:
							lot1 = in.readDouble();
							outlen-=DoubleLength;
							break;
						case Level:
							l1 = in.readUnsignedByte();
							outlen-=ByteLength;
							break;
						}
						tmpcnt++;
					}
					in.readUnsignedByte();
					outlen-=ByteLength;
					v.add(new RUserInfo(uid1,uname1,nname1,lat1,lot1,rg1,s1,l1));
					n++;
				}
				msg.obj = new ResUserInfo(n,v);
				msg.what = UserInfo;
				recall.sendMessage(msg);
				break;
			case Subscription:
				msg.obj = new ResSubscription();
				msg.what = Subscription;
				recall.sendMessage(msg);
				break;
			case Logout:
				msg.obj = new ResLogout();
				msg.what = Logout;
				recall.sendMessage(msg);
				Message ms = new Message();
				ms.what = ClosePush;
				h.sendMessage(ms);
				break;
			case SendMessage:
				msg.obj = new ResSendMessage();
				msg.what = SendMessage;
				recall.sendMessage(msg);
				break;
			case SetMarker:
				msg.obj = new ResSetMarker();
				msg.what = SetMarker;
				recall.sendMessage(msg);
				break;
			case SetPassword:
				msg.obj = new ResSetPassword();
				msg.what = SetPassword;
				recall.sendMessage(msg);
				break;
			}
			return Success;

		} catch (SocketTimeoutException e){
			e.printStackTrace();
			return TimeOut;			
		} catch (EOFException e) {
			e.printStackTrace();
			//return ServerFetchFailed;
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
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



}
