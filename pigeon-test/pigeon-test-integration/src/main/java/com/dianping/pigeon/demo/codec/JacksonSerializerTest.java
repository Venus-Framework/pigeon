package com.dianping.pigeon.demo.codec;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;

public class JacksonSerializerTest {

	@Test
	public void test1() {
		User user = User.getUser();

		JacksonSerializer serializer = new JacksonSerializer();
		String str = serializer.serializeObject(user);
		System.out.println(str);

		User user2 = serializer.deserializeObject(User.class, str);
		System.out.println(user2);
		Assert.assertEquals(user, user2);
	}

}
