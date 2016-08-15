package com.dianping.pigeon.registry.composite;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.util.Constants;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Properties;

/**
 * Created by chenchongze on 16/8/15.
 */
public class CompositeRegistry implements Registry {

    private final Logger logger = LoggerLoader.getLogger(getClass());

    private Properties properties;

    private final ConfigManager configManager = ConfigManagerLoader.getConfigManager();

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
        return Constants.REGISTRY_COMPOSITE_NAME;
    }

    @Override
    public String getValue(String key) {
        return properties.getProperty(key);
    }

    @Override
    public String getServiceAddress(String serviceName) throws RegistryException {
        return null;
    }

    @Override
    public String getServiceAddress(String serviceName, String group) throws RegistryException {
        return null;
    }

    @Override
    public String getServiceAddress(String serviceName, String group, boolean fallbackDefaultGroup) throws RegistryException {
        return null;
    }

    @Override
    public String getServiceAddress(String remoteAppkey, String serviceName, String group, boolean fallbackDefaultGroup) throws RegistryException {
        return null;
    }

    @Override
    public void registerService(String serviceName, String group, String serviceAddress, int weight) throws RegistryException {

    }

    @Override
    public void unregisterService(String serviceName, String serviceAddress) throws RegistryException {

    }

    @Override
    public void unregisterService(String serviceName, String group, String serviceAddress) throws RegistryException {

    }

    @Override
    public int getServerWeight(String serverAddress) throws RegistryException {
        return 0;
    }

    @Override
    public List<String> getChildren(String key) throws RegistryException {
        return null;
    }

    @Override
    public void setServerWeight(String serverAddress, int weight) throws RegistryException {

    }

    @Override
    public String getServerApp(String serverAddress) {
        return null;
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
    public String getServerVersion(String serverAddress) {
        return null;
    }

    @Override
    public void unregisterServerVersion(String serverAddress) {

    }

    @Override
    public String getStatistics() {
        return null;
    }

    @Override
    public boolean isSupportNewProtocol(String serviceAddress) throws RegistryException {
        return false;
    }

    @Override
    public boolean isSupportNewProtocol(String serviceAddress, String serviceName) throws RegistryException {
        return false;
    }

    @Override
    public void setSupportNewProtocol(String serviceAddress, String serviceName, boolean support) throws RegistryException {

    }

    @Override
    public void unregisterSupportNewProtocol(String serviceAddress, String serviceName, boolean support) throws RegistryException {

    }

    @Override
    public void updateHeartBeat(String serviceAddress, Long heartBeatTimeMillis) {

    }

    @Override
    public void deleteHeartBeat(String serviceAddress) {

    }

    @Override
    public void setServerService(String serviceName, String group, String hosts) throws RegistryException {

    }

    @Override
    public void delServerService(String serviceName, String group) throws RegistryException {

    }

    @Override
    public void setHostsWeight(String serviceName, String group, String hosts, int weight) throws RegistryException {

    }
}
