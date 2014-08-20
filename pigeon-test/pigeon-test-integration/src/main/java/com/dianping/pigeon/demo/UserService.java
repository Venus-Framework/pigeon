/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public interface UserService {

	User[] getUserDetail(User[] users, boolean withPassword);

	public static class User<T> implements Serializable {
		private String username;
		private String email;
		private String password;
		private List<T> roles;

		public List<T> getRoles() {
			return roles;
		}

		public void setRoles(List<T> roles) {
			this.roles = roles;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

	public static class Role implements Serializable {
		private String roleName;

		public Role(String name) {
			roleName = name;
		}

		public String getRoleName() {
			return roleName;
		}

		public void setRoleName(String roleName) {
			this.roleName = roleName;
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

}
