package com.dianping.pigeon.remoting.test.codec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;

public class JacksonSerializerTest {

	public static class User implements Serializable {
		private String username;
		private String email;
		private String password;

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
	public void test() {
		User user = new User();
		user.setUsername("wuxiang");
		List<User> users = new ArrayList<User>();
		users.add(user);

		JacksonSerializer serializer = new JacksonSerializer();
		String str = serializer.serializeObject(users);
		System.out.println(str);

		List<User> users2 = serializer.deserializeCollection(str, List.class, User.class);
		System.out.println(users2);
		Assert.assertEquals(users, users2);
	}

}
