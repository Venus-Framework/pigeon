package com.dianping.pigeon.registry.zookeeper;

import java.util.List;

import org.junit.Test;

public class UtilsTest {

	@Test
	public void test() {
		String serviceAddress = "127.0.0.1:4040,192.168.1.1:4080,s2:e4:w5:r3:y6:s5:4040,";
		List<String[]> results = Utils.getServiceIpPortList(serviceAddress);
		for (String[] result : results) {
			System.out.println(result[0]);
			System.out.println(result[1]);
		}
	}
}
