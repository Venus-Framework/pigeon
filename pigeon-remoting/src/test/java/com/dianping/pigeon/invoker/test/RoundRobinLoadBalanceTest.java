package com.dianping.pigeon.invoker.test;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.dianping.pigeon.remoting.invoker.route.balance.RoundRobinLoadBalance;

public class RoundRobinLoadBalanceTest extends LoadBalanceTestBase {
	Logger logger = Logger.getLogger(RoundRobinLoadBalanceTest.class);

	@Test
	public void test() {
		final String expect = "AABABCABC";
		RoundRobinLoadBalance wrr = new RoundRobinLoadBalance();

		StringBuilder sb = new StringBuilder();
		int len = expect.length();
		for (int i = 0; i < len; i++) {
			sb.append(client[(wrr.roundRobin(weights))]);
		}
		logger.info(sb.toString());
		assertEquals(expect, sb.toString());
	}
}
