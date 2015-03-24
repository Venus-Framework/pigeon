package com.dianping.pigeon.remoting.test.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.remoting.common.codec.protostuff.ProtostuffSerializer;

public class ProtostuffSerializerTest {

	public static class User implements Serializable {
		private String username;
		private String email;
		private String password;

		public User(String username) {
			this.username = username;
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
			return username + "," + email + "," + password;
		}

		@Override
		public boolean equals(Object obj) {
			User user = (User) obj;
			return new EqualsBuilder().append(this.username, user.getUsername()).isEquals();
		}
	}

	@Test
	public void test1() {
		User user = new User("wuxiang");

		ProtostuffSerializer serializer = new ProtostuffSerializer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		serializer.serializeRequest(os, user);

		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		Object user2 = serializer.deserializeObject(is, User.class);

		Assert.assertEquals(user, user2);
	}

}
