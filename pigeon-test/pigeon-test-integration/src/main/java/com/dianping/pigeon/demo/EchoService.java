/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public interface EchoService {

	String echo(String input);

	long now();

	void addUser(User user);

	List<User> findUsers(int count);

	enum Grade {
		low, high
	}

	public static class User implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2912743152637604246L;
		int id;
		String name;
		String email;
		String address;
		int age;
		Long amount;
//		int count;
//
//		public int getCount() {
//			return count;
//		}
//
//		public void setCount(int count) {
//			this.count = count;
//		}

		public Long getAmount() {
			return amount;
		}

		public void setAmount(Long amount) {
			this.amount = amount;
		}

		public User() {
		}

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

		public String toString() {
			return ToStringBuilder.reflectionToString(this).toString();
		}
	}
}
