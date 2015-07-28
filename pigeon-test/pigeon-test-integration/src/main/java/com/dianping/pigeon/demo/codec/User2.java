/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.codec;

public class User2 implements java.io.Serializable {
	private long age2;
	
	private String uname2;
	private String email;
	
	public long getAge2() {
		return age2;
	}

	public void setAge2(long age2) {
		this.age2 = age2;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUName2() {
		return uname2;
	}

	public void setUName2(String name) {
		this.uname2 = name;
	}

	public boolean equals(Object obj) {
		User2 an = (User2) obj;
		return this.uname2.equals(an.getUName2()) && this.age2 == an.getAge2();
	}

	public String toString() {
		return uname2 + "# " + age2 + "# " + email;
	}
}
