package com.dianping.pigeon.remoting.invoker.route.region;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.exception.InvalidParameterException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.exception.RegionException;
import com.dianping.pigeon.util.ClassUtils;
import com.dianping.pigeon.util.ServiceUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenchongze on 16/4/14.
 */
public enum RegionPolicyManager {

    INSTANCE;

    private RegionPolicyManager () {
        register(AutoSwitchRegionPolicy.NAME, null, AutoSwitchRegionPolicy.INSTANCE);
        register(WeightBasedRegionPolicy.NAME, null, WeightBasedRegionPolicy.INSTANCE);
        configManager.registerConfigChangeListener(new InnerConfigChangeListener());
        if(enableRegionPolicy) {
            initRegionsConfig();
        } else {
            logger.warn("Region route policy switch off!");
        }
    }

    private final Logger logger = LoggerLoader.getLogger(this.getClass());

    private final ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    // 自动切换region的开关
    private volatile boolean enableRegionPolicy = configManager.getBooleanValue("pigeon.regions.enable", false);
    private volatile boolean isInit = false;

    // example: 10.66 --> region1
    private Map<String, Region> patternRegionMappings = new HashMap<String, Region>();

    private Region[] regionArray;

    public final String DEFAULT_REGIONPOLICY = configManager.getStringValue(
            Constants.KEY_REGIONPOLICY, AutoSwitchRegionPolicy.NAME);

    private Map<String, RegionPolicy> regionPolicyMap = new ConcurrentHashMap<String, RegionPolicy>();

    private void checkClientsNotNull(List<Client> clientList, InvokerConfig<?> invokerConfig) {
        if(clientList == null) {
            throw new RegionException("no available clientList in region policy for service[" + invokerConfig + "], env:"
                    + ConfigManagerLoader.getConfigManager().getEnv());
        }
    }

    public List<Client> getPreferRegionClients(List<Client> clientList, InvokerConfig<?> invokerConfig) {
        if(!isEnableRegionPolicy()) {// region策略开关关闭时不处理
            return clientList;
        }

        RegionPolicy regionPolicy = getRegionPolicy(invokerConfig);

        if(regionPolicy == null) {
            regionPolicy = AutoSwitchRegionPolicy.INSTANCE;
        }

        clientList = regionPolicy.getPreferRegionClients(clientList, invokerConfig);
        checkClientsNotNull(clientList, invokerConfig);

        return clientList;
    }

    private RegionPolicy getRegionPolicy(InvokerConfig<?> invokerConfig) {
        String serviceId = ServiceUtils.getServiceId(invokerConfig.getUrl(), invokerConfig.getGroup());
        RegionPolicy regionPolicy = regionPolicyMap.get(serviceId);
        if (regionPolicy != null) {
            return regionPolicy;
        }
        regionPolicy = regionPolicyMap.get(invokerConfig.getRegionPolicy());
        if (regionPolicy != null) {
            return regionPolicy;
        }
        if (DEFAULT_REGIONPOLICY != null) {
            regionPolicy = regionPolicyMap.get(DEFAULT_REGIONPOLICY);
            if (regionPolicy != null) {
                regionPolicyMap.put(invokerConfig.getRegionPolicy(), regionPolicy);
                return regionPolicy;
            } else {
                logger.warn("the regionPolicy[" + DEFAULT_REGIONPOLICY + "] is invalid, only support "
                                + regionPolicyMap.keySet() + ".");
            }
        }
        return null;
    }

    /**
     * 注册RegionPolicy
     * @param serviceName
     * @param group
     * @param regionPolicy
     */
    @SuppressWarnings("unchecked")
    public void register(String serviceName, String group, Object regionPolicy) {
        String serviceId = ServiceUtils.getServiceId(serviceName, group);
        RegionPolicy regionPolicyObj = null;
        if(regionPolicy instanceof RegionPolicy) {
            regionPolicyObj = (RegionPolicy) regionPolicy;
        } else if (regionPolicy instanceof String && StringUtils.isNotBlank((String) regionPolicy)) {
            if (!regionPolicyMap.containsKey(regionPolicy)) {
                try {
                    Class<? extends RegionPolicy> regionPolicyClass = (Class<? extends RegionPolicy>) ClassUtils
                            .loadClass((String) regionPolicy);
                    regionPolicyObj = regionPolicyClass.newInstance();
                } catch (Throwable e) {
                    throw new InvalidParameterException("failed to register regionPolicy[service=" + serviceId
                            + ",class=" + regionPolicy + "]", e);
                }
            } else {
                regionPolicyObj = regionPolicyMap.get(regionPolicy);
            }
        } else if (regionPolicy instanceof Class) {
            try {
                Class<? extends RegionPolicy> regionPolicyClass = (Class<? extends RegionPolicy>) regionPolicy;
                regionPolicyObj = regionPolicyClass.newInstance();
            } catch (Throwable e) {
                throw new InvalidParameterException("failed to register regionPolicy[service=" + serviceId + ",class="
                        + regionPolicy + "]", e);
            }
        }
        if (regionPolicyObj != null) {
            regionPolicyMap.put(serviceId, regionPolicyObj);
        }
    }

