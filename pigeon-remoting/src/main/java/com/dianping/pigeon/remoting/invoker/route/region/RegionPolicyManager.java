package com.dianping.pigeon.remoting.invoker.route.region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.exception.RouteException;
import com.dianping.pigeon.util.ClassUtils;
import com.dianping.pigeon.util.ServiceUtils;

/**
 * Created by chenchongze on 16/4/14.
 */
public enum RegionPolicyManager {

    INSTANCE;

    private RegionPolicyManager () {}

    private static volatile boolean isInitialized = false;

    public void init() {
        if (!isInitialized) {
           synchronized (RegionPolicyManager.class) {
               if (!isInitialized) {
                   register(AutoSwitchRegionPolicy.NAME, null, AutoSwitchRegionPolicy.INSTANCE);
                   register(WeightBasedRegionPolicy.NAME, null, WeightBasedRegionPolicy.INSTANCE);

                   if(configManager.getBooleanValue(KEY_ENABLEREGIONPOLICY, DEFAULT_ENABLEREGIONPOLICY)) {
                       initRegionsConfig();
                   } else {
                       logger.warn("Region policy is disabled!");
                   }

                   configManager.registerConfigChangeListener(new InnerConfigChangeListener());
                   isInitialized = true;
               }
           }
        }
    }

    private final Monitor monitor = MonitorLoader.getMonitor();

    private final Logger logger = LoggerLoader.getLogger(this.getClass());

    private final ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    // 自动切换region的开关
    public final String KEY_ENABLEREGIONPOLICY = "pigeon.regions.route.enable";
    public final boolean DEFAULT_ENABLEREGIONPOLICY = false;
    public final String KEY_REGIONINFO = "pigeon.regions";
    public final String KEY_REGION_PREFER_BASE = "pigeon.regions.prefer.";
    private volatile boolean isEnabled = false;

    // example: 10.66 --> region1
    private Map<String, Region> patternRegionMappings = new HashMap<String, Region>();

    private volatile List<Region> regionArray;

    private volatile Region localRegion;

    public final String DEFAULT_REGIONPOLICY = configManager.getStringValue(
            Constants.KEY_REGIONPOLICY, AutoSwitchRegionPolicy.NAME);

    private Map<String, RegionPolicy> regionPolicyMap = new ConcurrentHashMap<String, RegionPolicy>();

    private void checkClientsNotNull(List<Client> clientList, InvokerConfig<?> invokerConfig) {
        if(clientList == null) {
            throw new RouteException("no available clientList in region policy for service[" + invokerConfig + "], env:"
                    + ConfigManagerLoader.getConfigManager().getEnv());
        }
    }

    public List<Client> getPreferRegionClients(List<Client> clientList, InvokerConfig<?> invokerConfig,
                                               InvocationRequest request) {
        RegionPolicy regionPolicy = getRegionPolicy(invokerConfig);

        if(regionPolicy == null) {
            regionPolicy = AutoSwitchRegionPolicy.INSTANCE;
        }

        clientList = regionPolicy.getPreferRegionClients(clientList, request);
        checkClientsNotNull(clientList, invokerConfig);
        monitor.logEvent("PigeonCall.region", request.getServiceName() + "#" + clientList.get(0).getRegion().getName(), "");

        return clientList;
    }

