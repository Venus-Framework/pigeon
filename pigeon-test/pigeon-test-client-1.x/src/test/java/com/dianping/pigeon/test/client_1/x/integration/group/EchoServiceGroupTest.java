package com.dianping.pigeon.test.client_1.x.integration.group;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.test.client_1.x.AnnotationBaseInvokerTest;
import com.dianping.pigeon.test.client_1.x.PigeonAutoTest;
import com.dianping.pigeon.test.service.EchoService;

public class EchoServiceGroupTest extends AnnotationBaseInvokerTest {

    @PigeonAutoTest(serviceName = "http://service.dianping.com/testService/echoService_1.0.0", group="Mobile")
    public EchoService echoService;

    @Test
    public void test() {
        String echo = echoService.echo("dianping");
        Assert.assertEquals("echo:dianping", echo);
    }

}
