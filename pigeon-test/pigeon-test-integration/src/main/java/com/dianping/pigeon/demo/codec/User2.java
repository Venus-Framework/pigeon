/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.codec;

public class User2 implements java.io.Serializable {
	private String name;
	private int age;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	static User2 getUser() {
		User2 user = new User2();
		user.setAge(10);
		user.setName("XiaoMing");
		return user;
	}

	public boolean equals(Object obj) {
		User2 an = (User2) obj;
		return this.name.equals(an.getName()) && this.age == an.getAge();
	}

	public String toString() {
		return name + ", " + age;
	}
}
