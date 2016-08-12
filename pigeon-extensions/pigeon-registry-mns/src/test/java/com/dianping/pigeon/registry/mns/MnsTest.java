package com.dianping.pigeon.registry.mns;

import com.dianping.pigeon.util.VersionUtils;
import com.google.common.collect.Maps;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.inf.octo.mns.sentinel.CustomizedManager;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGService;
import com.sankuai.sgagent.thrift.model.ServiceDetail;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenchongze on 16/8/12.
 */
public class MnsTest {

    String customizedSGAgentAddress = "10.66.45.180:5266";
    String testService = "hello.service";
    String protocol = "thrift";
    String localAppkey = "mns-test";
    String remoteAppkey = "com.sankuai.octo.hellopigeon";
    String ip = "10.4.245.3";
    int port = 4040;

    public static void main(String[] args) throws IOException, InterruptedException {
        MnsTest test = new MnsTest();

        test.specifyMns();
        test.testModif();

        for (int i = 0; i < 100; ++i) {
            System.out.println(i);
            Thread.sleep(5000);
            int flag = i % 2;

            if (flag == 0) {
                test.testStatusReg(0);
            } else {
                test.testStatusReg(2);
            }
        }

        test.keepInRead();
    }

    @Test
    public void testModif() {
        ProtocolRequest protocolRequest = new ProtocolRequest();
        protocolRequest.setServiceName(testService)
                .setLocalAppkey(localAppkey)
                .setProtocol(protocol);

        List<SGService> sgServices =  MnsInvoker.getServiceList(protocolRequest);
        for (SGService sgService : sgServices) {
            System.out.println(sgService);
        }
        // IServiceListChangeListener
        System.out.println(MnsInvoker.addServiceListener(protocolRequest,
                new TestServiceListChangeListener()));
    }

    @Test
    public void testStatusReg(int status) {
        Map<String, ServiceDetail> serviceDetailMap = Maps.newHashMap();
        serviceDetailMap.put(testService, new ServiceDetail(true));

        SGService sgService = new SGService();
        sgService.setAppkey(remoteAppkey)
                .setIp(ip).setPort(port).setProtocol(protocol)
                .setLastUpdateTime((int) (System.currentTimeMillis() / 1000))
                .setStatus(status)
                .setVersion(VersionUtils.VERSION)
                .setServiceInfo(serviceDetailMap);

        reg(sgService);
    }

    @Test
    public void mnsReg(String version, int weight, double fweight) {
        Map<String, ServiceDetail> serviceDetailMap = Maps.newHashMap();
        serviceDetailMap.put(testService, new ServiceDetail(true));

        SGService sgService = new SGService();
        sgService.setAppkey(remoteAppkey)
                .setIp(ip).setPort(port).setProtocol(protocol)
                .setLastUpdateTime((int) (System.currentTimeMillis() / 1000))
                .setServiceInfo(serviceDetailMap)
                .setVersion(version)
                .setWeight(weight)
                .setFweight(fweight);

        reg(sgService);
    }

    @Test
    public void mnsUnReg() {
        SGService sgService = new SGService();
        sgService.setAppkey(remoteAppkey);
        Map<String, ServiceDetail> serviceDetailMap = new HashMap<String, ServiceDetail>();
        serviceDetailMap.put(testService, new ServiceDetail(true));
        sgService.setServiceInfo(serviceDetailMap);

        sgService.setIp(ip);
        sgService.setPort(port);

        //sgService.setWeight(0);
        //sgService.setFweight(0.d);

        //sgService.setUnifiedProto(true);
        //sgService.setVersion("2.9.0");
        //sgService.setProtocol("thrift");
        sgService.setLastUpdateTime((int) (System.currentTimeMillis() / 1000));

        try {
            MnsInvoker.unRegisterService(sgService);
            System.out.println("unregisterProviderOnMns: " + sgService);
        } catch (TException e) {
            throw new RuntimeException("error while register service: hello.service", e);
        }
    }

    @Test
    public void mnsGetByServiceNameTest() {
        System.out.println(mnsGetByServiceName());
    }

    private boolean mnsGetByServiceName() {
        ProtocolRequest protocolRequest = new ProtocolRequest();
        protocolRequest.setProtocol(protocol);
        protocolRequest.setLocalAppkey(localAppkey);
        protocolRequest.setServiceName(testService);
        protocolRequest.setRemoteAppkey(remoteAppkey);
        List<SGService> sgServices = MnsInvoker.getServiceList(protocolRequest);

        for (SGService sgService : sgServices) {
            String host = sgService.getIp() + ":" + sgService.getPort();
            if(host.equals(ip + ":" + port)) {
                System.out.println("get registered info: " + sgService);
                return true;
            }

        }

        return false;
    }

    @Test
    public void mnsGet() {
        ProtocolRequest protocolRequest = new ProtocolRequest();
        protocolRequest.setProtocol(protocol);
        protocolRequest.setLocalAppkey(localAppkey);
        protocolRequest.setRemoteAppkey(remoteAppkey);
        List<SGService> sgServices = MnsInvoker.getServiceList(protocolRequest);

        for (SGService sgService : sgServices) {
            System.out.println("get registered info: " + sgService);
        }
    }

    @Before
    public void specifyMns() {

        CustomizedManager.setCustomizedSGAgents(customizedSGAgentAddress);
    }

    @After
    public void keepInRead() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reg(SGService sgService) {
        try {
            MnsInvoker.registServiceWithCmd(1, sgService);
            System.out.println("registerProviderOnMns: " + sgService);
        } catch (TException e) {
            throw new RuntimeException("error while register service: hello.service", e);
        }
    }
}
