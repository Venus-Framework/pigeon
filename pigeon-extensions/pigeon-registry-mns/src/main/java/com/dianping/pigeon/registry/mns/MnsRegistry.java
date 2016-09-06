package com.dianping.pigeon.registry.mns;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.util.Constants;
import com.dianping.pigeon.registry.util.HeartBeatSupport;
import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.exception.ServiceNotifyException;
import com.dianping.pigeon.remoting.provider.publish.ServicePublisher;
import com.dianping.pigeon.util.VersionUtils;
import com.google.common.collect.Maps;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.inf.octo.mns.sentinel.CustomizedManager;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGService;
import com.sankuai.sgagent.thrift.model.ServiceDetail;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.StringUtils;
import com.dianping.pigeon.log.Logger;
import org.apache.thrift.TException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by chenchongze on 16/5/25.
 */
public class MnsRegistry implements Registry {

    private final Logger logger = LoggerLoader.getLogger(getClass());

    private Properties properties;

    private final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    private static final JacksonSerializer jacksonSerializer = new JacksonSerializer();

    private final MnsChangeListenerManager
            mnsChangeListenerManager = MnsChangeListenerManager.INSTANCE;

    public static final int WEIGHT_DEFAULT = 1;

    private static final Map<String, String> hostRemoteAppkeyMapping = MnsUtils.getHostRemoteAppkeyMapping();

    private volatile boolean inited = false;
    private volatile boolean enable = true;

