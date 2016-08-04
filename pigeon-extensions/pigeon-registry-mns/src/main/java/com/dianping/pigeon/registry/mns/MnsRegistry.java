package com.dianping.pigeon.registry.mns;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.publish.ServicePublisher;
import com.dianping.pigeon.util.VersionUtils;
import com.google.common.collect.Maps;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGService;
import com.sankuai.sgagent.thrift.model.ServiceDetail;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TException;

import java.util.*;

/**
 * Created by chenchongze on 16/5/25.
 */
public class MnsRegistry implements Registry {

    private Logger logger = LoggerLoader.getLogger(getClass());

    private Properties properties;

    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private Map<String, String> hostRemoteAppkeyMapping = Maps.newConcurrentMap();

    public static final int WEIGHT_DEFAULT = 1;

    private volatile boolean inited = false;

    @Override
    public void init(Properties properties) {
        this.properties = properties;
        if (!inited) {
            synchronized (this) {
                if (!inited) {

                    inited = true;
                }
            }
        }
    }

    @Override
    public String getName() {
        return Constants.REGISTRY_MNS_NAME;
    }

    @Override
    public String getValue(String key) {
        return properties.getProperty(key);
    }

    @Override
    public String getServiceAddress(String serviceName) throws RegistryException {
        return getServiceAddress(null, serviceName, null, false);
    }

    @Override
    public String getServiceAddress(String serviceName, String group) throws RegistryException {
        return getServiceAddress(null, serviceName, group, false);
    }

    @Override
    public String getServiceAddress(String serviceName, String group, boolean fallbackDefaultGroup) throws RegistryException {
        return getServiceAddress(null, serviceName, group, fallbackDefaultGroup);
    }

    @Override
    public String getServiceAddress(String remoteAppkey, String serviceName, String group, boolean fallbackDefaultGroup) throws RegistryException {
        String result = "";

        if (StringUtils.isNotBlank(serviceName) && serviceName.startsWith("@HTTP@")) {
            logger.warn("mns is not support pigeon @HTTP@ service!");
            return result;
        }

        if(StringUtils.isNotBlank(group)) {
            logger.warn("mns is not support pigeon group feature!");
            return result;
        }

        ProtocolRequest protocolRequest = new ProtocolRequest();
        protocolRequest.setProtocol("thrift");
        protocolRequest.setLocalAppkey(configManager.getAppName());
        protocolRequest.setServiceName(serviceName);
        protocolRequest.setRemoteAppkey(remoteAppkey);
        List<SGService> sgServices = MnsInvoker.getServiceList(protocolRequest);

        logger.info("remoteAppkey: " + remoteAppkey + ", serviceName: " + serviceName);

        for (SGService sgService : sgServices) {
            if (MnsUtils.getPigeonWeight(sgService.getStatus(), sgService.getWeight()) > 0) {
                //todo 加入筛选掉octo的旧服务端
                String host = sgService.getIp() + ":" + sgService.getPort();
                result += host +",";
                String remoteAppkeyCache = null;

                if (hostRemoteAppkeyMapping.containsKey(host)) {
                    remoteAppkeyCache = hostRemoteAppkeyMapping.get(host);

                    if (StringUtils.isNotBlank(remoteAppkeyCache)) {
                        remoteAppkeyCache = remoteAppkey;
                        hostRemoteAppkeyMapping.put(host, remoteAppkeyCache);
                    }

                } else {
                    remoteAppkeyCache = remoteAppkey;
                    hostRemoteAppkeyMapping.put(host, remoteAppkeyCache);
                }
            }
        }

        return result;
    }

