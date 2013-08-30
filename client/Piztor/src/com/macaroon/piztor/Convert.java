package com.macaroon.piztor;

import android.annotation.SuppressLint;

public class Convert {
	
	@SuppressLint("DefaultLocale")
	public static byte[] hexStringToBytes(String hexString) {
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
	
	public static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
	
	public static byte[] intToBytes(int i) {   
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
	
	public static void write(byte[] s,byte[] w,int l) {
		
		for(int i=0;i<w.length;i++){
			s[i+l] = w[i];
		}
	}
	
	public static String byteToHexString(byte[] buffer){
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