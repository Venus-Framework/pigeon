package com.dianping.pigeon.demo.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.remoting.common.codec.hessian.HessianSerializer;
import com.google.common.io.Files;

public class HessianSerializerTest {

	@Test
	public void test1() throws IOException {
		User user = User.getUser();

		HessianSerializer serializer = new HessianSerializer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		serializer.serializeRequest(os, user);

		byte[] bytes = os.toByteArray();
		Files.write(bytes, new File("/data/appdatas/user.hessian"));
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		Object user2 = serializer.deserializeRequest(is);
		System.out.println(((User)user2).getName());
		Assert.assertEquals(user, user2);
	}

}
