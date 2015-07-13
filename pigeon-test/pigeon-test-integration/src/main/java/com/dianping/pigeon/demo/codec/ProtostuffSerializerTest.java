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
		User user = User.getUser();

		ProtostuffSerializer serializer = new ProtostuffSerializer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		serializer.serializeRequest(os, user);

		byte[] bytes = os.toByteArray();
		Files.write(bytes, new File("/data/appdatas/user.protostuff"));

		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		User2 user2 = (User2)serializer.deserializeObject(is, User2.class);
		System.out.println(user2);
		
		//Assert.assertEquals(user, user2);
	}

}
