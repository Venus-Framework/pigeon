package com.dianping.pigeon.demo;

import java.util.Date;

import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;

public class A {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JacksonSerializer serializer = new JacksonSerializer();
		Date t = new Date();
		String str = serializer.serializeObject(t);
		System.out.println(str);
		System.out.println(t.getTime());
	}

}
