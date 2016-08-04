package com.dianping.pigeon.registry.mns;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.listener.DefaultServiceChangeListener;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.registry.listener.ServiceChangeListener;
import com.google.common.collect.Lists;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.inf.octo.mns.listener.IServiceListChangeListener;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGService;
import com.sankuai.sgagent.thrift.model.serviceDetail;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

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

            // 简单处理，不管有没有变动，所有通知器全部通知
            for (SGService sgService : modifiedList) {
                String host = sgService.getIp() + ":" + sgService.getPort();
                //weight
                int weightNew = MnsUtils.getPigeonWeight(sgService.getStatus(), sgService.getWeight());
                int weightCached = RegistryManager.getInstance().getServiceWeightFromCache(host);

                if(weightNew != weightCached) {
                    weightChanged(host, weightNew);
                }

                //app 即remoteAppKey
                String appNew = sgService.getAppkey();
                String appCached = RegistryManager.getInstance().getReferencedAppFromCache(host);

                if (StringUtils.isNotBlank(appNew) && appNew.equals(appCached)) {
                    appChanged(host, appNew);
                }

                //version 目前貌似没用，暂不管
                String versionNew = sgService.getVersion();
                String versionCached = RegistryManager.getInstance().getReferencedVersionFromCache(host);

                if (StringUtils.isNotBlank(versionNew) && versionNew.equals(versionCached)) {
                    versionChanged(host, versionNew);
                }

                //protocol
                boolean supportedCached = RegistryManager.getInstance().isSupportNewProtocolFromCache(host, serviceName);
                boolean supportedNew = supportedCached;
                serviceDetail serviceDetail = sgService.getServiceInfo().get(serviceName);

                if (serviceDetail != null) {
                    supportedNew = serviceDetail.isUnifiedProto();
                }

                if(supportedNew != supportedCached) {
                    protocolChanged(host, serviceName, supportedNew);
                }


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

    private static void protocolChanged(String host, String serviceName, boolean isSupport) throws RegistryException {
        try {
            // load protocol map and update
            Map<String, Boolean> protocolInfoMap = RegistryManager.getInstance().getProtocolInfoFromCache(host);
            protocolInfoMap.put(serviceName, isSupport);
            logger.info("protocol changed, value " + serviceName + "#" + isSupport);
            RegistryEventListener.serverProtocolChanged(host, protocolInfoMap);
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
