/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.client.integration.loadbalance;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.test.client.BaseInvokerTest;
import com.dianping.pigeon.test.client.PigeonAutoTest;
import com.dianping.pigeon.test.service.EchoService;

public class LeastActiveLoadbalanceEchoServiceTest extends BaseInvokerTest {

    @PigeonAutoTest(serviceName = "http://service.dianping.com/testService/echoService_1.0.0", loadbalance = "leastActive")
    public EchoService echoService;

    public List<Integer> getPorts() {
    	List<Integer> ports = new ArrayList<Integer>();
    	ports.add(4625);
    	ports.add(4626);
    	ports.add(4627);
    	
    	return ports;
	}
    
    @Test
    public void test() {
        String echo = echoService.echo("dianping");
        Assert.assertEquals("echo:dianping", echo);
    }

}
