package com.dianping.pigeon.remoting.invoker.region;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenchongze on 16/2/19.
 */
public class RegionManager {

    private final Logger logger = LoggerLoader.getLogger(RegionManager.class);

    private static volatile RegionManager instance;

    private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private String localRegion;

    private String currentRegion;

    private ConcurrentHashMap<String, String> regionPatternMappings = new ConcurrentHashMap<String, String>();

    private ConcurrentHashMap<String, String> patternRegionMappings = new ConcurrentHashMap<String, String>();

    private ConcurrentHashMap<String, Boolean> regionHostHeartBeatStats = new ConcurrentHashMap<String, Boolean>();

    private RegionManager() {}

    public static RegionManager getInstance() {
        if (instance == null) {
            synchronized (RegionManager.class) {
                if (instance == null) {
                    instance = new RegionManager();
                    instance.init();
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

            for(String pattern : patterns) {
                patternRegionMappings.putIfAbsent(pattern, region);
            }

        }

        try {
            localRegion = getRegion(configManager.getLocalIp());
            currentRegion = localRegion;
        } catch (Exception e) {
            logger.error(e);
        }

    }

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

    public String getRegion(String ip) throws Exception {
        String pattern = ip.substring(0, ip.indexOf(".", 2));
        if(patternRegionMappings.containsKey(pattern)) {
            return patternRegionMappings.get(pattern);
        } else {
            throw new Exception("can't find ip pattern in region mapping: " + ip);
        }
    }

    public ConcurrentHashMap<String, Boolean> getRegionHostHeartBeatStats() {
        return regionHostHeartBeatStats;
    }
}
