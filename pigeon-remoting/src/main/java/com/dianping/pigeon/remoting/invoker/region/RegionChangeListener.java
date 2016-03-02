package com.dianping.pigeon.remoting.invoker.region;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegionManager;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.listener.ClusterListener;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by chenchongze on 16/2/19.
 */
public class RegionChangeListener implements Runnable, ClusterListener {

    private final Logger logger = LoggerLoader.getLogger(RegionChangeListener.class);

    private static RegionManager regionManager = RegionManager.getInstance();

    private static volatile RegionChangeListener instance;

    private Map<String, List<Client>> workingClients;

    // servicename --> localregion client
    private static ConcurrentMap<String, List<Client>> localRegionClients = new ConcurrentHashMap<String, List<Client>>();

    private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    /*private String localRegion;
    private String remoteRegion;
    private String currentRegion;

    private ConcurrentHashMap<String, String> regionPatternMappings = new ConcurrentHashMap<String, String>();

    // example: servicename_1.0.0 --> currentRegion1
    private static ConcurrentHashMap<String, String> serviceRegionMappings = new ConcurrentHashMap<String, String>();

    // example: 10.66 --> region1
    private ConcurrentHashMap<String, String> patternRegionMappings = new ConcurrentHashMap<String, String>();

    // example: 10.66.xx.yy --> true
    private ConcurrentHashMap<String, Boolean> regionHostHeartBeatStats = new ConcurrentHashMap<String, Boolean>();*/

    private RegionChangeListener() {}

    public static RegionChangeListener getInstance() {
        if (instance == null) {
            synchronized (RegionChangeListener.class) {
                if (instance == null) {
                    instance = new RegionChangeListener();
                }
            }
        }
        return instance;
    }
    
    /*private void switchRegion() {
        //TODO 判断条件 切换region
        currentRegion = remoteRegion;
    }*/

    public void setWorkingClients(Map<String, List<Client>> workingClients) {
        this.workingClients = workingClients;
    }

    public Map<String, List<Client>> getWorkingClients() {
        return workingClients;
    }



    @Override
    public void run() {
        //TODO 恢复检测线程
    }

    @Override
    public void addConnect(ConnectInfo cmd) {
        //TODO 趁机缓存localRegion的host

    }

    @Override
    public void removeConnect(Client client) {
        //TODO 如果是localRegion的host，添加到恢复检测线程的clientCache列表
    }

    @Override
    public void doNotUse(String serviceName, String host, int port) {
        //TODO 如果在恢复检测线程的clientCache列表中，删除他
    }

    public boolean isInCurrentRegion(HostInfo hostPort) {
        //TODO 返回是否属于当前region
        return false;
    }
}
