package com.example.piztor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketClient {
	static PrintStream cout = System.out;
	static Socket client;

	public SocketClient(String site, int port) throws UnknownHostException,
			IOException {
		try {
			cout.println(site + "   " + port);
			client = new Socket(site, port);
			cout.println("connected successfully!!!");
		} catch (UnknownHostException e) {
			cout.println("unknownhostexception!!");
			throw e;
		} catch (IOException e) {
			cout.println("IOException!!");
			throw e;
		}
	}

	public Myrespond sendMsg(Myrequest req) throws IOException {
		try {
			DataOutputStream out = new DataOutputStream(
					client.getOutputStream());
			int tmp = (Integer) req.contain.get(0);
			out.writeByte(tmp);
			switch (tmp) {
			case 0:
				String id = (String) req.contain.get(1);
				String pass = (String) req.contain.get(2);
				out.writeBytes(id + "\0" + pass);
				break;
			case 1:
				int tk1 = (Integer) req.contain.get(1);
				int acc = (Integer) req.contain.get(2);
				String mess = (String) req.contain.get(3);
				out.writeInt(tk1);
				out.writeInt(acc);
				out.writeBytes(mess);
				break;
			case 2:
				int tk2 = (Integer) req.contain.get(1);
				double slot = (Double) req.contain.get(2);
				double slat = (Double) req.contain.get(3);
				out.writeInt(tk2);
				out.writeDouble(slot);
				out.writeDouble(slat);
				break;
			case 3:
				int tk3 = (Integer) req.contain.get(1);
				int gid = (Integer) req.contain.get(2);
				out.writeInt(tk3);
				out.writeInt(gid);
				break;
			}
			out.flush();
			client.shutdownOutput();
			DataInputStream in = new DataInputStream(client.getInputStream());
			int type = in.readUnsignedByte();
			Myrespond r = new Myrespond();
			switch (type) {
			case 0:
				int id = in.readInt();
				int status = in.readUnsignedByte();
				r.contain.add(0);
				r.contain.add(id);
				r.contain.add(status);
				System.out.println(id);
				break;
			case 1:
				r.contain.add(1);
				// reserved
				break;
			case 2:
				r.contain.add(2);
				// reserved
				break;
			case 3:
				int n = in.readInt();
				r.contain.add(3);
				r.contain.add(n);
				for (int i = 1; i <= n; i++) {
					int tid = in.readInt();
					double lat = in.readDouble();
					double lot = in.readDouble();
					Rmsg a = new Rmsg(tid,lat,lot);
					r.contain.add(a);
				}
				break;
			}
			return r;
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
