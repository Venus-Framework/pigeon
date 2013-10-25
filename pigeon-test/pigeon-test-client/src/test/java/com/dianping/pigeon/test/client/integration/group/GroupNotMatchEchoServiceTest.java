package com.dianping.pigeon.test.client.integration.group;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.test.client.AnnotationBaseInvokerTest;
import com.dianping.pigeon.test.client.PigeonAutoTest;
import com.dianping.pigeon.test.service.EchoService;

public class GroupNotMatchEchoServiceTest extends AnnotationBaseInvokerTest {

    @PigeonAutoTest(serviceName = "http://service.dianping.com/testService/echoService_1.0.0")
    public EchoService echoService;

    @Test
    public void test() {
        String echo = echoService.echo("dianping");
        Assert.assertEquals("echo:dianping", echo);
    }

}
