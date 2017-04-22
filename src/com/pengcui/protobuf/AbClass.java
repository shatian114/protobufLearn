package com.pengcui.protobuf;

public class AbClass {
	
	private static int n;
	
	public AbClass(int i){
		n = i;
	}
	
	public static int getN(){
		return n;
	}
	
	public void setN(int i){
		n = i;
	}
}
