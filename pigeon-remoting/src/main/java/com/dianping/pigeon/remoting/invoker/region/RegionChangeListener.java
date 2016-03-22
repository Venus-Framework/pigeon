package com.dianping.pigeon.remoting.invoker.region;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.region.Region;
import com.dianping.pigeon.registry.region.RegionManager;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.listener.ClusterListener;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by chenchongze on 16/2/19.
 */
public class RegionChangeListener implements Runnable, ClusterListener {

    private final Logger logger = LoggerLoader.getLogger(RegionChangeListener.class);

    private final static RegionManager regionManager = RegionManager.getInstance();

    private static volatile RegionChangeListener instance;

    private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private static long interval = configManager.getLongValue(Constants.KEY_HEARTBEAT_INTERVAL,
            Constants.DEFAULT_HEARTBEAT_INTERVAL);

    private static float regionSwitchRatio = configManager.getFloatValue("pigeon.regions.switchratio", 0.5f);

    private RegionChangeListener() {
    }

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
    
    private void switchRegion(String serviceName, Region region) {
        regionManager.getServiceCurrentRegionMappings().put(serviceName, region);
    }

    public Map<String, List<Client>> getWorkingClients() {
        return ClientManager.getInstance().getClusterListener().getServiceClients();
    }

    @Override
    public void run() {
        //TODO 恢复检测线程
        long sleepTime = interval;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(sleepTime);

                Set<InvokerConfig<?>> services = ServiceFactory.getAllServiceInvokers().keySet();
                Map<String, String> serviceGroupMap = new HashMap<String, String>();
                for (InvokerConfig<?> invokerConfig : services) {
                    String vip = "";
                    if (StringUtils.isNotBlank(invokerConfig.getVip())) {
                        vip = invokerConfig.getVip();
                    }
                    serviceGroupMap.put(invokerConfig.getUrl(), invokerConfig.getGroup() + "#" + vip);
                }

                long now = System.currentTimeMillis();

                for (String url : serviceGroupMap.keySet()) {
                    String groupValue = serviceGroupMap.get(url);
                    String group = groupValue.substring(0, groupValue.lastIndexOf("#"));
                    String vip = groupValue.substring(groupValue.lastIndexOf("#") + 1);
                    if (vip != null && vip.startsWith("console:")) {
                        continue;
                    }

                    //TODO ①local时
                    if (regionManager.getLocalRegion().equals(regionManager.getServiceCurrentRegionMappings().get(url))) {
                        int available = getAvailableLocalClients(this.getWorkingClients().get(url));
                        int total = getTotalLocalRegionClients(url);
                        if (available < regionSwitchRatio * total) {
                            //TODO 切换为remote region，连接remote service，保留local service的连接
                            switchRegion(url, regionManager.getNotLocalRegion());

                            logger.info("[region-switch] auto switch region to " + regionManager.getNotLocalRegion());

                            String error = null;
                            try {
                                ClientManager.getInstance().registerClients(url, group, vip);
                            } catch (Throwable e) {
                                error = e.getMessage();
                            }
                            if (error != null) {
                                logger.warn("[provider-available] failed to get providers, caused by:" + error);
                            }

                        }
                    } else if(regionManager.getNotLocalRegion().equals(regionManager.getServiceCurrentRegionMappings().get(url))) { //TODO ②非local时
                        int available = getAvailableLocalClients(this.getWorkingClients().get(url));
                        int total = getTotalLocalRegionClients(url);
                        if (available >= regionSwitchRatio * total) {
                            //TODO 切换为local region，关闭remote service的连接
                            switchRegion(url, regionManager.getLocalRegion());

                            logger.info("[region-switch] auto switch region to " + regionManager.getLocalRegion());

                            for(HostInfo hostInfo : getRemoteHostInfos(url)) {
                                RegistryEventListener.providerRemoved(url, hostInfo.getHost(), hostInfo.getPort());
                            }

                        }
                    }

                }

                sleepTime = interval - (System.currentTimeMillis() - now);
            } catch (Throwable e) {
                logger.warn("[region_change] task failed:", e);
            } finally {
                if (sleepTime < 1000) {
                    sleepTime = 1000;
                }
            }
        }
    }

    private Set<HostInfo> getRemoteHostInfos(String url) {
        Set<HostInfo> result = new HashSet<HostInfo>();
        Map<String, Set<HostInfo>> serviceHostInfos = ClientManager.getInstance().getServiceHosts();

        if (serviceHostInfos.isEmpty()) {
            // never be here
            return null;
        }

        if(serviceHostInfos.containsKey(url)) {
            Set<HostInfo> hostInfoSet = serviceHostInfos.get(url);
            for(HostInfo hostInfo : hostInfoSet) {
                if(!regionManager.isInLocalRegion(hostInfo.getHost())) {
                    result.add(hostInfo);
                }
            }
        }
        return result;
    }

    private int getTotalLocalRegionClients(String url) {
        int total = 0;
        Map<String, Set<HostInfo>> serviceHostInfos = ClientManager.getInstance().getServiceHosts();

        if (serviceHostInfos.isEmpty()) {
            // never be here
            return total;
        }

        if(serviceHostInfos.containsKey(url)) {
            Set<HostInfo> hostInfoSet = serviceHostInfos.get(url);
            for(HostInfo hostInfo : hostInfoSet) {
                if(regionManager.isInLocalRegion(hostInfo.getHost())) {
                    ++total;
                }
            }
        }

        return total;
    }

    @Override
    public void addConnect(ConnectInfo connectInfo) {
        //废弃
        //TODO 趁机缓存localRegion的host
    }

    @Override
    public void removeConnect(Client client) {
        //废弃
        //TODO 如果是localRegion的host，添加到恢复检测线程的clientCache列表
    }

    @Override
    public void doNotUse(String serviceName, String host, int port) {
        //废弃
        //TODO 删除缓存的localRegion的host
    }

    // TODO 统计local region下当前可用的provider
    private int getAvailableLocalClients(List<Client> clientList) {
        int available = 0;
        if (CollectionUtils.isEmpty(clientList)) {
            available = 0;
        } else {
            for (Client client : clientList) {
                String address = client.getAddress();
                int w = RegistryManager.getInstance().getServiceWeight(address);
                boolean isAlive = regionManager.getRegionHostHeartBeatStats().containsKey(address)
                        ? regionManager.getRegionHostHeartBeatStats().get(address) : true;
                if (w > 0 && client.isConnected() && client.isActive()
                        && isAlive && regionManager.isInLocalRegion(address)) {
                    available += w;
                }
            }
        }
        return available;
    }

    class RegionHeartBeatCache {

        private String address;
        private boolean isAlive = true;

        public RegionHeartBeatCache(String address) {
            this.address = address;
        }

    }

}
