package com.dianping.pigeon.test.client.loadbalance;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.invoker.route.balance.RoundRobinLoadBalance;
import com.dianping.pigeon.remoting.invoker.route.balance.RoundRobinLoadBalance.RoundRobinBalancer;

public class RoundRobinLoadBalanceTest {
	Logger logger = LoggerLoader.getLogger(RoundRobinLoadBalanceTest.class);

	@Test
	public void test1() {
		String[] client = { "A", "B", "C" };
		int[] weights = { 4, 3, 2 };
		final String expect = "AABABCABC";
		RoundRobinLoadBalance wrr = new RoundRobinLoadBalance();

		StringBuilder sb = new StringBuilder();
		int len = expect.length();
		RoundRobinBalancer balancer = new RoundRobinBalancer();
		for (int i = 0; i < len; i++) {
			sb.append(client[(wrr.roundRobin(balancer, weights))]);
		}
		logger.info(sb.toString());
		Assert.assertEquals(expect, sb.toString());
	}

}