    @Override
    public void init(Properties properties) {
        this.properties = properties;
        if (!inited) {
            synchronized (this) {
                if (!inited) {
                    String specifySgAgent = configManager
                            .getStringValue("pigeon.mns.sgagent.customized.address", "");

                    if (StringUtils.isNotBlank(specifySgAgent)) {
                        CustomizedManager.setCustomizedSGAgents(specifySgAgent);
                    }

                    if (!checkAppExist()) {
                        enable = false;
                        logger.warn("can not find APPKEY: [" + configManager.getAppName()
                                + "] in mns, set mns registry to DISABLED!");
                    }

                    inited = true;
                }
            }
        }
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

    private boolean checkAppExist() {
        String checkAppExistUri = configManager.getStringValue("pigeon.registry.mns.checkexist.api")
                .replace("{APPKEY}", configManager.getAppName());
        HttpClient httpClient = getHttpClient();
        GetMethod getMethod = null;
        String response = null;
        logger.debug("check appkey exist uri: " + checkAppExistUri);
        try {
            getMethod = new GetMethod(checkAppExistUri);
            httpClient.executeMethod(getMethod);
            if (getMethod.getStatusCode() >= 300) {
                throw new IllegalStateException("Did not receive successful HTTP response: status code = "
                        + getMethod.getStatusCode() + ", status message = [" + getMethod.getStatusText() + "]");
            }
            InputStream inputStream = getMethod.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String str = null;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            response = sb.toString();
            br.close();
        } catch (Throwable t) {
            logger.error("failed to get result while call uri: " + checkAppExistUri, t);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }

        CheckResult checkResult = new CheckResult();

        if (StringUtils.isNotBlank(response)) {
            try {
                checkResult = (CheckResult) jacksonSerializer.toObject(CheckResult.class, response);
            } catch (Throwable t) {
                logger.error("failed to deserialize result!", t);
            }
        }

        return checkResult.getData();
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
        return getServiceAddress(remoteAppkey, serviceName, group, fallbackDefaultGroup, true);
    }

    @Override
    public void registerService(String serviceName, String group, String serviceAddress, int weight) throws RegistryException {
        if (!checkSupport(serviceName, group)) {
            return ;
        }

        SGService sgService = new SGService();
        sgService.setAppkey(configManager.getAppName());
        sgService.setVersion(VersionUtils.VERSION);

        ProviderConfig providerConfig = ServicePublisher.getServiceConfig(serviceName);
        boolean isSupport = providerConfig.isSupported();

        Map<String, ServiceDetail> serviceDetailMap = Maps.newHashMap();
        serviceDetailMap.put(serviceName, new ServiceDetail(isSupport));
        sgService.setServiceInfo(serviceDetailMap);

        int index = serviceAddress.lastIndexOf(":");
        try {
            String ip = serviceAddress.substring(0, index);
            String port = serviceAddress.substring(index + 1);
            sgService.setIp(ip);
            sgService.setPort(Integer.valueOf(port));
        } catch (Throwable e) {
            throw new RegistryException("error service address: " + serviceAddress, e);
        }

        sgService.setWeight(MnsUtils.getMtthriftWeight(weight));
        sgService.setFweight(MnsUtils.getMtthriftFWeight(weight));
        sgService.setStatus(MnsUtils.getMtthriftStatus(weight));
        sgService.setHeartbeatSupport(HeartBeatSupport.BOTH.getValue());

        sgService.setProtocol("thrift");
        sgService.setLastUpdateTime((int) (System.currentTimeMillis() / 1000));

        try {
            MnsInvoker.registServiceWithCmd(MnsUtils.UPT_CMD_ADD, sgService);
            logger.info("register provider on mns: " + sgService);
        } catch (TException e) {
            throw new RegistryException("failed to register service: " + serviceName, e);
        }
    }

    @Override
    public void unregisterService(String serviceName, String serviceAddress) throws RegistryException {
        unregisterService(serviceName, null, serviceAddress);
    }

    @Override
    public void unregisterService(String serviceName, String group, String serviceAddress) throws RegistryException {
        if (!checkSupport(serviceName, group)) {
            return ;
        }

        SGService sgService = new SGService();
        sgService.setAppkey(configManager.getAppName());
        sgService.setVersion(VersionUtils.VERSION);

        ProviderConfig providerConfig = ServicePublisher.getServiceConfig(serviceName);
        boolean isSupport = providerConfig.isSupported();
        Map<String, ServiceDetail> serviceDetailMap = Maps.newHashMap();
        serviceDetailMap.put(serviceName, new ServiceDetail(isSupport));
        sgService.setServiceInfo(serviceDetailMap);
        sgService.setHeartbeatSupport(HeartBeatSupport.BOTH.getValue());

        int index = serviceAddress.lastIndexOf(":");
        try {
            String ip = serviceAddress.substring(0, index);
            String port = serviceAddress.substring(index + 1);
            sgService.setIp(ip);
            sgService.setPort(Integer.valueOf(port));
        } catch (Throwable e) {
            throw new RegistryException("error service address: " + serviceAddress, e);
        }

        try {
            MnsInvoker.registServiceWithCmd(MnsUtils.UPT_CMD_DEL, sgService);
            logger.info("unregister provider on mns: " + sgService);
        } catch (TException e) {
            throw new RegistryException("failed to unregister service: " + serviceName, e);
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
        try {
            String remoteAppkey = getRemoteAppkeyMapping(serverAddress);

            if (StringUtils.isNotBlank(remoteAppkey)) {
                SGService sgService = getSGService(remoteAppkey, null, serverAddress);
                return MnsUtils.getWeight(sgService.getStatus());
            }

            throw new RegistryException("failed to get weight for " + serverAddress);
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
        try {
            String remoteAppkey = getRemoteAppkeyMapping(serverAddress);

            if (StringUtils.isNotBlank(remoteAppkey)) {
                SGService sgService = getSGService(remoteAppkey, null, serverAddress);
                return sgService.getAppkey();
            }

            throw new RegistryException("failed to get app for " + serverAddress);
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
        try {
            String remoteAppkey = getRemoteAppkeyMapping(serverAddress);

            if (StringUtils.isNotBlank(remoteAppkey)) {
                SGService sgService = getSGService(remoteAppkey, null, serverAddress);
                return sgService.getVersion();
            }

            throw new RegistryException("failed to get version for " + serverAddress);
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
        if (!checkSupport(serviceName, null)) {
            return false;
        }

        SGService sgService = getSGService(null, serviceName, serviceAddress);
        ServiceDetail serviceDetail = sgService.getServiceInfo().get(serviceName);

        if(serviceDetail != null) {
            return serviceDetail.isUnifiedProto();
        }

        // 判断是否是新版mtthrift服务节点
        if (VersionUtils.isThriftSupported(sgService.getVersion())) {
            return true;
        }

        throw new RegistryException("service detail not existed for " + serviceAddress + "#" + serviceName);
    }

    /**
     * for provider
     * @param serverAddress
     * @param weight
     * @throws RegistryException
     */
    @Override
    public void setServerWeight(String serverAddress, int weight) throws RegistryException {
        SGService sgService = null;
        String remoteAppkey = configManager.getAppName();

        try {
            sgService = getSGService(remoteAppkey, null, serverAddress);
        } catch (RegistryException e) {
            logger.warn("failed to set server weight! no sg_service found of " + serverAddress);
            return ;
        }

        sgService.setWeight(MnsUtils.getMtthriftWeight(weight));
        sgService.setFweight(MnsUtils.getMtthriftFWeight(weight));
        int status = MnsUtils.getMtthriftStatus(weight);
        sgService.setStatus(status);
        sgService.setServiceInfo(null);
        sgService.setAppkey(remoteAppkey);
        sgService.setVersion(VersionUtils.VERSION);
        sgService.setHeartbeatSupport(HeartBeatSupport.BOTH.getValue());

        try {
            MnsInvoker.registServiceWithCmd(MnsUtils.UPT_CMD_ADD, sgService);
            logger.info("update provider's status: " + sgService);
        } catch (TException e) {
            throw new RegistryException("failed to update host weight: " + serverAddress, e);
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
        // keep blank is enough
    }

    /**
     * for provider
     * @param serviceAddress
     * @param serviceName
     * @throws RegistryException
     */
    @Override
    public void unregisterSupportNewProtocol(String serviceAddress, String serviceName,
                                             boolean support) throws RegistryException {
        //keep blank is enough
    }

    /**
     * for provider
     * @param serverAddress
     * @param app
     */
    @Override
    public void setServerApp(String serverAddress, String app) {
        // keep blank is enough
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
        if (!checkSupport(serviceName, null)) {
            throw new RegistryException("mns does not support pigeon @HTTP@ service!");
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
        return getName() + ": NULL";
    }

    @Override
    public byte getServerHeartBeatSupport(String serviceAddress) throws RegistryException {
        try {
            String remoteAppkey = getRemoteAppkeyMapping(serviceAddress);

            if (StringUtils.isNotBlank(remoteAppkey)) {
                SGService sgService = getSGService(remoteAppkey, null, serviceAddress);
                return sgService.getHeartbeatSupport();
            }

        } catch (Throwable e) {
            logger.error("failed to get server heartbeat support for " + serviceAddress);
        }

        return HeartBeatSupport.BOTH.getValue();
    }

    @Override
    public List<String> getChildren(String key) throws RegistryException {
        throw new RegistryException("unsupported interface in registry: " + getName());
    }

    @Override
    public void setServerService(String serviceName, String group, String hosts) throws RegistryException {
        if (!checkSupport(serviceName, group)) {
            return ;
        }

        // 管理端接口，待定
    }

    @Override
    public void delServerService(String serviceName, String group) throws RegistryException {
        if (!checkSupport(serviceName, group)) {
            return ;
        }

        // 管理端接口，待定
    }

    @Override
    public void setHostsWeight(String serviceName, String group, String hosts, int weight) throws RegistryException {
        if (!checkSupport(serviceName, group)) {
            return ;
        }

        for (String host : hosts.split(",")) {
            SGService sgService = getSGService(null, serviceName, host);
            sgService.setStatus(MnsUtils.getMtthriftStatus(weight));
            sgService.setWeight(MnsUtils.getMtthriftWeight(weight));
            sgService.setFweight(MnsUtils.getMtthriftFWeight(weight));
            sgService.setServiceInfo(null);
            sgService.setHeartbeatSupport(HeartBeatSupport.BOTH.getValue());

            try {
                MnsInvoker.registServiceWithCmd(MnsUtils.UPT_CMD_ADD, sgService);
                logger.info("update provider's status: " + sgService);
            } catch (TException e) {
                //todo 管理端这里抛异常的处理要打磨一下
                throw new RegistryException("failed to update host weight: " + host, e);
            }
        }

    }

    @Override
    public String getServiceAddress(String remoteAppkey, String serviceName, String group, boolean fallbackDefaultGroup, boolean needListener) throws RegistryException {
        String result = "";

        if (!checkSupport(serviceName, group)) {
            return result;
        }

        ProtocolRequest protocolRequest = new ProtocolRequest();
        protocolRequest.setProtocol("thrift");
        protocolRequest.setLocalAppkey(configManager.getAppName());
        protocolRequest.setServiceName(serviceName);
        protocolRequest.setRemoteAppkey(remoteAppkey);
        List<SGService> sgServices = MnsInvoker.getServiceList(protocolRequest);

        // 添加listener，注意去重
        if (needListener) {
            mnsChangeListenerManager.registerListener(protocolRequest);
        }

        for (SGService sgService : sgServices) {
            // 剔除掉octo的旧服务端
            if (MnsUtils.checkVersion(sgService.getVersion())) {
                String host = sgService.getIp() + ":" + sgService.getPort();
                result += host + ",";
                String remoteAppkeyReal = sgService.getAppkey();

                if (remoteAppkeyReal == null) {
                    remoteAppkeyReal = "";
                }

                hostRemoteAppkeyMapping.put(host, remoteAppkeyReal);
            }
        }

        return result;
    }

    @Override
    public String getServiceAddress(String serviceName, String group, boolean fallbackDefaultGroup, boolean needListener) throws RegistryException {
        return getServiceAddress(null, serviceName, group, fallbackDefaultGroup, needListener);
    }

    @Override
    public void updateHeartBeat(String serviceAddress, Long heartBeatTimeMillis) {
        // keep blank
    }

    @Override
    public void deleteHeartBeat(String serviceAddress) {
        // keep blank
    }

    private String getRemoteAppkeyMapping(String serverAddress) {
        String app = hostRemoteAppkeyMapping.get(serverAddress);

        if (StringUtils.isBlank(app) && serverAddress.startsWith(configManager.getLocalIp())) {
            app = configManager.getAppName();
        }

        return StringUtils.isNotBlank(app) ? app : "";
    }

    private boolean checkSupport(String serviceName, String group) {
        if (StringUtils.isNotBlank(serviceName) && serviceName.startsWith("@HTTP@")) {
            logger.warn("mns does not support @HTTP@ service!");
            return false;
        }

        if(StringUtils.isNotBlank(group)) {
            logger.warn("mns does not support group feature!");
            return false;
        }

        return true;
    }

    private static class CheckResult {

        private boolean data = false;
        private boolean isSuccess = false;

        public boolean getData() {
            return data;
        }

        public void setData(boolean data) {
            this.data = data;
        }

        public boolean getIsSuccess() {
            return isSuccess;
        }

        public void setIsSuccess(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }
    }

    private HttpClient getHttpClient() {
        HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setMaxTotalConnections(300);
        params.setDefaultMaxConnectionsPerHost(50);
        params.setConnectionTimeout(3000);
        params.setTcpNoDelay(true);
        params.setSoTimeout(3000);
        params.setStaleCheckingEnabled(true);
        connectionManager.setParams(params);
        HttpClient httpClient = new HttpClient();
        httpClient.setHttpConnectionManager(connectionManager);

        return httpClient;
    }
}
