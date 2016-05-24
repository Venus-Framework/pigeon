package com.dianping.pigeon.remoting.invoker.route.region;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.exception.InvalidParameterException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
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

    private RegionPolicyManager () {}

    public void init() {
        register(AutoSwitchRegionPolicy.NAME, null, AutoSwitchRegionPolicy.INSTANCE);
        register(WeightBasedRegionPolicy.NAME, null, WeightBasedRegionPolicy.INSTANCE);
        if(configManager.getBooleanValue(KEY_ENABLEREGIONPOLICY, DEFAULT_ENABLEREGIONPOLICY)) {
            initRegionsConfig();
        } else {
            logger.warn("Region policy is disabled!");
        }
        configManager.registerConfigChangeListener(new InnerConfigChangeListener());
    }

    private final Monitor monitor = MonitorLoader.getMonitor();

    private final Logger logger = LoggerLoader.getLogger(this.getClass());

    private final ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    // 自动切换region的开关
    private final String KEY_ENABLEREGIONPOLICY = "pigeon.regions.enable";
    private final boolean DEFAULT_ENABLEREGIONPOLICY = false;
    private volatile boolean isInit = false;

    // example: 10.66 --> region1
    private Map<String, Region> patternRegionMappings = new HashMap<String, Region>();

    private List<Region> regionArray;

    public final String DEFAULT_REGIONPOLICY = configManager.getStringValue(
            Constants.KEY_REGIONPOLICY, AutoSwitchRegionPolicy.NAME);

    private Map<String, RegionPolicy> regionPolicyMap = new ConcurrentHashMap<String, RegionPolicy>();

    private void checkClientsNotNull(List<Client> clientList, InvokerConfig<?> invokerConfig) {
        if(clientList == null) {
            throw new RegionException("no available clientList in region policy for service[" + invokerConfig + "], env:"
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
        monitor.logEvent("PigeonCall.region", clientList.get(0).getRegion().getName(), "");

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
            if (key.endsWith(KEY_ENABLEREGIONPOLICY)) {

                if(Boolean.valueOf(value)) { // region路由开,重新读取配置
                    // 清空allClient region信息
                    clearRegion();
                    initRegionsConfig();
                } else { // region路由关
                    isInit = false;
                    logger.warn("Region policy is disabled!");
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
        return configManager.getBooleanValue(KEY_ENABLEREGIONPOLICY, DEFAULT_ENABLEREGIONPOLICY) && isInit;
    }

    private synchronized void initRegionsConfig() {
        if(isInit) {return;}
        try {
            // 处理pigeon.regions配置
            String pigeonRegionsConfig = configManager.getStringValue("pigeon.regions");
            String[] regionConfigs = pigeonRegionsConfig.split(";");

            int regionCount = regionConfigs.length;

            if(regionCount <= 0) {
                logger.error("Error! Region policy is disabled! Please check regions config!");
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
                List<Region> regions = initRegionsWithPriority(localRegionName);
                if(regionSet.size() == regions.size()) {
                    for(Region region : regions) {
                        if(!regionSet.contains(region.getName())) {
                            logger.error("Error! Region policy is disabled! regions prefer not match regions config: " + region.getName());
                            return;
                        }
                    }
                    regionArray = Collections.unmodifiableList(regions);
                    // 初始化pattern region映射
                    initPatterRegionMappings(patternRegionNameMappings);
                    isInit = true;
                    logger.warn("Region route policy switch on! Local region is: " + regionArray.get(0));
                } else {
                    logger.error("Error! Region policy is disabled! regions prefer counts not match regions config!");
                }
            } else {
                logger.error("Error! Region policy is disabled! Can't init local region: " + configManager.getLocalIp());
            }
        } catch (Throwable t) {
            logger.error("Error! Region policy is disabled!", t);
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

    private List<Region> initRegionsWithPriority(String localRegionName) {
        String regionsPrefer = configManager.getStringValue("pigeon.regions.prefer." + localRegionName);
        if(StringUtils.isNotBlank(regionsPrefer)) {
            String[] regionNameAndWeights = regionsPrefer.split(",");
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

}
