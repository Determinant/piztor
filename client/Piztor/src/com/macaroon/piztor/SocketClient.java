package com.macaroon.piztor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
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
			out.writeByte(tmp);
			switch (tmp) {
			case 0:
				ReqLogin rau = (ReqLogin) req;
				String id = rau.user;
				String pa = rau.pass;
				out.writeBytes(id + "\0" + pa);
				break;
			case 2:
				ReqUpdate rup = (ReqUpdate) req;
				int tk2 = rup.token;
				double slat = rup.lat;
				double slot = rup.lot;
				out.writeInt(tk2);
				out.writeDouble(slat);
				out.writeDouble(slot);
				break;
			case 3:
				ReqLocation ras = (ReqLocation) req;
				int tk3 = ras.token;
				int gid = ras.gid;
				out.writeInt(tk3);
				out.writeInt(gid);
				break;
			}
			out.flush();
			client.shutdownOutput();
			DataInputStream in = new DataInputStream(client.getInputStream());
			Message msg = new Message();
			int type = in.readUnsignedByte();
			switch (type) {
			case 0:
				int id = in.readInt();
				int status = in.readUnsignedByte();
				ResLogin rchklogin = new ResLogin(id,status);
				msg.obj = rchklogin;
				msg.what = 0;
				recall.sendMessage(msg);
				break;
			case 2:
				int status1 = in.readUnsignedByte();
				ResUpdate rchkupd = new ResUpdate(status1);
				msg.obj = rchkupd;
				msg.what = 1;
				recall.sendMessage(msg);
				break;
			case 3:
				int n = in.readInt();
				Vector<Rlocation> tmpv = new Vector<Rlocation>();
				for (int i = 1; i <= n; i++) {
					int tid = in.readInt();
					double lat = in.readDouble();
					double lot = in.readDouble();
					tmpv.add(new Rlocation(tid,lat,lot));
				}
				ResLocation rlocin = new ResLocation(n,tmpv);
				msg.obj = rlocin;
				msg.what = 3;
				recall.sendMessage(msg);
				break;
			}

		} catch (IOException e) {
			throw e;
		}
	}

	public void closeSocket() {
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
