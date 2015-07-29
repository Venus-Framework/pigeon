package com.dianping.pigeon.demo.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.remoting.common.codec.fst.FstSerializer;
import com.google.common.io.Files;

public class FstSerializerTest {

	@Test
	public void test1() throws IOException {
		FstSerializer serializer = new FstSerializer();
		User user = User.getUser();
		// serializer.registerClass(User.class);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		serializer.serializeRequest(os, user);

		byte[] bytes = os.toByteArray();
		Files.write(bytes, new File("/data/appdatas/user.fst"));
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		// ByteArrayInputStream is = new
		// ByteArrayInputStream(Files.toByteArray(new
		// File("/data/appdatas/user.fst")));
		User user2 = (User) serializer.deserializeObject(is);
		System.out.println(user2);
		Assert.assertEquals(user, user2);
	}
}
