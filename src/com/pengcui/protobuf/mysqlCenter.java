package com.pengcui.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.ndktools.javamd5.core.MD5;
import mysqlCenter.protoBuf.mysqlBuf.Buf;

public class mysqlCenter extends Thread {
	
	ServerSocket server = null;
	Socket client = null;
	
	public void startServer(){
		try {
			server = new ServerSocket(23647);
			System.out.println("port listening on: 23647");
		} catch (IOException e) {
			System.out.println("start server err: " + e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	public void run(){
		startServer();
		while(true){
			try{
				client = server.accept();
				new clientThread(client).start();
			}catch(Exception e){
				System.out.println("accpet client err: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws ClassNotFoundException{
		Class.forName("com.mysql.jdbc.Driver");
		new mysqlCenter().start();
	}
	
	class clientThread extends Thread{
		
		Buf buf = null;		
		private Socket client;
		private int readCount, len;
		private byte[] lenByte = new byte[4], byteArr;
		private Connection con = null;
		private MD5 md5 = null;
		private String sendStr = "";
		
		public clientThread(Socket clientt){
			client = clientt;
		}
		
		public void run(){
			System.out.println("get a client");
			try{
				md5 = new MD5();
				con = DriverManager.getConnection("jdbc:mysql://localhost:3306/fpeng", "fpeng_wxuser", "332aBC48");
				client.setKeepAlive(true);
				client.setTcpNoDelay(true);
				OutputStream os = client.getOutputStream();
				InputStream is = client.getInputStream();
				while(client.isConnected() && !client.isClosed()){
					System.out.println("receiver");
					while(is.available() < 4) sleep(100);
					readCount = 0;
					while(readCount < 4){
						readCount += is.read(lenByte, readCount, 4-readCount);
					}
					
					len = byte2int(lenByte);
					if(len == 0) break;
					byteArr = new byte[len];
					while(is.available() < len) sleep(100);
					readCount = 0;
					while(readCount < len){
						readCount += is.read(byteArr, readCount, len-readCount);
					}
					
					buf = Buf.parseFrom(byteArr);
					
					switch(buf.getOperateType()){
					case "regist38679":
						sendStr = addUser(buf.getUserName(), buf.getPassword(), con, md5);
						break;
					case "login":
						sendStr = login(buf.getUserName(), buf.getPassword(), con, md5, client.getInetAddress().getHostName());
						break;
					case "logout":
						logout(buf.getUserName(), con, client.getInetAddress().getHostName());
						sendStr = "退出成功";
						break;
					case "changePassword":
						sendStr = changePassword(buf.getUserName(), buf.getPassword(), con, md5);
						break;
					default:
						break;
					}
					os.write(getSendByte(sendStr.getBytes()));
				}
			}catch(Exception e){
				
				System.out.println("process client err: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public static String addUser(String userName, String password, final Connection con, MD5 md5){
		System.out.println("start regist: " + userName);
		PreparedStatement preStat = null;
		ResultSet res = null;
		try {
			//check username is regist
			if(haveUserName(userName, con)){
				return "此用户名已注册";
			}
			
			preStat = con.prepareStatement("insert into multiWxUser set userName=?, password=?, createTime=?");
			preStat.setString(1, userName);
			preStat.setString(2, md5.getMD5ofStr(password));
			preStat.setLong(3, System.currentTimeMillis()/1000);
			preStat.execute();
			preStat.close();
			return "注册成功";
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return "未知错误";
		}
	}
	
	public String login(String userName, String password, final Connection con, MD5 md5, String ip){
		ResultSet res = null;
		try{
			PreparedStatement preStat = null;
			preStat = con.prepareStatement("select * from multiWxUser where userName = ?");
			preStat.setString(1, userName);
			res = preStat.executeQuery();
			if(res.next()){
				if(res.getBoolean("loginEd")){
					return "此帐号已登陆";
				}
				if(res.getString("password").equals(md5.getMD5ofStr(password))){
					preStat = con.prepareStatement("update multiWxUser set loginEd = ? where userName = ?");
					preStat.setBoolean(1, true);
					preStat.setString(2, userName);
					preStat.execute();
					
					preStat = con.prepareStatement("insert into loginRecord set loginIp = ?, loginEd = ?, loginTime = ?");
					preStat.setString(1, ip);
					preStat.setBoolean(2, true);
					preStat.setLong(3, System.currentTimeMillis()/1000);
					preStat.execute();
					return "登陆成功";
				}else{
					return "密码错误";
				}
			}else{
				return "用户名错误";
			}
		}catch(Exception e){
			System.out.println("login err: " + e.getMessage());
			e.printStackTrace();
			return "未知错误";
		}
	}
	
	public void logout(String userName, final Connection con, String ip){
		PreparedStatement preStat = null;
		try{
			preStat = con.prepareStatement("update multiWxUser set loginEd = ? where userName = ?");
			preStat.setBoolean(1, false);
			preStat.setString(2, userName);
			preStat.execute();
			
			preStat = con.prepareStatement("insert into loginRecord set loginIp = ?, loginEd = ?, loginTime = ?");
			preStat.setString(1, ip);
			preStat.setBoolean(2, false);
			preStat.setLong(3, System.currentTimeMillis()/1000);
			preStat.execute();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("logout err: " + e.getMessage());
		}
	}
	
	private static String changePassword(String userName, String password, final Connection con, MD5 md5){
		if(haveUserName(userName, con)){
			PreparedStatement preStat = null;
			try{
				preStat = con.prepareStatement("update multiWxUser set password=? where userName=?");
				preStat.setString(1, md5.getMD5ofStr(password));
				preStat.setString(2, userName);
				preStat.executeUpdate();
				return "修改成功";
			}catch(Exception e){
				e.printStackTrace();
				return "修改失败";
			}
		}else{
			return "用户名错误";
		}
	}
	
	private static boolean haveUserName(String userName, final Connection con){
		PreparedStatement preStat;
		ResultSet res = null;
		try{
			preStat = con.prepareStatement("select * from multiWxUser where userName = ?");
			preStat.setString(1, userName);
			res = preStat.executeQuery();
			if(res.next()){
				return true;
			}else{
				return false;
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public static byte[] getSendByte(byte[] bufByte){
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
