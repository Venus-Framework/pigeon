package com.dianping.pigeon.registry.mns;

import com.dianping.pigeon.registry.listener.DefaultServiceChangeListener;
import com.dianping.pigeon.registry.listener.ServiceChangeListener;
import com.google.common.collect.Lists;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.inf.octo.mns.listener.IServiceListChangeListener;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenchongze on 16/5/26.
 */
public class MnsEventNotifier {

    private static ServiceChangeListener serviceChangeListener = new DefaultServiceChangeListener();

    private static List<IServiceListChangeListener> serviceListChangeListeners = Lists.newArrayList();

    public static void eventReceived(ProtocolRequest req,
                                     List<SGService> oldList,
                                     List<SGService> newList,
                                     List<SGService> addList,
                                     List<SGService> deletedList,
                                     List<SGService> modifiedList) throws Exception {
        String remoteAppkey = req.getRemoteAppkey();
        String serviceName = req.getServiceName();


        if (newList.size() > 0) { //newList 通知addressChanged
            String hosts = "";

            for (SGService sgService : newList) {
                if (MnsUtils.getPigeonWeight(sgService.getStatus(), sgService.getWeight()) > 0) {
                    hosts += sgService.getIp() + ":" + sgService.getPort() +",";
                }
            }

            List<String[]> hostDetail = MnsUtils.getServiceIpPortList(hosts);
            addressChanged(serviceName, hostDetail);
        }

        if (modifiedList.size() > 0) { //modifiedList 检查修改的字段，通知不同的通知器

        }

        //old本地有缓存，用不到； add delete也暂时用不到

    }

    private static void addressChanged(String serviceName, List<String[]> hostList) {
        serviceChangeListener.onServiceHostChange(serviceName, hostList);
    }

    private static void weightChanged(String host, int weight) {
        serviceChangeListener.onHostWeightChange(host, weight);
    }

    public static void eventReceived(MnsEvent event) throws Exception {

    }

    public static void main(String[] args) {
        ProtocolRequest serviceListRequest = new ProtocolRequest();
        serviceListRequest.setRemoteAppkey("remoteAppkey");
        serviceListRequest.setLocalAppkey("localAppkey");
        serviceListRequest.setProtocol("thrift");
        serviceListRequest.setServiceName("serviceName");
        MnsInvoker.addServiceListener(serviceListRequest, new DefaultServiceListChangeListener());
    }
}
