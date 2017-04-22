package com.pengcui.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import mysqlCenter.protoBuf.mysqlBuf.Buf;

public class refTest {
	public static void main(String[] args) {
		/*AbClass ac = new AbClass(0);
		AbClass ac1 = new AbClass(1);
		String s = "123";
		byte[] b = new byte[2];
		
		Connection connection = null;
		try {
			connection = DriverManager.getConnection("jdbc:mysql://116.62.38.80:3306/fpeng", "fpeng_wxuser", "l;asd*(*@`lgd%^&*~~");
			Statement statement = connection.createStatement();
			statement.execute("create table if not exists wx2(content text, path int, userName text primary key, contentType int, contentTime text)");
			ResultSet res = statement.executeQuery("select * from wx1");
			System.out.println(res.getRow());
			while(res.next()){
				System.out.println(res.getString("friendName"));
				System.out.println("content");
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CloseableHttpClient hc = HttpClients.createDefault();
		HttpEntity he = null;
		StringEntity strEntity = null;
		
		HttpPost hp = new HttpPost("http://openapi.tuling123.com/openapi/api/v2");
		JSONObject jo = new JSONObject();*/
		try {
			Buf.Builder buf = Buf.newBuilder();
			buf.clear();
			buf.setOperateType("logout");
			//buf.setOperateType("regist38679");
			buf.setUserName("pengT");
			buf.setPassword("321");
			
			Socket client = new Socket("116.62.38.80", 23647);
			OutputStream os = client.getOutputStream();
			InputStream is = client.getInputStream();
			int readCount, len;
			byte[] lenByte = new byte[4], byteArr;
			
			os.write(getSendByte(buf.build().toByteArray()));
			while(is.available() < 4) Thread.sleep(100);
			readCount = 0;
			while(readCount < 4){
				readCount += is.read(lenByte, readCount, 4-readCount);
			}
			
			len = byte2int(lenByte);
			if(len == 0) return;
			byteArr = new byte[len];
			while(is.available() < len) Thread.sleep(100);
			readCount = 0;
			while(readCount < len){
				readCount += is.read(byteArr, readCount, len-readCount);
			}
			System.out.println(new String(byteArr));
			
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
