package com.dianping.pigeon.demo.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.dianping.pigeon.remoting.common.codec.fst.FstSerializer;
import com.dianping.pigeon.remoting.common.codec.java.JavaSerializer;
import com.google.common.io.Files;

public class JavaSerializerTest {

	@Test
	public void test1() throws IOException {
		JavaSerializer serializer = new JavaSerializer();
		User user = User.getUser();
		long start = System.currentTimeMillis();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		serializer.serializeRequest(os, user);

		byte[] bytes = os.toByteArray();
		Files.write(bytes, new File("/data/appdatas/user.java"));
		//
		// ByteArrayInputStream is = new
		// ByteArrayInputStream(Files.toByteArray(new
		// File("/data/appdatas/user.java")));
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		User user2 = (User) serializer.deserializeRequest(is);
		long end = System.currentTimeMillis();
		System.out.println(end - start);
		// System.out.println(user2);

		// Assert.assertEquals(user, user2);
	}
}
