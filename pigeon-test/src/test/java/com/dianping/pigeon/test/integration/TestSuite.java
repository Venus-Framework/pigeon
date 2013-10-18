/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.integration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.pigeon.test.integration.call.CallbackEchoServiceTest;
import com.dianping.pigeon.test.integration.call.DefaultEchoServiceTest;
import com.dianping.pigeon.test.integration.call.DefaultSyncEchoServiceTest;
import com.dianping.pigeon.test.integration.call.FutureEchoServiceTest;
import com.dianping.pigeon.test.integration.call.OnewayEchoServiceTest;
import com.dianping.pigeon.test.integration.cluster.FailfastClusterEchoServiceTest;
import com.dianping.pigeon.test.integration.cluster.FailoverClusterEchoServiceTest;
import com.dianping.pigeon.test.integration.cluster.FailsafeClusterEchoServiceTest;
import com.dianping.pigeon.test.integration.group.EchoServiceGroupTest;
import com.dianping.pigeon.test.integration.group.GroupNotMatchEchoServiceTest;
import com.dianping.pigeon.test.integration.loadbalance.ConsistentHashLoadbalanceEchoServiceTest;
import com.dianping.pigeon.test.integration.loadbalance.LeastActiveLoadbalanceEchoServiceTest;
import com.dianping.pigeon.test.integration.loadbalance.LeastSuccessLoadbalanceEchoServiceTest;
import com.dianping.pigeon.test.integration.loadbalance.RandomLoadbalanceEchoServiceTest;
import com.dianping.pigeon.test.integration.loadbalance.RoundRobinLoadbalanceEchoServiceTest;
import com.dianping.pigeon.test.integration.serialize.HessianEchoServiceTest;
import com.dianping.pigeon.test.integration.serialize.JavaEchoServiceTest;

/**
 * 
 * 
 * @author xiang.wu
 * @since 2013-10-16
 */
@RunWith(Suite.class)
@SuiteClasses({
	DefaultEchoServiceTest.class,
	CallbackEchoServiceTest.class,
	DefaultSyncEchoServiceTest.class,
	FutureEchoServiceTest.class,
	OnewayEchoServiceTest.class,
	FailfastClusterEchoServiceTest.class,
	FailoverClusterEchoServiceTest.class,
	FailsafeClusterEchoServiceTest.class,
	EchoServiceGroupTest.class,
	GroupNotMatchEchoServiceTest.class,
	ConsistentHashLoadbalanceEchoServiceTest.class,
	LeastActiveLoadbalanceEchoServiceTest.class,
	LeastSuccessLoadbalanceEchoServiceTest.class,
	RandomLoadbalanceEchoServiceTest.class,
	RoundRobinLoadbalanceEchoServiceTest.class,
	DefaultEchoServiceTest.class,
	HessianEchoServiceTest.class,
	JavaEchoServiceTest.class
})
public class TestSuite {

}
