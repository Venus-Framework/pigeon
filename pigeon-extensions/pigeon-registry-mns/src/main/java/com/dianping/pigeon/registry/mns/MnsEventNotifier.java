package com.dianping.pigeon.registry.mns;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.listener.DefaultServiceChangeListener;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.registry.listener.ServiceChangeListener;
import com.google.common.collect.Lists;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.inf.octo.mns.listener.IServiceListChangeListener;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGService;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenchongze on 16/5/26.
 */
public class MnsEventNotifier {

    private static Logger logger = LoggerLoader.getLogger(MnsEventNotifier.class);

    private static ServiceChangeListener serviceChangeListener = new DefaultServiceChangeListener();

    private static List<IServiceListChangeListener> serviceListChangeListeners = Lists.newArrayList();

    public static void eventReceived(ProtocolRequest req,
                                     List<SGService> oldList,
                                     List<SGService> newList,
                                     List<SGService> addList,
                                     List<SGService> deletedList,
                                     List<SGService> modifiedList) throws Throwable {
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

            for (SGService sgService : modifiedList) {
                //weight
                sgService.getStatus();

                //app 即remoteAppKey

                //version
                sgService.getVersion();
                //protocol
                sgService.isUnifiedProto();

            }
        }

        //old本地有缓存，用不到； add delete也暂时用不到

    }

    private static void addressChanged(String serviceName, List<String[]> hostList) throws RegistryException {
        try {
            serviceChangeListener.onServiceHostChange(serviceName, hostList);
        } catch (Throwable e) {
            throw new RegistryException(e);
        }
    }

    private static void weightChanged(String host, int weight) throws RegistryException {
        try {
            serviceChangeListener.onHostWeightChange(host, weight);
        } catch (Throwable e) {
            throw new RegistryException(e);
        }
    }

    private static void appChanged(String host, String appName) throws RegistryException {
        try {
            logger.info("app changed, value " + appName);
            RegistryEventListener.serverAppChanged(host, appName);
        } catch (Throwable e) {
            throw new RegistryException(e);
        }
    }

    private static void versionChanged(String host, String version) throws RegistryException {
        try {
            logger.info("version changed, value " + version);
            RegistryEventListener.serverVersionChanged(host, version);
        } catch (Throwable e) {
            throw new RegistryException(e);
        }
    }

    private static void protocolChanged(String host, String serviceName, String protocol) throws RegistryException {
        try {
            //todo load protocol map and update
            Map<String, Boolean> infoMap = new HashMap<>();
            logger.info("protocol changed, value " + serviceName + "#" + protocol);
            RegistryEventListener.serverProtocolChanged(host, infoMap);
        } catch (Throwable e) {
            throw new RegistryException(e);
        }
    }

    public static void eventReceived(MnsEvent event) throws Throwable {

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
