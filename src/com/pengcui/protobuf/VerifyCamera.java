package com.pengcui.protobuf;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class VerifyCamera extends Thread{
	
	ServerSocket server;
	Socket client = null;
	int logFile = 0;
	public VerifyCamera(){
		try {
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
		new VerifyCamera().start();
	}
	
	class clientThread extends Thread{
		Socket client = null;
		public clientThread(Socket client, int logF){
			this.client = client;
		}
		
		public void run(){
			try{
				OutputStream os = client.getOutputStream();
				os.write("1".getBytes());
				os.close();
				client.close();
			} catch (Exception e) {
			}
		}
	}
}
