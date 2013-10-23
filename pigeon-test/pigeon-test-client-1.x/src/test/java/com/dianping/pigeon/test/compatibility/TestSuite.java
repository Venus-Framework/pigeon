/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.compatibility;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.pigeon.test.compatibility.call.CallbackEchoServiceTest;
import com.dianping.pigeon.test.compatibility.call.DefaultEchoServiceTest;
import com.dianping.pigeon.test.compatibility.call.DefaultSyncEchoServiceTest;
import com.dianping.pigeon.test.compatibility.call.FutureEchoServiceTest;
import com.dianping.pigeon.test.compatibility.call.OnewayEchoServiceTest;
import com.dianping.pigeon.test.compatibility.cluster.FailfastClusterEchoServiceTest;
import com.dianping.pigeon.test.compatibility.cluster.FailoverClusterEchoServiceTest;
import com.dianping.pigeon.test.compatibility.cluster.FailsafeClusterEchoServiceTest;
import com.dianping.pigeon.test.compatibility.group.EchoServiceGroupTest;
import com.dianping.pigeon.test.compatibility.group.GroupNotMatchEchoServiceTest;
import com.dianping.pigeon.test.compatibility.loadbalance.ConsistentHashLoadbalanceEchoServiceTest;
import com.dianping.pigeon.test.compatibility.loadbalance.LeastActiveLoadbalanceEchoServiceTest;
import com.dianping.pigeon.test.compatibility.loadbalance.LeastSuccessLoadbalanceEchoServiceTest;
import com.dianping.pigeon.test.compatibility.loadbalance.RandomLoadbalanceEchoServiceTest;
import com.dianping.pigeon.test.compatibility.loadbalance.RoundRobinLoadbalanceEchoServiceTest;
import com.dianping.pigeon.test.compatibility.serialize.HessianEchoServiceTest;
import com.dianping.pigeon.test.compatibility.serialize.JavaEchoServiceTest;

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
