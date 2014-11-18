package com.dianping.pigeon.test.benchmark;

import java.util.HashMap;
import java.util.Map;

import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;

public class Test {

	private static class User {
		private String name = null;

		public User(String name) {
			this.name = name;
		}
	}

	public static void main(String[] args) {
		Map<User, Double> m = new HashMap<User, Double>();
		User u1 = new User("w");
		m.put(u1, 123.4d);
		m.put(new User("x"), 123.4d);
		JacksonSerializer serializer = new JacksonSerializer();
		String str = serializer.serializeObject(m);
		System.out.println(str);
		System.out.println(serializer.serializeObject(u1));
		
	}

}