    private class InnerConfigChangeListener implements ConfigChangeListener {

        @Override
        public void onKeyUpdated(String key, String value) {
            if (key.endsWith("pigeon.regions.enable")) {
                boolean _enableRegionPolicy = Boolean.valueOf(value);

                if(enableRegionPolicy != _enableRegionPolicy) { // region路由开关改变
                    enableRegionPolicy = _enableRegionPolicy;
                    if(enableRegionPolicy) { // region路由开,重新读取配置
                        initRegionsConfig();
                    } else { // region路由关
                        isInit = false;
                        logger.warn("Region auto switch off!");
                    }
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

    public boolean isEnableRegionPolicy() {
        return enableRegionPolicy && isInit;
    }

    private synchronized void initRegionsConfig() {
        if(isInit) {return;}
        try {
            // 处理pigeon.regions配置
            String pigeonRegionsConfig = configManager.getStringValue("pigeon.regions");
            String[] regionConfigs = pigeonRegionsConfig.split(";");

            int regionCount = regionConfigs.length;

            if(regionCount <= 0) {
                logger.error("Error! Set [enableRegionAutoSwitch] to false! Please check regions config!");
                enableRegionPolicy = false;
                return ;
            }

            Set<String> regionSet = new HashSet<String>();
            Map<String, String> patternRegionNameMappings = new HashMap<String, String>();
            for(int i = 0; i < regionCount; ++i) {
                String[] regionPatternMapping = regionConfigs[i].split(":");
                String regionName = regionPatternMapping[0];
                String[] patterns = regionPatternMapping[1].split(",");

                regionSet.add(regionName);

                for(String pattern : patterns) {
                    patternRegionNameMappings.put(pattern, regionName);
                }
            }

            //初始化local region
            String localRegionPattern = getPattern(configManager.getLocalIp());
            if(patternRegionNameMappings.containsKey(localRegionPattern)) {
                String localRegionName = patternRegionNameMappings.get(localRegionPattern);
                // 权重处理
                Region[] regions = initRegionsWithPriority(localRegionName);
                if(regionSet.size() == regions.length) {
                    for(Region region : regions) {
                        if(!regionSet.contains(region.getName())) {
                            logger.error("Error! Set [enableRegionAutoSwitch] to false! regions prefer not match regions config: " + region.getName());
                            enableRegionPolicy = false;
                            return;
                        }
                    }
                    regionArray = regions;
                    // 初始化pattern region映射
                    initPatterRegionMappings(patternRegionNameMappings);
                    isInit = true;
                    logger.warn("Region auto switch on! Local region is: " + regionArray[0]);
                } else {
                    logger.error("Error! Set [enableRegionAutoSwitch] to false! regions prefer counts not match regions config!");
                    enableRegionPolicy = false;
                }
            } else {
                logger.error("Error! Set [enableRegionAutoSwitch] to false! Can't init local region: " + configManager.getLocalIp());
                enableRegionPolicy = false;
            }
        } catch (Throwable t) {
            logger.error("Error! Set [enableRegionAutoSwitch] to false!", t);
            enableRegionPolicy = false;
        }
    }

    private String getPattern(String host) {
        return host.substring(0, host.indexOf(".", host.indexOf(".") + 1 ));
    }

    private void initPatterRegionMappings(Map<String, String> patternRegionNameMappings) {
        patternRegionMappings.clear();
        for(Map.Entry<String, String> entry : patternRegionNameMappings.entrySet()) {
            patternRegionMappings.put(entry.getKey(), getRegionByName(entry.getValue()));
        }
    }

    private Region[] initRegionsWithPriority(String localRegionName) {
        String regionsPrefer = configManager.getStringValue("pigeon.regions.prefer." + localRegionName);
        if(StringUtils.isNotBlank(regionsPrefer)) {
            String[] regionNames = regionsPrefer.split(",");
            Region[] regions = new Region[regionNames.length];
            for(int i = 0; i < regionNames.length; ++i) {
                regions[i] = new Region(regionNames[i], i);
            }

            return regions;
        }
        return new Region[0];
    }

    private Region getRegionByName(String regionName) {
        for (Region region : regionArray) {
            if (region.getName().equalsIgnoreCase(regionName)) {
                return region;
            }
        }
        return null;
    }

}
