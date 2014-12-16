/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.benchmark.service;

import java.io.Serializable;
import java.util.List;

public interface EchoService {

	String echo(String input);

	long now();

	List<User> findUsers(int count);

	static class User implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2912743152637604246L;
		int id;
		String name;
		String email;
		String address;
		int age;

		public User(int id, String name, String email, String address, int age) {
			this.id = id;
			this.name = name;
			this.email = email;
			this.address = address;
			this.age = age;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

	}
}
