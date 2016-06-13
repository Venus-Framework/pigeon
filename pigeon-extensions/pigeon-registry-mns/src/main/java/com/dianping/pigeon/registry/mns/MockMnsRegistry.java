package com.dianping.pigeon.registry.mns;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.mns.mock.MnsInvoker;
import com.dianping.pigeon.registry.mns.mock.SGService;
import com.dianping.pigeon.registry.mns.mock.ServiceListRequest;
import com.dianping.pigeon.registry.util.Constants;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TException;

import java.util.List;
import java.util.Properties;

/**
 * Created by chenchongze on 16/5/25.
 */
public class MockMnsRegistry implements Registry {

    private Logger logger = LoggerLoader.getLogger(getClass());

    private Properties properties;

    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    @Override
    public void init(Properties properties) {
        this.properties = properties;
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
        ServiceListRequest serviceListRequest = new ServiceListRequest();
        serviceListRequest.setAppkey(configManager.getAppName());
        serviceListRequest.setServiceName(serviceName);

        List<SGService> services = MnsInvoker.getServiceList(serviceListRequest);
        List<String> serviceHosts = Lists.newArrayList();

        for (SGService service : services) {
            serviceHosts.add(service.getHostId());
        }

        return StringUtils.join(serviceHosts, ",");
    }

    @Override
    public String getServiceAddress(String serviceName, String group) throws RegistryException {
        return getServiceAddress(serviceName);
    }

    @Override
    public String getServiceAddress(String serviceName, String group, boolean fallbackDefaultGroup) throws RegistryException {
        return getServiceAddress(serviceName);
    }

    @Override
    public void registerService(String serviceName, String group, String serviceAddress, int weight) throws RegistryException {
        SGService sgService = new SGService();
        sgService.setAppkey(configManager.getAppName());
        sgService.setServiceName(serviceName);

        int index = serviceAddress.lastIndexOf(":");
        try {
            String ip = serviceAddress.substring(0, index);
            String port = serviceAddress.substring(index + 1);
            sgService.setIp(ip);
            sgService.setPort(Integer.valueOf(port));
        } catch (Throwable e) {
            throw new RegistryException("error serviceAddress: " + serviceAddress, e);
        }

        //todo weight convert
        if (weight <=0) {
            sgService.setStatus(2);
        } else {
            sgService.setWeight(10);
        }

        try {
            MnsInvoker.registerService(sgService);
        } catch (TException e) {
            throw new RegistryException("error while register service: " + serviceName, e);
        }
    }

    @Override
    public void unregisterService(String serviceName, String serviceAddress) throws RegistryException {
        SGService sgService = new SGService();
        sgService.setAppkey(configManager.getAppName());
        sgService.setServiceName(serviceName);

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
            MnsInvoker.unRegisterService(sgService);
        } catch (TException e) {
            throw new RegistryException("error while unregister service: " + serviceName, e);
        }
    }

    @Override
    public void unregisterService(String serviceName, String group, String serviceAddress) throws RegistryException {
        unregisterService(serviceName, serviceAddress);
    }

    @Override
    public int getServerWeight(String serverAddress) throws RegistryException {
        //todo 北京侧的最小单位不是serverAddress

        try {
            return 1;
        } catch (Throwable e) {
            logger.error("failed to get weight for " + serverAddress);
            throw new RegistryException(e);
        }
    }

    @Override
    public List<String> getChildren(String key) throws RegistryException {
        return null;
    }

    @Override
    public void setServerWeight(String serverAddress, int weight) throws RegistryException {

    }

    @Override
    public String getServerApp(String serverAddress) throws RegistryException {
        try {

            return "";
        } catch (Throwable e) {
            logger.error("failed to get app for " + serverAddress);
            throw new RegistryException(e);
        }
    }

    @Override
    public void setServerApp(String serverAddress, String app) {

    }

    @Override
    public void unregisterServerApp(String serverAddress) {

    }

    @Override
    public void setServerVersion(String serverAddress, String version) {

    }

    @Override
    public String getServerVersion(String serverAddress) throws RegistryException {
        try {

            return "";
        } catch (Throwable e) {
            logger.error("failed to get version for " + serverAddress);
            throw new RegistryException(e);
        }
    }

    @Override
    public void unregisterServerVersion(String serverAddress) {

    }

    @Override
    public String getStatistics() {
        return null;
    }

    @Override
    public void setServerService(String serviceName, String group, String hosts) throws RegistryException {

    }

    @Override
    public void delServerService(String serviceName, String group) throws RegistryException {

    }

    @Override
    public void updateHeartBeat(String serviceAddress, Long heartBeatTimeMillis) {

    }

    @Override
    public void deleteHeartBeat(String serviceAddress) {

    }

    @Override
    public boolean isSupportNewProtocol(String serviceAddress, String serviceName) throws RegistryException {
        try {

            return false;

        } catch (Throwable e) {
            logger.error("failed to get protocol:" + serviceName
                    + "of host:" + serviceAddress + ", caused by:" + e.getMessage());
            throw new RegistryException(e);
        }
    }

    @Override
    public void setSupportNewProtocol(String serviceAddress, String serviceName, boolean support) throws RegistryException {

    }

    @Override
    public void unregisterSupportNewProtocol(String serviceAddress, String serviceName) throws RegistryException {

    }

    public static void main(String[] args) {
        String serviceAddress = ":";
        int index = serviceAddress.lastIndexOf(":");
        if (index != -1) {
            String ip = serviceAddress.substring(0, index);
            String port = serviceAddress.substring(index + 1);
            System.out.println(ip);
            System.out.println(port);
            System.out.println("end");
        }

    }
}
