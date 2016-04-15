package com.dianping.pigeon.remoting.invoker.region;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.invoker.exception.RegionException;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.listener.ClusterListener;
import com.dianping.pigeon.remoting.invoker.route.region.Region;
import com.dianping.pigeon.threadpool.ThreadPool;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by chenchongze on 16/2/19.
 */
@Deprecated
public class RegionChangeListener implements Runnable, ClusterListener {

    public final static RegionChangeListener INSTANCE = new RegionChangeListener();

    private final Logger logger = LoggerLoader.getLogger(RegionChangeListener.class);

    private final static RegionManager regionManager = RegionManager.INSTANCE;

    private final static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private static long interval = configManager.getLongValue(Constants.KEY_HEARTBEAT_INTERVAL,
            Constants.DEFAULT_HEARTBEAT_INTERVAL);

    private static float regionSwitchRatio = configManager.getFloatValue("pigeon.regions.switchratio", 0.5f);

    private final static ThreadPool toRemoveHostsThreadPool = ClientManager.getRegisterThreadPool();

    private final static long CLOSE_PAUSE = 1000;

    private RegionChangeListener() {}

    @Override
    public void run() {
        long sleepTime = interval;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(sleepTime);
                //检查region路由开关
                if(!regionManager.isEnableRegionAutoSwitch()) { continue; }

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

                for (final String url : serviceGroupMap.keySet()) {
                    String groupValue = serviceGroupMap.get(url);
                    String group = groupValue.substring(0, groupValue.lastIndexOf("#"));
                    String vip = groupValue.substring(groupValue.lastIndexOf("#") + 1);
                    if (vip != null && vip.startsWith("console:")) {
                        continue;
                    }

                    final int priority = regionManager.getCurrentRegion(url).getPriority();
                    int toRemoveStartIndex = priority + 1;
                    final Region[] regionArray = regionManager.getRegionArray();

                    // 检查当前region的前置region
                    for(int i = 0; i < priority; ++i) {
                        int total = getRegionTotalClients(url, regionArray[i]);
                        int available = getRegionAvailableClients(url, regionArray[i]);

                        if(total > 0 && available >= regionSwitchRatio * total) {
                            //有恢复，切换，关闭后置连接，break
                            regionManager.switchRegion(url, regionArray[i]);
                            toRemoveStartIndex = i + 1;
                            logger.info("[region-switch] auto switch region to " + regionArray[i]);
                            // 关闭后置连接
                            HashSet<Region> toRemoveRegions = new HashSet<Region>();
                            Collections.addAll(toRemoveRegions, regionArray[i + 1], regionArray[priority]);

                            // 灰度慢关闭
                            toRemoveHostsThreadPool.submit(new InnerCloseClientsTask(url, toRemoveRegions));

                            break;
                        }
                    }

                    if(priority == regionManager.getCurrentRegion(url).getPriority()) {// 没有切换或切换失败，判断是否切换后置region
                        int total = getRegionTotalClients(url, regionArray[priority]);
                        int available = getRegionAvailableClients(url, regionArray[priority]);
                        if(total == 0 || available < regionSwitchRatio * total) {
                            final int candidate = priority + 1;
                            if(candidate < regionArray.length) {
                                regionManager.switchRegion(url, regionArray[candidate]);
                                toRemoveStartIndex = candidate + 1;
                                logger.info("[region-switch] auto switch region to " + regionArray[candidate]);

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
                        }
                    }

                    // 慢关闭后置的可能连接
                    if(toRemoveStartIndex < regionArray.length) {
                        HashSet<Region> toRemoveRegions = new HashSet<Region>();
                        for(int i=toRemoveStartIndex; i<regionArray.length; ++i) {
                            if( getRegionTotalClients(url, regionArray[i]) > 0 ) {
                                toRemoveRegions.add(regionArray[i]);
                            }
                        }

                        // 灰度慢关闭
                        toRemoveHostsThreadPool.submit(new InnerCloseClientsTask(url, toRemoveRegions));
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

    private class InnerCloseClientsTask implements Runnable {

        private final String url;
        private final HashSet<Region> toRemoveRegions;

        public InnerCloseClientsTask(String url, HashSet<Region> toRemoveRegions) {
            this.url = url;
            this.toRemoveRegions = toRemoveRegions;
        }

        @Override
        public void run() {
            final Set<HostInfo> hostInfos = getToRemoveHostInfos(url, toRemoveRegions);
            if(hostInfos != null) {
                for (final HostInfo hostInfo : hostInfos) {
                    try {
                        Thread.sleep(CLOSE_PAUSE);
                        RegistryEventListener.providerRemoved(url, hostInfo.getHost(), hostInfo.getPort());
                    } catch (Throwable t) {
                        logger.error("remove " + hostInfo.getConnect() + " for " + url +"error!", t);
                    }
                }
            }
        }
    }

    public Map<String, List<Client>> getWorkingClients() {
        return ClientManager.getInstance().getClusterListener().getServiceClients();
    }

    // 统计某服务指定region下当前可用的provider
    private int getRegionAvailableClients(String url, Region region) {

        List<Client> clientList = this.getWorkingClients().get(url);
        int available = 0;

        if (!CollectionUtils.isEmpty(clientList)) {
            for (Client client : clientList) {
                String address = client.getAddress();
                try {
                    if(regionManager.getRegion(address).equals(region)) {
                        int w = RegistryManager.getInstance().getServiceWeight(address);
                        boolean isAlive = regionManager.getRegionHostHeartBeatStats().containsKey(address)
                                ? regionManager.getRegionHostHeartBeatStats().get(address) : true;
                        if (w > 0 && client.isConnected() && client.isActive() && isAlive ) {
                            available += w;
                        }
                    }
                } catch (RegionException e) {
                    logger.error(e);
                }
            }
        }

        return available;
    }

    private int getRegionTotalClients(String url, Region region) {
        int total = 0;
        Map<String, Set<HostInfo>> serviceHostInfos = ClientManager.getInstance().getServiceHosts();

        if (serviceHostInfos.isEmpty()) {
            // never be here
            return total;
        }

        if(serviceHostInfos.containsKey(url)) {
            Set<HostInfo> hostInfoSet = serviceHostInfos.get(url);
            for(HostInfo hostInfo : hostInfoSet) {
                try {
                    if(regionManager.getRegion(hostInfo.getHost()).equals(region)) {
                        ++total;
                    }
                } catch (RegionException e) {
                    logger.error(e);
                }
            }
        }

        return total;
    }

    // 拿权重低的后置region的client切断
    private Set<HostInfo> getToRemoveHostInfos(String url, HashSet<Region> toRemoveRegions) {
        Set<HostInfo> result = new HashSet<HostInfo>();
        Map<String, Set<HostInfo>> serviceHostInfos = ClientManager.getInstance().getServiceHosts();

        if (serviceHostInfos.isEmpty()) {
            // never be here
            return null;
        }

        if(serviceHostInfos.containsKey(url)) {
            Set<HostInfo> hostInfoSet = serviceHostInfos.get(url);
            for(HostInfo hostInfo : hostInfoSet) {
                // 拿后置区域的host
                try {
                    Region region = regionManager.getRegion(hostInfo.getHost());
                    if(toRemoveRegions.contains(region)) {
                        result.add(hostInfo);
                    }
                } catch (RegionException e) {
                    logger.error(e);
                }
            }
        }

        return result;
    }

    @Override
    public void addConnect(ConnectInfo connectInfo) {
        // 建立region心跳缓存
        //regionManager.getRegionHostHeartBeatStats().put(connectInfo.getConnect(), true);
    }

    @Override
    public void removeConnect(Client client) {
        //regionManager.getRegionHostHeartBeatStats().put(client.getAddress(), false);
    }

    @Override
    public void doNotUse(String serviceName, String host, int port) {
    }

}
