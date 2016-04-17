package com.dianping.pigeon.remoting.invoker.route.region;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.exception.RegionException;
import com.dianping.pigeon.threadpool.DefaultThreadFactory;
import com.dianping.pigeon.util.ServiceUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chenchongze on 16/4/15.
 */
public class AutoSwitchRegionPolicy implements RegionPolicy {

    public final static AutoSwitchRegionPolicy INSTANCE = new AutoSwitchRegionPolicy();

    public static final String NAME = "autoSwitch";

    private final Logger logger = LoggerLoader.getLogger(this.getClass());

    private final RegionPolicyManager regionPolicyManager = RegionPolicyManager.INSTANCE;

    private final ClientManager clientManager = ClientManager.getInstance();

    private final RegistryManager registryManager = RegistryManager.getInstance();

    private Map<String, Set<HostInfo>> serviceHostInfos = clientManager.getServiceHosts();
    private Map<String, Client> allClients = clientManager.getClusterListener().getAllClients();
    private Region[] regionArray = regionPolicyManager.getRegionArray();

    private final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    //TODO 加入动态变化
    private float regionSwitchRatio = configManager.getFloatValue("pigeon.regions.switchratio", 0.5f);

    // serviceId --> regionHeartBeatList
    private ConcurrentMap<String, List<InnerRegionHeartBeatStat>> regionHeartBeatStats
            = new ConcurrentHashMap<String, List<InnerRegionHeartBeatStat>>();

    private AutoSwitchRegionPolicy() {
        try {
            ExecutorService regionAutoSwitchThreadPool = Executors.newFixedThreadPool(1,
                    new DefaultThreadFactory("Pigeon-Client-RegionAutoSwitch-ThreadPool"));
            regionAutoSwitchThreadPool.submit(new InnerCheckTask());
        } catch (Throwable t) {
            throw new RegionException("Init autoSwitchRegionTask failed!", t);
        }
    }

    @Override
    public List<Client> getPreferRegionClients(List<Client> clientList, InvokerConfig<?> invokerConfig) {
        //TODO 自动开关关闭时，返回用户指定region的所有clients
        return getRegionActiveClients(clientList);
    }

    private class InnerCheckTask implements Runnable {

        @Override
        public void run() {
            long sleepTime = 1000L;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(sleepTime);
                    //检查region路由开关
                    if(!regionPolicyManager.isEnableRegionPolicy()) { continue; }

                    //取出采用当前策略的services
                    Set<InvokerConfig<?>> services = ServiceFactory.getAllServiceInvokers().keySet();
                    Map<String, String> serviceGroupMap = new HashMap<String, String>();
                    for (InvokerConfig<?> invokerConfig : services) {
                        //TODO 待验证
                        if(!regionPolicyManager.getRegionPolicy(invokerConfig).equals(this)) {
                            continue;
                        }
                        String vip = "";
                        if (StringUtils.isNotBlank(invokerConfig.getVip())) {
                            vip = invokerConfig.getVip();
                        }
                        serviceGroupMap.put(invokerConfig.getUrl(), invokerConfig.getGroup() + "#" + vip);

                        getRegionHeartBeatStatWithCreate(ServiceUtils.getServiceId(invokerConfig.getUrl(),
                                invokerConfig.getGroup()));
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
                        final Region[] regionArray = regionPolicyManager.getRegionArray();

                        // 检查当前region的前置region
                        for(int i = 0; i < priority; ++i) {
                            int total = getRegionTotalClients(url, regionArray[i]);
                            int available = getRegionAvailableClients(url, regionArray[i]);

                            if(total > 0 && available >= regionSwitchRatio * total) {
                                //有恢复，切换，关闭后置连接，break
                                regionManager.switchRegion(url, regionArray[i]);
                                logger.info("[region-switch] auto switch region to " + regionArray[i]);

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
    }

    private List<Client> getRegionActiveClients(List<Client> clientList) {
        Map<Region, InnerRegionstat> regionstats = new HashMap<Region, InnerRegionstat>();
        for(Region region : regionArray) {
            regionstats.put(region, new InnerRegionstat());
        }

        for(Client client : clientList) {
            try {
                InnerRegionstat regionstat = regionstats.get(client.getRegion());
                regionstat.addClient(client);
                regionstat.addTotal();
                if(client.isActive() && registryManager.getServiceWeightFromCache(client.getAddress()) > 0) {
                    regionstat.addActive();
                }
            } catch (Throwable t) {
                logger.error(t);
            }
        }

        for (Region aRegionArray : regionArray) {// 优先级大小按数组大小排列
            try {
                InnerRegionstat regionstat = regionstats.get(aRegionArray);
                if (regionstat.getTotal() > 0 && regionstat.getActive() >= regionSwitchRatio * regionstat.getTotal()) {
                    return regionstat.getClientList();
                }
            } catch (Throwable t) {
                logger.error(t);
            }
        }

        return clientList;
    }

    private boolean isRegionActive(String url, Region region) {
        int total = 0;
        int active = 0;
        if(!serviceHostInfos.isEmpty() & serviceHostInfos.containsKey(url)) {
            Set<HostInfo> hostInfoSet = serviceHostInfos.get(url);
            for(HostInfo hostInfo : hostInfoSet) {
                try {
                    Client client = allClients.get(hostInfo.getConnect());
                    if(client.getRegion().equals(region)) {
                        ++total;
                    }
                    if(client.isActive() && hostInfo.getWeight() > 0) {
                        ++active;
                    }
                } catch (Throwable t) {
                    logger.error(t);
                }
            }
        }
        return total > 0 && active >= regionSwitchRatio * total;
    }

    private List<InnerRegionHeartBeatStat> getRegionHeartBeatStatWithCreate(String serviceId) {
        List<InnerRegionHeartBeatStat> heartBeatStatList = regionHeartBeatStats.get(serviceId);
        if (heartBeatStatList == null) {
            List<InnerRegionHeartBeatStat> _heartBeatStatList = new ArrayList<InnerRegionHeartBeatStat>();
            heartBeatStatList = regionHeartBeatStats.putIfAbsent(serviceId, _heartBeatStatList);
            if (heartBeatStatList == null) {
                heartBeatStatList = _heartBeatStatList;
            }
        }
        return heartBeatStatList;
    }

    private class InnerRegionHeartBeatStat {
        private final String name;
        private final int priority;
        private boolean active = true;
        private float liveRate = 1f;

        public InnerRegionHeartBeatStat(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }


    }

    private class InnerRegionstat {
        private int active = 0;
        private int total = 0;
        private List<Client> clientList = new ArrayList<Client>();

        public List<Client> getClientList() {
            return clientList;
        }

        public void addClient(Client client) {
            clientList.add(client);
        }

        public int getActive() {
            return active;
        }

        public void addActive() {
            ++active;
        }

        public int getTotal() {
            return total;
        }

        public void addTotal() {
            ++total;
        }
    }

}
