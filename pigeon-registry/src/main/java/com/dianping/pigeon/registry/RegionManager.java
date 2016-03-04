package com.dianping.pigeon.registry;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.log.LoggerLoader;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenchongze on 16/2/29.
 */
public class RegionManager {

    private final Logger logger = LoggerLoader.getLogger(RegionManager.class);

    private static volatile RegionManager instance;

    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    // 自动切换region的开关
    private boolean enableRegionAutoSwitch = configManager.getBooleanValue("pigeon.regions.enable", false);

    // example: 10.66.xx.yy --> true
    private ConcurrentHashMap<String, Boolean> regionHostHeartBeatStats = new ConcurrentHashMap<String, Boolean>();

    private String localRegion;

    private String notLocalRegion;
    // service --> region mapping
    private ConcurrentHashMap<String, String> serviceCurrentRegionMappings = new ConcurrentHashMap<String, String>();

    // example: 10.66 --> region1
    private ConcurrentHashMap<String, String> patternRegionMappings = new ConcurrentHashMap<String, String>();

    // region1 --> 10.66 192.168
    private ConcurrentHashMap<String, List<String>> regionPatternMappings = new ConcurrentHashMap<String, List<String>>();

    private RegionManager() {
        init();
        try {
            localRegion = getRegion(configManager.getLocalIp());
            notLocalRegion = "NOT_" + localRegion;
        } catch (Exception e) {
            logger.error("error, set enableRegionAutoSwitch to false", e);
            enableRegionAutoSwitch = false;
        }
    }

    public static RegionManager getInstance() {
        if (instance == null) {
            synchronized (RegionManager.class) {
                if (instance == null) {
                    instance = new RegionManager();
                }
            }
        }
        return instance;
    }

    private void init() {
        String pigeonRegionsConfig = configManager.getStringValue("pigeon.regions", "region1:10.1,10.3;region2:10.6;region3:10.8,10.9");
        String[] regionConfigs = pigeonRegionsConfig.split(";");

        for(String regionConfig : regionConfigs) {
            String[] regionPatternMapping = regionConfig.split(":");
            String region = regionPatternMapping[0];
            String[] patterns = regionPatternMapping[1].split(",");

            List<String> patternList = new ArrayList<String>();
            for(String pattern : patterns) {
                patternRegionMappings.putIfAbsent(pattern, region);
                patternList.add(pattern);
            }
            regionPatternMappings.putIfAbsent(region, patternList);

        }

    }


    public String getRegion(String host) throws Exception {
        String pattern = host.substring(0, host.indexOf(".", 2));
        if(patternRegionMappings.containsKey(pattern)) {
            String _region = patternRegionMappings.get(pattern);
            //TODO 之后还要做完善，现在暂时分为本地非本地
            if(localRegion.equalsIgnoreCase(_region)) {
                return localRegion;
            } else {
                return notLocalRegion;
            }
        } else {
            throw new Exception("can't find ip pattern in region mapping: " + host);
        }
    }

    public boolean isInLocalRegion(String host) {
        try {
            if(getRegion(host).equalsIgnoreCase(localRegion)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.warn(e);
            return false;
        }
    }

    public boolean isInCurrentRegion(String serviceName, HostInfo hostInfo) {
        //TODO 判断并加入缓存(主要缓存本region的，重点观察对象)
        String currentRegion = serviceCurrentRegionMappings.get(serviceName);
        try {
            if(getRegion(hostInfo.getHost()).equalsIgnoreCase(currentRegion)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.warn(e);
            return false;
        }
    }

    // 判断并重新将属于本region的ip组成待连接的串
    public String getLocalRegionHosts(String hosts) {
        Set<String> resultSet = new HashSet<String>();

        if(localRegion == null) {
            return hosts;
        }

        for(String host : hosts.split(",")) {
            if(StringUtils.isNotBlank(host)) {
                // 注意：ip 和 host 语义不同
                try {
                    if(localRegion.equalsIgnoreCase(getRegion(host))) {
                        resultSet.add(host);
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }

        return StringUtils.join(resultSet, ",");
    }

    public String getFilterHosts(String serviceAddress) {
        //TODO filter策略，根据currentRegion来选择
        if(isEnableRegionAutoSwitch()) {
            return getCurrentRegionHosts(serviceAddress);
        } else {
            return serviceAddress;
        }
    }

    private String getCurrentRegionHosts(String hosts) {
        return getLocalRegionHosts(hosts);
    }

    public boolean isEnableRegionAutoSwitch() {
        return enableRegionAutoSwitch;
    }

    public void register(String serviceName) {
        serviceCurrentRegionMappings.putIfAbsent(serviceName, localRegion);
    }

    public ConcurrentHashMap<String, String> getServiceCurrentRegionMappings() {
        return serviceCurrentRegionMappings;
    }

    public String getLocalRegion() {
        return localRegion;
    }

    public String getNotLocalRegion() {
        return notLocalRegion;
    }

    public ConcurrentHashMap<String, Boolean> getRegionHostHeartBeatStats() {
        return regionHostHeartBeatStats;
    }
}