    public RegionPolicy getRegionPolicy(InvokerConfig<?> invokerConfig) {
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
                    throw new IllegalArgumentException("failed to register regionPolicy[service=" + serviceId
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
                throw new IllegalArgumentException("failed to register regionPolicy[service=" + serviceId + ",class="
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

            if (key.endsWith(KEY_ENABLEREGIONPOLICY)) {

                if(Boolean.valueOf(value)) { // region路由开,重新读取配置
                    // 清空allClient region信息
                    initRegionsConfig();

                } else { // region路由关
                    isEnabled = false;
                    logger.warn("Region policy is disabled!");
                }

            } else if(isEnabled && key.endsWith(KEY_REGIONINFO)) {

                initRegionsConfig(value);

            } else if(isEnabled && localRegion != null && key.endsWith(KEY_REGION_PREFER_BASE + localRegion.getName())) {

                initRegionsConfig(configManager.getStringValue(KEY_REGIONINFO), value);

            }

        }

        @Override
        public void onKeyAdded(String key, String value) {

        }

        @Override
        public void onKeyRemoved(String key) {

        }
    }

    private void clearRegion() {
        ConcurrentHashMap<String, Client> allClients = ClientManager.getInstance().getClusterListener().getAllClients();
        if(!allClients.isEmpty()) {
            for(String address : allClients.keySet()) {
                Client client = allClients.get(address);
                client.clearRegion();
            }
        }
    }

    public boolean isEnableRegionPolicy() {
        return configManager.getBooleanValue(KEY_ENABLEREGIONPOLICY, DEFAULT_ENABLEREGIONPOLICY) && isEnabled;
    }

    private void initRegionsConfig() {
        initRegionsConfig(configManager.getStringValue(KEY_REGIONINFO));
    }

    private void initRegionsConfig(String pigeonRegionsConfig) {
        initRegionsConfig(pigeonRegionsConfig, null);
    }

    private synchronized void initRegionsConfig(String pigeonRegionsConfig, String regionsPreferConfig) {
        try {
            String[] regionConfigs = pigeonRegionsConfig.split(";");

            int regionCount = regionConfigs.length;

            if(regionCount <= 0) {
                logger.error("Error! Please check regions config!");
                return ;
            }

            Set<String> regionSet = new HashSet<String>();
            Map<String, String> patternRegionNameMappings = new HashMap<String, String>();
            for (String regionConfig : regionConfigs) {
                String[] regionPatternMapping = regionConfig.split(":");
                String regionName = regionPatternMapping[0];
                String[] patterns = regionPatternMapping[1].split(",");

                regionSet.add(regionName);

                for (String pattern : patterns) {
                    patternRegionNameMappings.put(pattern, regionName);
                }
            }

            //初始化local region
            String localRegionPattern = getPattern(configManager.getLocalIp());
            if(patternRegionNameMappings.containsKey(localRegionPattern)) {
                String localRegionName = patternRegionNameMappings.get(localRegionPattern);
                // 权重处理
                if (StringUtils.isBlank(regionsPreferConfig)) {
                    regionsPreferConfig = configManager.getStringValue(KEY_REGION_PREFER_BASE + localRegionName);
                }
                List<Region> regions = initRegionsWithPriority(regionsPreferConfig);
                if(regionSet.size() == regions.size()) {
                    for(Region region : regions) {
                        if(!regionSet.contains(region.getName())) {
                            logger.error("Error! Regions prefer not match regions config: " + region.getName());
                            return;
                        }
                    }

                    //(re)init
                    regionArray = Collections.unmodifiableList(regions);// 下面的步骤都基于regionArray
                    initPatterRegionMappings(patternRegionNameMappings);// 初始化pattern region映射
                    localRegion = getRegionByName(localRegionName);
                    clearRegion();
                    isEnabled = true;
                    logger.warn("Region route policy switch on! Local region is: " + regionArray.get(0));

                } else {
                    logger.error("Error! Regions prefer counts not match regions config!");
                }
            } else {
                logger.error("Error! Can't init local region: " + configManager.getLocalIp());
            }
        } catch (Throwable t) {
            logger.error("Error! Init region policy failed!", t);
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

    private List<Region> initRegionsWithPriority(String regionsPreferConfig) {
        if(StringUtils.isNotBlank(regionsPreferConfig)) {
            String[] regionNameAndWeights = regionsPreferConfig.split(",");
            List<Region> regions = new ArrayList<Region>(regionNameAndWeights.length);
            for(int i = 0; i < regionNameAndWeights.length; ++i) {
                String[] _regionNameAndWeight = regionNameAndWeights[i].split(":");
                String regionName = _regionNameAndWeight[0];
                int regionWeight = Integer.parseInt(_regionNameAndWeight[1]);
                regions.add(new Region(regionName, i, regionWeight));
            }

            return regions;
        }
        return new ArrayList<Region>();
    }

    private Region getRegionByName(String regionName) {
        for (Region region : regionArray) {
            if (region.getName().equalsIgnoreCase(regionName)) {
                return region;
            }
        }
        return null;
    }

    public Region getRegion(String host) {
        String pattern = getPattern(host);
        if(patternRegionMappings.containsKey(pattern)) {
            return patternRegionMappings.get(pattern);
        } else {
            logger.error("can't find ip pattern in region mapping: " + host);
            return null;
        }
    }

    public List<Region> getRegionArray() {
        return regionArray;
    }

    public Region getLocalRegion() {
        return localRegion;
    }
}