    @Override
    public void registerService(String serviceName, String group, String serviceAddress, int weight) throws RegistryException {
        if (StringUtils.isNotBlank(serviceName) && serviceName.startsWith("@HTTP@")) {
            logger.warn("mns is not support pigeon @HTTP@ service!");
            return ;
        }

        if (StringUtils.isNotBlank(group)) {// 暂时忽略group
            logger.warn("mns is not support pigeon group feature!");
            return ;
        }

        SGService sgService = new SGService();

        ProviderConfig providerConfig = ServicePublisher.getServiceConfig(serviceName);
        boolean isSupport = providerConfig.isSupported();

        sgService.setAppkey(configManager.getAppName());
        Map<String, ServiceDetail> serviceDetailMap = Maps.newHashMap();
        serviceDetailMap.put(serviceName, new ServiceDetail(isSupport));
        sgService.setServiceInfo(serviceDetailMap);
        sgService.setStatus(MnsUtils.getMtthriftStatus(weight));

        int index = serviceAddress.lastIndexOf(":");
        try {
            String ip = serviceAddress.substring(0, index);
            String port = serviceAddress.substring(index + 1);
            sgService.setIp(ip);
            sgService.setPort(Integer.valueOf(port));
        } catch (Throwable e) {
            throw new RegistryException("error serviceAddress: " + serviceAddress, e);
        }

        sgService.setWeight(10);
        sgService.setFweight(10.d);

        sgService.setVersion(VersionUtils.VERSION);
        sgService.setProtocol("thrift");
        sgService.setLastUpdateTime((int) (System.currentTimeMillis() / 1000));

        // 下面这两个有用吗？
        sgService.setRole(0);
        // 慢启动
        /*String extend = clusterManager +
                Consts.vbar + "slowStartSeconds" + Consts.colon + slowStartSeconds;
        sgService.setExtend(extend);*/

        try {
            MnsInvoker.registServicewithCmd(MnsUtils.UPT_CMD_ADD, sgService);
            logger.info("registerProviderOnMns: " + sgService);
        } catch (TException e) {
            throw new RegistryException("error while register service: " + serviceName, e);
        }
    }

    @Override
    public void unregisterService(String serviceName, String serviceAddress) throws RegistryException {
        unregisterService(serviceName, null, serviceAddress);
    }

    @Override
    public void unregisterService(String serviceName, String group, String serviceAddress) throws RegistryException {
        if (StringUtils.isNotBlank(serviceName) && serviceName.startsWith("@HTTP@")) {
            logger.warn("mns is not support pigeon @HTTP@ service!");
            return ;
        }

        if (StringUtils.isNotBlank(group)) {// 暂时忽略group
            logger.warn("mns is not support pigeon group feature!");
            return ;
        }

        SGService sgService = new SGService();
        sgService.setAppkey(configManager.getAppName());

        ProviderConfig providerConfig = ServicePublisher.getServiceConfig(serviceName);
        boolean isSupport = providerConfig.isSupported();
        Map<String, ServiceDetail> serviceDetailMap = Maps.newHashMap();
        serviceDetailMap.put(serviceName, new ServiceDetail(isSupport));
        sgService.setServiceInfo(serviceDetailMap);
        // 设置status为禁用
        //sgService.setStatus(MnsUtils.getMtthriftStatus(0));

        int index = serviceAddress.lastIndexOf(":");
        try {
            String ip = serviceAddress.substring(0, index);
            String port = serviceAddress.substring(index + 1);
            sgService.setIp(ip);
            sgService.setPort(Integer.valueOf(port));
        } catch (Throwable e) {
            throw new RegistryException("error serviceAddress: " + serviceAddress, e);
        }

        try {
            MnsInvoker.registServicewithCmd(MnsUtils.UPT_CMD_DEL, sgService);
            logger.info("unregisterProviderOnMns: " + sgService);
        } catch (TException e) {
            throw new RegistryException("error while unregister service: " + serviceName, e);
        }
    }

    /**
     * for invoker
     * @param serverAddress
     * @return
     * @throws RegistryException
     */
    @Override
    public int getServerWeight(String serverAddress) throws RegistryException {
        //todo 北京侧的最小单位不是serverAddress
        //todo client建立连接时候，带上host和remoteAppkey的映射
        //todo host ---> remoteAppkey
        //todo 存在的问题，高度依赖于连接client时序，是否一定是先建立client连接
        try {
            String remoteAppkey = hostRemoteAppkeyMapping.get(serverAddress);

            if (StringUtils.isNotBlank(remoteAppkey)) {
                SGService sgService = getSGService(remoteAppkey, null, serverAddress);
                return MnsUtils.getPigeonWeight(sgService.getStatus(), sgService.getWeight());
            }

            return WEIGHT_DEFAULT;
        } catch (Throwable e) {
            logger.error("failed to get weight for " + serverAddress);
            throw new RegistryException(e);
        }
    }

    /**
     * for invoker
     * @param serverAddress
     * @return
     * @throws RegistryException
     */
    @Override
    public String getServerApp(String serverAddress) throws RegistryException {
        //todo 参考getServerWeight
        try {
            String remoteAppkey = hostRemoteAppkeyMapping.get(serverAddress);

            if (StringUtils.isNotBlank(remoteAppkey)) {
                SGService sgService = getSGService(remoteAppkey, null, serverAddress);
                return sgService.getAppkey();
            }

            return "";
        } catch (Throwable e) {
            logger.error("failed to get app for " + serverAddress);
            throw new RegistryException(e);
        }
    }

