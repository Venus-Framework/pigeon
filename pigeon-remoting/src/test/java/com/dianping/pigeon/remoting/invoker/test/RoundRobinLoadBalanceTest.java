package com.dianping.pigeon.remoting.invoker.test;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.monitor.Log4jLoader;
import com.dianping.pigeon.remoting.invoker.route.balance.RoundRobinLoadBalance;

public class RoundRobinLoadBalanceTest extends LoadBalanceBaseTest {
	Logger logger = Log4jLoader.getLogger(RoundRobinLoadBalanceTest.class);

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
		Assert.assertEquals(expect, sb.toString());
	}
}
