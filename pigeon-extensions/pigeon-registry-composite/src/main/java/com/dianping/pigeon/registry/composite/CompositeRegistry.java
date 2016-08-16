package com.dianping.pigeon.registry.composite;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.util.Constants;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Created by chenchongze on 16/8/15.
 */
public class CompositeRegistry implements Registry {

    private final Logger logger = LoggerLoader.getLogger(getClass());

    private Properties properties;

    private final ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private volatile List<Registry> registryList = Lists.newArrayList();

    private volatile boolean inited = false;

    private static final String KEY_PIGEON_REGISTRY_PREFER = "pigeon.registry.prefer.snapshot";

    @Override
    public void init(Properties properties) {
        this.properties = properties;
        if (!inited) {
            synchronized (this) {
                if (!inited) {
                    try {
                        String registryPreferConfig = configManager.getStringValue(KEY_PIGEON_REGISTRY_PREFER,
                                Constants.REGISTRY_CURATOR_NAME);
                        parseRegistryConfig(registryPreferConfig);

                        for (Registry registry : registryList) {
                            registry.init(properties);
                        }

                        logger.info("composite registry prefer is " + registryPreferConfig);
                        configManager.registerConfigChangeListener(new InnerConfigChangeListener());
                        inited = true;
                    } catch (Throwable t) {
                        logger.error("failed to init composite registry...");
                        throw new RuntimeException(t);
                    }
                }
            }
        }
    }

