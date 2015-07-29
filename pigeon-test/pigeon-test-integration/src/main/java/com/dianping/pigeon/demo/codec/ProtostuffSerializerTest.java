package com.dianping.pigeon.demo.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.remoting.common.codec.protostuff.ProtostuffSerializer;
import com.google.common.io.Files;

public class ProtostuffSerializerTest {

	@Test
	public void test1() throws IOException {
		ProtostuffSerializer serializer = new ProtostuffSerializer();
		User user = User.getUser();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		serializer.serializeRequest(os, user);

		byte[] bytes = os.toByteArray();
		Files.write(bytes, new File("/data/appdatas/user.protostuff"));
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		// ByteArrayInputStream is = new ByteArrayInputStream(
		// Files.toByteArray(new File("/data/appdatas/user.protostuff")));
		User2 user2 = (User2) serializer.deserializeObject(is, User2.class);
		// System.out.println(user2);
		Assert.assertEquals(user.getName(), user2.getName2());
		Assert.assertEquals(user.getAge(), user2.getAge2());
	}
}
