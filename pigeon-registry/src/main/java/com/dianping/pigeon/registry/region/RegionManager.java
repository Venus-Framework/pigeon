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

    // service --> current region mapping
    private ConcurrentHashMap<String, Region> serviceCurrentRegionMappings = new ConcurrentHashMap<String, Region>();

    private Region[] regionArray;

    // example: 10.66 --> region1
    private Map<String, Region> patternRegionMappings = new HashMap<String, Region>();

    //TODO 添加动态开关多region路由功能


    private RegionManager() {
        if(enableRegionAutoSwitch) {
            initRegionsPriority();
        } else {
            logger.warn("Region auto switch off!");
        }
    }

    private void initRegionsPriority() {
        try {
            // 处理pigeon.regions配置
            String pigeonRegionsConfig = configManager.getStringValue("pigeon.regions");
            String[] regionConfigs = pigeonRegionsConfig.split(";");

            int regionCount = regionConfigs.length;

            if(regionCount <= 0) {
                logger.error("Error! Set [enableRegionAutoSwitch] to false! Please check regions config!");
                enableRegionAutoSwitch = false;
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
                            enableRegionAutoSwitch = false;
                            return;
                        }
                    }
                    regionArray = regions;
                    // 初始化pattern region映射
                    initPatterRegionMappings(patternRegionNameMappings);
                    logger.warn("Region auto switch on! Local region is: " + regionArray[0]);
                } else {
                    logger.error("Error! Set [enableRegionAutoSwitch] to false! regions prefer counts not match regions config!");
                    enableRegionAutoSwitch = false;
                }
            } else {
                logger.error("Error! Set [enableRegionAutoSwitch] to false! Can't init local region: " + configManager.getLocalIp());
                enableRegionAutoSwitch = false;
            }
        } catch (Throwable t) {
            logger.error("Error! Set [enableRegionAutoSwitch] to false!", t);
            enableRegionAutoSwitch = false;
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

    private void initPatterRegionMappings(Map<String, String> patternRegionNameMappings) {
        patternRegionMappings.clear();
        for(Map.Entry<String, String> entry : patternRegionNameMappings.entrySet()) {
            patternRegionMappings.put(entry.getKey(), getRegionByName(entry.getValue()));
        }
    }

    private Region getRegionByName(String regionName) {
        for (Region region : regionArray) {
            if (region.getName().equalsIgnoreCase(regionName)) {
                return region;
            }
        }
        return null;
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

    /*public boolean isInLocalRegion(String host) {
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
    }*/

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

    // 监听到前置region的host增加
    public boolean isInMonitorRegion(String serviceName, HostInfo hostInfo) {
        try {
            Region currentRegion = getCurrentRegion(serviceName);
            Region region = getRegion(hostInfo.getHost());
            return region.compareTo(currentRegion) <= 0;
        } catch (Throwable t) {
            logger.error(t);
            return false;
        }
    }

    // 判断并重新将属于本region的ip组成待连接的串
    /*public String getLocalRegionHosts(String hosts) {
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
    }*/

    public boolean isEnableRegionAutoSwitch() {
        return enableRegionAutoSwitch;
    }

    public void register(String serviceName) {
        serviceCurrentRegionMappings.put(serviceName, regionArray[0]);
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