    /**
     * for invoker
     * @param serverAddress
     * @return
     * @throws RegistryException
     */
    @Override
    public String getServerVersion(String serverAddress) throws RegistryException {
        //todo 参考getServerWeight
        try {
            String remoteAppkey = hostRemoteAppkeyMapping.get(serverAddress);

            if (StringUtils.isNotBlank(remoteAppkey)) {
                SGService sgService = getSGService(remoteAppkey, null, serverAddress);
                return sgService.getVersion();
            }

            return "";
        } catch (Throwable e) {
            logger.error("failed to get version for " + serverAddress);
            throw new RegistryException(e);
        }
    }

    /**
     * for invoker
     * @param serviceAddress
     * @return
     * @throws RegistryException
     */
    @Override
    public boolean isSupportNewProtocol(String serviceAddress) throws RegistryException {
        return VersionUtils.isThriftSupported(getServerVersion(serviceAddress));
    }

    /**
     * for invoker
     * @param serviceAddress
     * @param serviceName
     * @return
     * @throws RegistryException
     */
    @Override
    public boolean isSupportNewProtocol(String serviceAddress, String serviceName) throws RegistryException {
        if (StringUtils.isNotBlank(serviceName) && serviceName.startsWith("@HTTP@")) {
            logger.warn("mns is not support pigeon @HTTP@ service!");
            return false;
        }

        SGService sgService = getSGService(null, serviceName, serviceAddress);
        ServiceDetail serviceDetail = sgService.getServiceInfo().get(serviceName);

        if(serviceDetail != null) {
            return serviceDetail.isUnifiedProto();
        }

        throw new RegistryException("serviceDetail not existed for " + serviceAddress + "#" + serviceName);
    }

    /**
     * for provider
     * @param serverAddress
     * @param weight
     * @throws RegistryException
     */
    @Override
    public void setServerWeight(String serverAddress, int weight) throws RegistryException {
        SGService sgService = getSGService(configManager.getAppName(), null, serverAddress);
        sgService.setStatus(MnsUtils.getMtthriftStatus(weight));

        try {
            MnsInvoker.registerService(sgService);
            logger.info("update provider's status: " + sgService);
        } catch (TException e) {
            throw new RegistryException("error while update host weight: " + serverAddress, e);
        }
    }

    /**
     * for provider
     * @param serviceAddress
     * @param serviceName
     * @param support
     * @throws RegistryException
     */
    @Override
    public void setSupportNewProtocol(String serviceAddress, String serviceName, boolean support) throws RegistryException {
        if (StringUtils.isNotBlank(serviceName) && serviceName.startsWith("@HTTP@")) {
            logger.warn("mns is not support pigeon @HTTP@ service!");
            return ;
        }

        SGService sgService = getSGService(configManager.getAppName(), serviceName, serviceAddress);
        ServiceDetail serviceDetail = sgService.getServiceInfo().get(serviceName);

        if (serviceDetail != null) {
            serviceDetail.setUnifiedProto(support);
        } else {
            serviceDetail = new ServiceDetail(support);
            sgService.getServiceInfo().put(serviceName, serviceDetail);
        }

        try {
            MnsInvoker.registerService(sgService);
            logger.info("update provider's protocol: " + serviceAddress + "#" + serviceName + ": " + support);
        } catch (TException e) {
            throw new RegistryException("error while update service protocol: " + serviceAddress + "#" + serviceName,  e);
        }
    }

    /**
     * for provider
     * @param serviceAddress
     * @param serviceName
     * @throws RegistryException
     */
    @Override
    public void unregisterSupportNewProtocol(String serviceAddress, String serviceName) throws RegistryException {
        setSupportNewProtocol(serviceAddress, serviceName, false);
    }

    /**
     * for provider
     * @param serverAddress
     * @param app
     */
    @Override
    public void setServerApp(String serverAddress, String app) {
        // keep blank is enough
        /*try {
            SGService sgService = getSGService(configManager.getAppName(), null, serverAddress);
            sgService.setAppkey(configManager.getAppName());
            MnsInvoker.registerService(sgService);
            logger.info("update provider's appkey: " + sgService);
        } catch (Throwable e) {
            logger.error("failed to set app of " + serverAddress + " to " + app);
        }*/
    }

    /**
     * for provider
     * @param serverAddress
     */
    @Override
    public void unregisterServerApp(String serverAddress) {
        // keep blank is enough
    }