    private void parseRegistryConfig(String registryPreferConfig) {
        List<Registry> _registryList = Lists.newArrayList();

        for (Registry registry : ExtensionLoader.getExtensionList(Registry.class)) {
            if (!registry.getName().equals(this.getName())) {
                _registryList.add(registry);
            }
        }

        Map<String, Registry> registryMapByName = Maps.newHashMap();
        for (Registry registry : _registryList) {
            registryMapByName.put(registry.getName(), registry);
        }

        List<String> registryPrefer = Arrays.asList(registryPreferConfig.split(","));
        List<Registry> orderedRegistryList = Lists.newArrayList();

        for (String registryName : registryPrefer) {
            if (registryMapByName.containsKey(registryName)) {
                orderedRegistryList.add(registryMapByName.get(registryName));
            } else {
                logger.error("pigeon.registry.prefer config error! no registry: " + registryName);
                return;
            }
        }

        if (orderedRegistryList.size() > 0) {
            registryList = orderedRegistryList;
        } else {
            logger.error("pigeon.registry.prefer config error! registry num is 0!");
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
        String addr = "";

        for (Registry registry : registryList) { // merge registry addr
            try {
                addr = mergeAddress(addr, registry.getServiceAddress(serviceName));
            } catch (Throwable e) {
                logger.warn("failed to get service address from registry: "  + registry.getName());
            }
        }

        return addr;
    }

    @Override
    public String getServiceAddress(String serviceName, String group) throws RegistryException {
        String addr = "";

        for (Registry registry : registryList) { // merge registry addr
            try {
                addr = mergeAddress(addr, registry.getServiceAddress(serviceName, group));
            } catch (Throwable e) {
                logger.warn("failed to get service address from registry: " + registry.getName());
            }
        }

        return addr;
    }

    @Override
    public String getServiceAddress(String serviceName, String group, boolean fallbackDefaultGroup) throws RegistryException {
        String addr = "";

        for (Registry registry : registryList) { // merge registry addr
            try {
                addr = mergeAddress(addr, registry.getServiceAddress(serviceName, group, fallbackDefaultGroup));
            } catch (Throwable e) {
                logger.warn("failed to get service address from registry: " + registry.getName());
            }
        }

        return addr;
    }

    @Override
    public String getServiceAddress(String remoteAppkey, String serviceName, String group, boolean fallbackDefaultGroup) throws RegistryException {
        String addr = "";

        for (Registry registry : registryList) { // merge registry addr
            try {
                addr = mergeAddress(addr, registry.getServiceAddress(remoteAppkey, serviceName, group, fallbackDefaultGroup));
            } catch (Throwable e) {
                logger.warn("failed to get service address from registry: " + registry.getName());
            }
        }

        return addr;
    }

    @Override
    public void registerService(String serviceName, String group, String serviceAddress, int weight) throws RegistryException {
        for (Registry registry : registryList) {
            try {
                registry.registerService(serviceName, group, serviceAddress, weight);
            } catch (Throwable e) {
                logger.warn("failed to register service to registry: " + registry.getName());
            }
        }
    }

    @Override
    public void unregisterService(String serviceName, String serviceAddress) throws RegistryException {
        for (Registry registry : registryList) {
            try {
                registry.unregisterService(serviceName, serviceAddress);
            } catch (Throwable e) {
                logger.warn("failed to unregister service to registry: " + registry.getName());
            }
        }
    }

    @Override
    public void unregisterService(String serviceName, String group, String serviceAddress) throws RegistryException {
        for (Registry registry : registryList) {
            try {
                registry.unregisterService(serviceName, group, serviceAddress);
            } catch (Throwable e) {
                logger.warn("failed to unregister service to registry: " + registry.getName());
            }
        }
    }

    @Override
    public int getServerWeight(String serverAddress) throws RegistryException {
        int weight = Constants.DEFAULT_WEIGHT;
        List<Integer> checkList = Lists.newArrayList();

        for (Registry registry : registryList) {
            try {
                checkList.add(registry.getServerWeight(serverAddress));
            } catch (Throwable e) {
                logger.warn("failed to get weight from registry: " + registry.getName());
            }
        }

        weight = checkValueConsistency(checkList);

        return weight;
    }

    @Override
    public List<String> getChildren(String key) throws RegistryException {
        throw new RegistryException("unsupported interface in registry: " + getName());
    }

    @Override
    public void setServerWeight(String serverAddress, int weight) throws RegistryException {
        for (Registry registry : registryList) {
            try {
                registry.setServerWeight(serverAddress, weight);
            } catch (Throwable e) {
                logger.warn("failed to set weight to registry: " + registry.getName());
            }
        }
    }

    @Override
    public String getServerApp(String serverAddress) {
        String app = "";
        List<String> checkList = Lists.newArrayList();

        for (Registry registry : registryList) {
            try {
                checkList.add(registry.getServerApp(serverAddress));
            } catch (Throwable e) {
                logger.warn("failed to get appname from registry: " + registry.getName());
            }
        }

        app = checkValueConsistency(checkList);

        return app;
    }

    @Override
    public void setServerApp(String serverAddress, String app) {
        for (Registry registry : registryList) {
            try {
                registry.setServerApp(serverAddress, app);
            } catch (Throwable e) {
                logger.warn("failed to set app to registry: " + registry.getName());
            }
        }
    }

    @Override
    public void unregisterServerApp(String serverAddress) {
        for (Registry registry : registryList) {
            try {
                registry.unregisterServerApp(serverAddress);
            } catch (Throwable e) {
                logger.warn("failed to unregister app to registry: " + registry.getName());
            }
        }
    }

    @Override
    public void setServerVersion(String serverAddress, String version) {
        for (Registry registry : registryList) {
            try {
                registry.setServerVersion(serverAddress, version);
            } catch (Throwable e) {
                logger.warn("failed to set version to registry: " + registry.getName());
            }
        }
    }

    @Override
    public String getServerVersion(String serverAddress) {
        String version = "";
        List<String> checkList = Lists.newArrayList();

        for (Registry registry : registryList) {
            try {
                checkList.add(registry.getServerVersion(serverAddress));
            } catch (Throwable e) {
                logger.warn("failed to get version from registry: " + registry.getName());
            }
        }

        version = checkValueConsistency(checkList);

        return version;
    }

    @Override
    public void unregisterServerVersion(String serverAddress) {
        for (Registry registry : registryList) {
            try {
                registry.unregisterServerVersion(serverAddress);
            } catch (Throwable e) {
                logger.warn("failed to unregister version to registry: " + registry.getName());
            }
        }
    }

    @Override
    public String getStatistics() {
        String stats = "";

        for (Registry registry : registryList) {
            stats += registry.getStatistics() + "\n";
        }

        return stats;
    }

    @Override
    public byte getServerHeartBeatSupport(String serviceAddress) throws RegistryException {
        return 0;
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

    private String mergeAddress(String address, String anotherAddress) {
        Set<String> result = Sets.newHashSet();

        if (StringUtils.isNotBlank(address)) {
            result.addAll(Arrays.asList(address.split(",")));
        }

        if (StringUtils.isNotBlank(anotherAddress)) {
            result.addAll(Arrays.asList(anotherAddress.split(",")));
        }

        return StringUtils.join(result, ",");
    }

    private <T> T checkValueConsistency(List<T> checkList) {
        T result = null;

        if(checkList.size() > 0) {
            result = checkList.get(0);
        }

        for (int i = 0; i < checkList.size(); i++) {
            T t = checkList.get(i);

            if (t != null && !t.equals(result)) {
                String errorMsg = "result not same in different registries! index0: "
                        + result + ", index" + i + ": " + t;

                if (configManager.getBooleanValue("pigeon.registry.check.value.consistency.exception", false)) {
                    throw new RuntimeException(errorMsg);
                }

                if (configManager.getBooleanValue(LoggerLoader.KEY_LOG_DEBUG_ENABLE, false)) {
                    logger.warn(errorMsg);
                }

                break;
            }
        }

        return result;
    }

    private class InnerConfigChangeListener implements ConfigChangeListener {

        @Override
        public void onKeyUpdated(String key, String value) {
            if (key.endsWith(KEY_PIGEON_REGISTRY_PREFER)) {
                try {
                    parseRegistryConfig(value);

                    for (Registry registry : registryList) {
                        registry.init(properties);
                    }

                    logger.info("composite registry prefer change to " + value);

                } catch (Throwable t) {
                    logger.error(t);
                }
            }
        }

        @Override
        public void onKeyAdded(String key, String value) {

        }

        @Override
        public void onKeyRemoved(String key) {

        }
    }
}
