package com.dianping.pigeon.registry.region;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.exception.RegionException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenchongze on 16/2/29.
 */
public class RegionManager {

    private final Logger logger = LoggerLoader.getLogger(RegionManager.class);

    private static volatile RegionManager instance;

    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private PriorityQueue<Region> waitingRegionQueue;

    // 自动切换region的开关
    private boolean enableRegionAutoSwitch = configManager.getBooleanValue("pigeon.regions.enable", false);

    // example: 10.66.xx.yy --> true
    private ConcurrentHashMap<String, Boolean> regionHostHeartBeatStats = new ConcurrentHashMap<String, Boolean>();

    private Region localRegion;

    private Region notLocalRegion;

    // service --> region mapping
    private ConcurrentHashMap<String, Region> serviceCurrentRegionMappings = new ConcurrentHashMap<String, Region>();

    // example: 10.66 --> region1
    private ConcurrentHashMap<String, Region> patternRegionMappings = new ConcurrentHashMap<String, Region>();

    // region1 --> 10.66 192.168
    //private ConcurrentHashMap<String, List<String>> regionPatternMappings = new ConcurrentHashMap<String, List<String>>();

    private RegionManager() {
        init();
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

        if(regionConfigs.length <= 0) {
            logger.error("Error! Set [enableRegionAutoSwitch] to false! Please check regions config!");
            enableRegionAutoSwitch = false;
            return ;
        }

        waitingRegionQueue = new PriorityQueue<Region>(regionConfigs.length, new RegionComparator());

        for(int i = 0; i < regionConfigs.length; ++i) {
            String[] regionPatternMapping = regionConfigs[i].split(":");
            String regionName = regionPatternMapping[0];
            String[] patterns = regionPatternMapping[1].split(",");

            Region region = new Region(regionName, i);
            waitingRegionQueue.offer(region);

            for(String pattern : patterns) {
                patternRegionMappings.putIfAbsent(pattern, region);
            }
        }

        /*for(String regionConfig : regionConfigs) {
            String[] regionPatternMapping = regionConfig.split(":");
            String region = regionPatternMapping[0];
            String[] patterns = regionPatternMapping[1].split(",");

            List<String> patternList = new ArrayList<String>();
            for(String pattern : patterns) {
                patternRegionMappings.putIfAbsent(pattern, region);
                patternList.add(pattern);
            }
            regionPatternMappings.putIfAbsent(region, patternList);

        }*/

        //初始化local region
        String pattern = getPattern(configManager.getLocalIp());
        if(patternRegionMappings.containsKey(pattern)) {
            localRegion = patternRegionMappings.get(pattern);
        } else {
            logger.error("Error! Set [enableRegionAutoSwitch] to false! Can't init local region: " + configManager.getLocalIp());
            enableRegionAutoSwitch = false;
        }

    }

    private String getPattern(String host) {
        return host.substring(0, host.indexOf(".", host.indexOf(".") + 1 ));
    }

    private Region getRegion(String host) throws RegionException {
        String pattern = getPattern(host);
        if(patternRegionMappings.containsKey(pattern)) {
            return patternRegionMappings.get(pattern);
        } else {
            throw new RegionException("can't find ip pattern in region mapping: " + host);
        }
    }

    public boolean isInLocalRegion(String host) {
        try {
            if (getRegion(host).equals(localRegion)) {
                return true;
            } else {
                return false;
            }
        } catch (RegionException e) {
            logger.warn(e);
            return false;
        } catch (Throwable t) {
            logger.error(t);
            return false;
        }
    }

    public boolean isInCurrentRegion(String serviceName, HostInfo hostInfo) {
        //TODO 判断并加入缓存(主要缓存本region的，重点观察对象)
        Region currentRegion = serviceCurrentRegionMappings.get(serviceName);
        try {
            if(getRegion(hostInfo.getHost()).equals(currentRegion)) {
                return true;
            } else {
                return false;
            }
        } catch (RegionException e) {
            logger.warn(e);
            return false;
        } catch (Throwable t) {
            logger.error(t);
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
                    if(localRegion.equals(getRegion(host))) {
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

    public ConcurrentHashMap<String, Region> getServiceCurrentRegionMappings() {
        return serviceCurrentRegionMappings;
    }

    public Region getLocalRegion() {
        return localRegion;
    }

    public Region getNotLocalRegion() {
        return notLocalRegion;
    }

    public ConcurrentHashMap<String, Boolean> getRegionHostHeartBeatStats() {
        return regionHostHeartBeatStats;
    }

    private class RegionComparator implements Comparator<Region> {

        @Override
        public int compare(Region o1, Region o2) {
            Integer priority1 = o1.getPriority();
            Integer priority2 = o2.getPriority();

            return priority1.compareTo(priority2);
        }
    }
}
