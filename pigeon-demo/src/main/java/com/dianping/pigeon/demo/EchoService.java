/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

public interface EchoService {

	String echo(String msg);

	long now();

	void addUser(User<?> user);

	List<User<?>> findUsers(int count);
	
	String test(Map<User, String> values);

	enum Grade {
		low, high
	}

	public static class User<T> implements Serializable {
		Gender gender;
		int id;
		String name;
		String email;
		String address;
		int age;
		BigDecimal amount;
		T[] userProfile;

		// int count;
		//
		// public int getCount() {
		// return count;
		// }
		//
		// public void setCount(int count) {
		// this.count = count;
		// }

		public Gender getGender() {
			return gender;
		}

		public void setGender(Gender gender) {
			this.gender = gender;
		}

		public T[] getUserProfile() {
			return userProfile;
		}

		public void setUserProfile(T[] userProfile) {
			this.userProfile = userProfile;
		}

		public BigDecimal getAmount() {
			return amount;
		}

		public void setAmount(BigDecimal amount) {
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

	public static class UserProfile implements Serializable {
		private String fav;
		private BigDecimal v;

		public BigDecimal getV() {
			return v;
		}

		public void setV(BigDecimal v) {
			this.v = v;
		}

		public String getFav() {
			return fav;
		}

		public void setFav(String fav) {
			this.fav = fav;
		}

	}

	public enum Gender {
		AUTO, MALE, FEMALE;
	}
}