    /**
     * for provider
     * @param serverAddress
     * @param version
     */
    @Override
    public void setServerVersion(String serverAddress, String version) {
        // keep blank is enough
        /*try {
            SGService sgService = getSGService(configManager.getAppName(), null, serverAddress);
            sgService.setVersion(version);
            MnsInvoker.registerService(sgService);
            logger.info("update provider's version: " + sgService);
        } catch (Throwable e) {
            logger.error("failed to set version of " + serverAddress + " to " + version);
        }*/
    }

    /**
     * for provider
     * @param serverAddress
     */
    @Override
    public void unregisterServerVersion(String serverAddress) {
        // keep blank is enough
    }

    private SGService getSGService(String remoteAppkey, String serviceName, String serverAddress) throws RegistryException {
        if (StringUtils.isNotBlank(serviceName) && serviceName.startsWith("@HTTP@")) {
            logger.warn("mns is not support pigeon @HTTP@ service!");
            return new SGService();
        }

        ProtocolRequest protocolRequest = new ProtocolRequest();
        protocolRequest.setProtocol("thrift");
        protocolRequest.setLocalAppkey(configManager.getAppName());
        protocolRequest.setServiceName(serviceName);
        protocolRequest.setRemoteAppkey(remoteAppkey);
        List<SGService> sgServices = MnsInvoker.getServiceList(protocolRequest);

        if (sgServices != null && sgServices.size() > 0) {
            for (SGService sgService : sgServices) {
                String host = sgService.getIp() + ":" + sgService.getPort();
                if(host.equals(serverAddress)) {
                    return sgService;
                }
            }
        }

        throw new RegistryException("SGService not found: " + remoteAppkey + ", " + serviceName + ", " + serverAddress);
    }

    @Override
    public String getStatistics() {
        return getName();
    }

    @Override
    public List<String> getChildren(String key) throws RegistryException {
        throw new RegistryException("unsupported interface in registry: " + getName());
    }

    @Override
    public void setServerService(String serviceName, String group, String hosts) throws RegistryException {
        if (StringUtils.isNotBlank(serviceName) && serviceName.startsWith("@HTTP@")) {
            logger.warn("mns is not support pigeon @HTTP@ service!");
            return ;
        }

        if(StringUtils.isNotBlank(group)) {
            logger.warn("mns is not support pigeon group feature!");
            return ;
        }

        // 管理端接口，待定
    }

    @Override
    public void delServerService(String serviceName, String group) throws RegistryException {
        if (StringUtils.isNotBlank(serviceName) && serviceName.startsWith("@HTTP@")) {
            logger.warn("mns is not support pigeon @HTTP@ service!");
            return ;
        }

        if(StringUtils.isNotBlank(group)) {
            logger.warn("mns is not support pigeon group feature!");
            return ;
        }

        // 管理端接口，待定
    }

    @Override
    public void setHostsWeight(String serviceName, String group, String hosts, int weight) throws RegistryException {
        if (StringUtils.isNotBlank(serviceName) && serviceName.startsWith("@HTTP@")) {
            logger.warn("mns is not support pigeon @HTTP@ service!");
            return ;
        }

        if(StringUtils.isNotBlank(group)) {
            logger.warn("mns is not support pigeon group feature!");
            return ;
        }

        for (String host : hosts.split(",")) {
            SGService sgService = getSGService(null, serviceName, host);
            sgService.setStatus(MnsUtils.getMtthriftStatus(weight));

            try {
                MnsInvoker.registerService(sgService);
                logger.info("update provider's status: " + sgService);
            } catch (TException e) {
                //todo 管理端这里抛异常的处理要打磨一下
                throw new RegistryException("error while update host weight: " + host, e);
            }
        }

    }

    @Override
    public void updateHeartBeat(String serviceAddress, Long heartBeatTimeMillis) {
        // keep blank
    }

    @Override
    public void deleteHeartBeat(String serviceAddress) {
        // keep blank
    }

    public static void main(String[] args) {
        String serviceName = "#fdsd";
        String[] appkeyAndUrl = serviceName.split("#");
        if(appkeyAndUrl.length == 2) {
            //注意判空
            System.out.println(appkeyAndUrl[0]);
            System.out.println(appkeyAndUrl[1]);
        } else if (appkeyAndUrl.length == 1) {
            System.out.println(appkeyAndUrl[0]);
        } else {
            throw new RuntimeException();
        }
    }
}
