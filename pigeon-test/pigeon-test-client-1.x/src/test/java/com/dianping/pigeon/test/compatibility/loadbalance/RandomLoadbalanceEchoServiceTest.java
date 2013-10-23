/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.compatibility.loadbalance;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.test.AnnotationBaseInvokerTest;
import com.dianping.pigeon.test.PigeonAutoTest;
import com.dianping.pigeon.test.service.EchoService;

public class RandomLoadbalanceEchoServiceTest extends AnnotationBaseInvokerTest {

    @PigeonAutoTest(serviceName = "http://service.dianping.com/testService/echoService_1.0.0", loadbalance = "random")
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
        Assert.assertEquals("Echo: dianping", echo);
    }

}
