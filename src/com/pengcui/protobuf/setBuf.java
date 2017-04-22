package com.pengcui.protobuf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.JSONObject;
import pengcui.protoBuf.bufClass.Buf;

public class setBuf extends Thread{
	
	ServerSocket server;
	Socket client = null;
	int logFile = 0;
	public setBuf(){
		try {
			//old port is 23645
			server = new ServerSocket(23646);
			System.out.println("start listen on 23646...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run(){
		while(true){
			try {
				client = server.accept();
				logFile++;
				clientThread ct = new clientThread(client, logFile);
				ct.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String[] args){
		new setBuf().start();
	}
	
	class clientThread extends Thread{
		JSONObject jo = null;
		Buf.Builder buf = Buf.newBuilder();
		Socket client = null;
		byte[] byteLen = new byte[4], byteArr;
		int len = 0, readCount, logFi;
		boolean bufSetOk = false;
		
		public clientThread(Socket client, int logF){
			this.client = client;
			logFi = logF; 
		}
		
		public void run(){
			System.out.println("get a client");
			File f = new File("jm"+logFi+"ErrJson.log");
			try {
				FileWriter fw = new FileWriter(f);
				this.client.setKeepAlive(true);
				this.client.setTcpNoDelay(true);
				OutputStream os = this.client.getOutputStream();
				InputStream is = this.client.getInputStream();
				while(this.client.isConnected() && !this.client.isClosed()){
					buf.clear();
					System.out.println("receiver");
					while(is.available() < 4) sleep(100);
					readCount = 0;
					while(readCount < 4){
						readCount += is.read(byteLen, readCount, 4-readCount);
					}
					
					//调试，返回接受到的数据
					os.write(byteLen);
					
					len = byte2int(byteLen);
					if(len == 0) break;
					byteArr = new byte[len];
					while(is.available() < len) sleep(100);
					readCount = 0;
					while(readCount < len){
						readCount += is.read(byteArr, readCount, len-readCount);
					}
					
					//调试，返回接收的数据
					os.write(byteArr);
					os.flush();
					System.out.println(new String(byteArr));
					
					try{
						buf.clear();
						jo = new JSONObject(new String(byteArr));
						buf.setOperateType(jo.getInt("operateType"));
						switch(jo.getInt("operateType")){
						case 0:
							buf.setInputText(jo.getString("inputText")).setIsDel(jo.getBoolean("isDelText"));
							break;
						case 1:
							buf.setGpsStart(jo.getInt("gpsStart"));
							if(jo.getInt("gpsStart") == 1){
								buf.setLongitude(jo.getDouble("longitude")).setLatitude(jo.getDouble("latitude")).setAltitude(jo.getDouble("altitude")).setDelay(jo.getLong("delay"));
							}
							break;
						case 3:
							buf.setUrl(jo.getString("url"));
							break;
						case 4:
							buf.setKeyCode(jo.getInt("keyCode"));
							break;
						case 5:
							buf.setFindOf(jo.getInt("findOf")).setFindStr(jo.getString("findStr")).setIsChild(jo.getBoolean("isChild"));
							if(jo.getBoolean("isChild")){
								buf.setChildStr(jo.getString("childStr")).setChildLayer(jo.getInt("childLayer"));
							}
							break;
						case 6:
							buf.setCmdStr(jo.getString("cmdStr")).setIsRoot(jo.getBoolean("isRoot"));
							break;
						case 7:
							buf.setContactType(jo.getInt("contactType"));
							switch(jo.getInt("contactType")){
							case 1:
								buf.setContactCount(jo.getInt("contactCount")).setContactName(jo.getString("contactName"));
								break;
							case 2:
								buf.setContactCount(jo.getInt("contactCount")).setContactName(jo.getString("contactName")).setContactNum(jo.getString("contactNum"));
								break;
							}
							break;
						case 9:
							buf.setFileName(jo.getString("fileName")).setFileLen(jo.getInt("fileLen"));
							break;
						case 10:
							buf.setWxRun(jo.getBoolean("wxRun"));
							break;
						case 15:
							buf.setRobotApiKey(jo.getString("robotApiKey"));
							break;
						case 17:
							buf.setDlUrl(jo.getString("dlUrl"));
							break;
						}
						bufSetOk = true;
					}catch(Exception e){
						//e.printStackTrace();
						os.write(int2byte(0));
						bufSetOk = false;
						fw.write(new String(byteArr) + "\n");
						fw.flush();
					}
					if(bufSetOk){
						System.out.println("start send buf");
						os.write(getBuf(buf.build().toByteArray()));
					}
					os.flush();
				}
				fw.close();
				is.close();
				os.close();
				client.close();
				System.out.println("stop a client");
			} catch (Exception e) {
			}
		}
	}
	
	public static byte[] getBuf(byte[] bufByte){
		int bufLen = bufByte.length;
		byte[] sendByte = new byte[4+bufLen];
		System.arraycopy(int2byte(bufLen), 0, sendByte, 0, 4);
		System.arraycopy(bufByte, 0, sendByte, 4, bufLen);
		return sendByte;
	}
	
	private static byte[] int2byte(int m){
		byte[] bys = new byte[4];
		for(int i=0; i< 4; i++){
			bys[i] = (byte) ((m>>((i)*8))&0xff);
		}
		return bys;
	}
	
	private static int byte2int(byte[] bys){
		int m = 0;
		for(int i=0; i<4; i++){
			m |= (bys[i]&0xff)<<(8*i);
		}
		return m;
	}
}
