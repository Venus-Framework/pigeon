package com.dianping.pigeon.registry.mns;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.listener.DefaultServiceChangeListener;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.registry.listener.ServiceChangeListener;
import com.sankuai.inf.octo.mns.listener.IServiceListChangeListener;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGService;
import com.sankuai.sgagent.thrift.model.ServiceDetail;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Created by chenchongze on 16/5/26.
 */
public class MnsServiceChangeListener implements IServiceListChangeListener {

    private static final Logger logger = LoggerLoader.getLogger(MnsServiceChangeListener.class);

    private static final ServiceChangeListener serviceChangeListener = new DefaultServiceChangeListener();

    private static final Map<String, String> hostRemoteAppkeyMapping = MnsUtils.getHostRemoteAppkeyMapping();

    @Override
    public void changed(ProtocolRequest req,
                                     List<SGService> oldList,
                                     List<SGService> newList,
                                     List<SGService> addList,
                                     List<SGService> deletedList,
                                     List<SGService> modifiedList) {
        String remoteAppkey = req.getRemoteAppkey();
        String serviceName = req.getServiceName();

        logger.info(req + "changed, new count: " + newList.size() + ", modified count: " + modifiedList.size());

        try {
            if (addList.size() > 0 || deletedList.size() > 0) { // 通知addressChanged
                String toAddHosts = "";
                String toDelHosts = "";

                for (SGService sgService : addList) {
                    // 剔除掉octo的旧服务端
                    if (MnsUtils.checkVersion(sgService.getVersion())) {
                        String host = sgService.getIp() + ":" + sgService.getPort();
                        toAddHosts += host + ",";
                        String remoteAppkeyReal = sgService.getAppkey();

                        if (remoteAppkeyReal == null) {
                            remoteAppkeyReal = "";
                        }

                        hostRemoteAppkeyMapping.put(host, remoteAppkeyReal);
                    }
                }

                for (SGService sgService : deletedList) {
                    // 剔除掉octo的旧服务端
                    if (MnsUtils.checkVersion(sgService.getVersion())) {
                        String host = sgService.getIp() + ":" + sgService.getPort();
                        toDelHosts += host + ",";
                        String remoteAppkeyReal = sgService.getAppkey();

                        if (remoteAppkeyReal == null) {
                            remoteAppkeyReal = "";
                        }

                        hostRemoteAppkeyMapping.put(host, remoteAppkeyReal);
                    }
                }

                addressChanged(serviceName, toAddHosts, toDelHosts);
            }

            if (modifiedList.size() > 0) { //modifiedList 检查修改的字段，通知不同的通知器

                // 简单处理，不管有没有变动，所有通知器全部通知
                for (SGService sgService : modifiedList) {
                    String host = sgService.getIp() + ":" + sgService.getPort();
                    //weight
                    int weightNew = MnsUtils.getWeight(sgService.getStatus());
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

                    //version
                    String versionNew = sgService.getVersion();
                    String versionCached = RegistryManager.getInstance().getReferencedVersionFromCache(host);

                    if (StringUtils.isNotBlank(versionNew) && versionNew.equals(versionCached)) {
                        versionChanged(host, versionNew);
                    }

                    //protocol
                    boolean supportedCached = RegistryManager.getInstance().isSupportNewProtocolFromCache(host, serviceName);
                    boolean supportedNew = supportedCached;
                    ServiceDetail serviceDetail = sgService.getServiceInfo().get(serviceName);

                    if (serviceDetail != null) {
                        supportedNew = serviceDetail.isUnifiedProto();
                    }

                    if(supportedNew != supportedCached) {
                        protocolChanged(host, serviceName, supportedNew);
                    }

                    //heartbeat support
                    //todo

                    hostRemoteAppkeyMapping.put(host, appNew);
                }
            }

            //old本地有缓存，用不到； add delete也暂时用不到
        } catch (Throwable e) {
            logger.error("failed to notify service list change...", e);
        }

    }

    private static void addressChanged(String serviceName, String toAddHosts, String toDelHosts)
            throws RegistryException {
        try {
            logger.info("Service address changed, " + serviceName
                    + ", add: " + toAddHosts + " del: " + toDelHosts);
            List<String[]> toAddHostDetail = MnsUtils.getServiceIpPortList(toAddHosts);
            List<String[]> toDelHostDetail = MnsUtils.getServiceIpPortList(toDelHosts);
            serviceChangeListener.onServiceHostChange(serviceName, toAddHostDetail, toDelHostDetail);
        } catch (Throwable e) {
            throw new RegistryException(e);
        }
    }

    private static void addressChanged(String serviceName, String hosts) throws RegistryException {
        try {
            logger.info("Service address changed, " + serviceName +": " + hosts);
            List<String[]> hostDetail = MnsUtils.getServiceIpPortList(hosts);
            serviceChangeListener.onServiceHostChange(serviceName, hostDetail);
        } catch (Throwable e) {
            throw new RegistryException(e);
        }
    }

    private static void weightChanged(String host, int weight) throws RegistryException {
        try {
            logger.info("service weight changed, value " + weight);
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

}
