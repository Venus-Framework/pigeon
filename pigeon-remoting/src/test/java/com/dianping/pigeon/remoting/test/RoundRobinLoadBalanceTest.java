package com.dianping.pigeon.remoting.test;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class RoundRobinLoadBalanceTest extends LoadBalanceBaseTest {
	Logger logger = LoggerLoader.getLogger(RoundRobinLoadBalanceTest.class);

	@Test
	public void test() {
//		final String expect = "AABABCABC";
//		RoundRobinLoadBalance wrr = new RoundRobinLoadBalance();
//
//		StringBuilder sb = new StringBuilder();
//		int len = expect.length();
//		for (int i = 0; i < len; i++) {
//			sb.append(client[(wrr.roundRobin(weights))]);
//		}
//		logger.info(sb.toString());
//		Assert.assertEquals(expect, sb.toString());
	}
}
