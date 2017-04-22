package com.pengcui.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import pengcui.protoBuf.bufClass.Buf;

public class bufTest {
	static Socket client = null;
	private static final int testCount = 0;
	private static String shellStr1 = "mkdir /sdcard/bb", shellStr2 = "rm -rf /sdcard/bb/*";
	private static int instructCode = 0;

	public static void main(String args[]) {
		int readCount = 0;
		 try{
			 System.out.println("start connect phone");
			 Socket client = new Socket("localhost", 10000);
			 System.out.println("phone is connected");
			 OutputStream os = client.getOutputStream();
			 InputStream is = client.getInputStream();
			 Buf.Builder buf = Buf.newBuilder();
			 buf.setOperateType(6);
			 buf.setGView(true).setGText("光");
			 buf.setCmdStr("pwd").setIsChild(false);
			 //buf.setTouchStr("d 0 800 800 50\nc\nu 0\nc\n");
			 buf.setUrl("http://www.baidu.com");
			 buf.setKeyCode(6);
			 buf.setFindOf(1).setFindStr("目录浏览").setIsChild(false);
			 buf.setGpsStart(3).setLongitude(50.6).setLatitude(50.3).setAltitude(50).setDelay(0);
			 for(int i=0; i<10000; i++){
				 switch(instructCode){
				 case 0:
					 buf.setOperateType(6).setCmdStr(shellStr1).setIsRoot(false);
					 break;
				 case 1:
					 buf.setOperateType(6).setCmdStr(shellStr2).setIsRoot(false);
					 break;
				 case 2:
					 buf.setOperateType(6).setCmdStr("dumpsys power | grep mWakefulness").setIsChild(true);
					 break;
				 case 3:
					 buf.setOperateType(4).setKeyCode(3);
					 break;
				 }
				 if(instructCode == 3){
					 instructCode = 0;
				 }else{
					 instructCode++;
				 }
				 byte[] sendByte = buf.build().toByteArray();
				 os.write(int2byte(sendByte.length));
				 os.flush();
				 os.write(sendByte);
				 
				 System.out.println("start read data" + i);
				 
				 Thread.sleep(100);
				 //System.out.println(is.available());
				 
				 
				 /*byte[] metaByte = new byte[4];
				 readCount = 0;
				 while(readCount < 4){
					 readCount += is.read(metaByte, readCount, 4-readCount);
				 }
				 System.out.println(byte2int(metaByte));*/
				 
				 byte[] lenByte = new byte[4];
				 readCount = 0;
				 while(readCount < 4){
					 readCount += is.read(lenByte, readCount, 4-readCount);
				 }
				 //System.out.println("len " + byte2int(lenByte));
				 
				 int bufLen = byte2int(lenByte);
				 byte[] bufByte = new byte[bufLen];
				 readCount = 0;
				 while(readCount < bufLen){
					 readCount += is.read(bufByte, readCount, bufLen-readCount);
				 }
				 System.out.println(new String(bufByte));
			 }
			 
			 is.close();
			 os.close();
			 client.close();
		 }catch(Exception e){
			 e.printStackTrace();
		 }

	bufTestClass[] bufTestO = new bufTestClass[testCount];
	for(int i = 0;i<testCount;i++)
	{
		bufTestO[i] = new bufTestClass(i + "");
		bufTestO[i].start();
		System.out.println(i + "thread start");
	}
}

static class bufTestClass extends Thread {

	private String s1 = "";
	private int readCount, byteLen;
	byte[] lenByte = new byte[4];
	byte[] readByte = null;
	Buf buf = null;

	public bufTestClass(String s) {
		s1 = s;
	}

	public void run() {
		try {
			// System.out.println("start connect phone");
			Socket client = new Socket("localhost", 23645);
			// System.out.println("phone is connected");
			OutputStream os = client.getOutputStream();
			InputStream is = client.getInputStream();

			for (int i = 0; i < testCount; i++) {
				String jsonStr = "{operateType: 6, cmdStr: 'pwd" + s1 + "-" + i + "', isRoot: false}";
				if (i % 5 == 0) {
					jsonStr += "}";
				}
				os.write(int2byte(jsonStr.getBytes().length));
				os.write(jsonStr.getBytes());
				if (i % 5 == 0) {
					break;
				}

				readCount = 0;
				while (readCount < 4) {
					readCount += is.read(lenByte, readCount, 4 - readCount);
				}

				byteLen = byte2int(lenByte);
				readByte = new byte[byteLen];
				readCount = 0;
				while (readCount < byteLen) {
					readCount += is.read(readByte, readCount, byteLen - readCount);
				}
				if (byteLen != 0) {
					buf = Buf.parseFrom(readByte);
					if (!buf.getCmdStr().equals("pwd" + s1 + "-" + i)) {
						System.out.println(s1 + " thread " + i);
					}
				}
			}

			is.close();
			os.close();
			client.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	}

	private static byte[] int2byte(int m) {
		byte[] bys = new byte[4];
		for (int i = 0; i < 4; i++) {
			bys[i] = (byte) ((m >> ((i) * 8)) & 0xff);
		}
		return bys;
	}

	private static int byte2int(byte[] bys) {
		int m = 0;
		for (int i = 0; i < 4; i++) {
			m |= (bys[i] & 0xff) << (8 * i);
		}
		return m;
	}
}
