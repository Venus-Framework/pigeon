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
public enum RegionManager {

    INSTANCE;

    private final Logger logger = LoggerLoader.getLogger(RegionManager.class);

    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    // 自动切换region的开关
    private boolean enableRegionAutoSwitch = configManager.getBooleanValue("pigeon.regions.enable", false);

    // example: 10.66.xx.yy --> true
    private ConcurrentHashMap<String, Boolean> regionHostHeartBeatStats = new ConcurrentHashMap<String, Boolean>();

    private Region localRegion;

    // service --> current region mapping
    private ConcurrentHashMap<String, Region> serviceCurrentRegionMappings = new ConcurrentHashMap<String, Region>();

    private Region[] regionArray;

    // example: 10.66 --> region1
    private ConcurrentHashMap<String, Region> patternRegionMappings = new ConcurrentHashMap<String, Region>();

    // region1 --> 10.66 192.168 貌似用hashset就够了
    private ConcurrentHashMap<Region, List<String>> regionPatternMappings = new ConcurrentHashMap<Region, List<String>>();

    private RegionManager() {
        if(enableRegionAutoSwitch) {
            initRegionsPriority();
        }
    }

    private void initRegionsPriority() {
        String pigeonRegionsConfig = configManager.getStringValue("pigeon.regions", "region1:10.1,10.3;region2:10.6;region3:10.8,10.9");
        String[] regionConfigs = pigeonRegionsConfig.split(";");

        int regionCount = regionConfigs.length;

        if(regionCount <= 0) {
            logger.error("Error! Set [enableRegionAutoSwitch] to false! Please check regions config!");
            enableRegionAutoSwitch = false;
            return ;
        }

        Set<Region> regionSet = new HashSet<Region>();

        for(int i = 0; i < regionCount; ++i) {
            String[] regionPatternMapping = regionConfigs[i].split(":");
            String regionName = regionPatternMapping[0];
            String[] patterns = regionPatternMapping[1].split(",");

            Region region = new Region(regionName, i);
            regionSet.add(region);

            List<String> patternList = new ArrayList<String>();

            for(String pattern : patterns) {
                patternRegionMappings.put(pattern, region);
                patternList.add(pattern);
            }

            regionPatternMappings.put(region, patternList);
        }

        //初始化local region
        String pattern = getPattern(configManager.getLocalIp());
        if(patternRegionMappings.containsKey(pattern)) {
            localRegion = patternRegionMappings.get(pattern);

            //TODO 权重处理
            Region[] regions = getRegionsWithPriority();
            if(regionSet.size() == regions.length) {
                for(Region region : regions) {
                    if(!regionSet.contains(region)) {
                        logger.error("Error! Set [enableRegionAutoSwitch] to false! regions prefer not match regions config: " + region.getName());
                        enableRegionAutoSwitch = false;
                        return;
                    }
                }
                regionArray = regions;
                logger.info("Region auto switch on! Local region is: " + localRegion);
            } else {
                logger.error("Error! Set [enableRegionAutoSwitch] to false! regions prefer counts not match regions config!");
                enableRegionAutoSwitch = false;
            }
        } else {
            logger.error("Error! Set [enableRegionAutoSwitch] to false! Can't init local region: " + configManager.getLocalIp());
            enableRegionAutoSwitch = false;
        }
    }

    private Region[] getRegionsWithPriority() {
        String regionsPrefer = configManager.getStringValue("pigeon.regions.prefer." + localRegion.getName());
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

    private String getPattern(String host) {
        return host.substring(0, host.indexOf(".", host.indexOf(".") + 1 ));
    }

    public Region getRegion(String host) throws RegionException {
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
        try {
            return getRegion(hostInfo.getHost()).equals(getCurrentRegion(serviceName));
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

    public boolean isEnableRegionAutoSwitch() {
        return enableRegionAutoSwitch;
    }

    public void register(String serviceName) {
        serviceCurrentRegionMappings.put(serviceName, localRegion);
    }

    public void unregister(String serviceName) {
        //TODO 是否必要
        serviceCurrentRegionMappings.remove(serviceName);
    }

    public void switchRegion(String serviceName, Region region) {
        serviceCurrentRegionMappings.put(serviceName, region);
    }

    public Region getCurrentRegion(String serviceName) {
        if(serviceCurrentRegionMappings.containsKey(serviceName)) {
            return serviceCurrentRegionMappings.get(serviceName);
        } else {
            throw new IllegalArgumentException("can't find config for service: " + serviceName);
        }
    }

    public ConcurrentHashMap<String, Boolean> getRegionHostHeartBeatStats() {
        return regionHostHeartBeatStats;
    }

    public Region[] getRegionArray() {
        return regionArray;
    }
}
