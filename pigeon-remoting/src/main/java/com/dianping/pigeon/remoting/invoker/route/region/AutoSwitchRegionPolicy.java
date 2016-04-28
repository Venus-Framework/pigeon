package com.dianping.pigeon.remoting.invoker.route.region;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import org.apache.logging.log4j.Logger;

import java.util.*;

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
    private final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    //TODO 加入动态变化
    private float regionSwitchRatio = configManager.getFloatValue("pigeon.regions.switchratio", 0.5f);

    private AutoSwitchRegionPolicy() {}

    @Override
    public List<Client> getPreferRegionClients(List<Client> clientList, InvokerConfig<?> invokerConfig) {
        //TODO 自动开关关闭时，返回用户指定region的所有clients
        return getRegionActiveClients(clientList);
    }

    private List<Client> getRegionActiveClients(List<Client> clientList) {
        Map<Region, InnerRegionStat> regionStats = new HashMap<Region, InnerRegionStat>();
        for(Region region : regionPolicyManager.getRegionArray()) {
            regionStats.put(region, new InnerRegionStat());
        }

        for(Client client : clientList) {
            try {
                InnerRegionStat regionStat = regionStats.get(client.getRegion());
                regionStat.addClient(client);
                regionStat.addTotal();
                if(client.isActive() && registryManager.getServiceWeightFromCache(client.getAddress()) > 0) {
                    regionStat.addActive();
                }
            } catch (Throwable t) {
                logger.error(t);
            }
        }

        for (Region region : regionPolicyManager.getRegionArray()) {// 优先级大小按数组大小排列
            try {
                InnerRegionStat regionStat = regionStats.get(region);
                if (regionStat.getTotal() > 0 && regionStat.getActive() >= regionSwitchRatio * regionStat.getTotal()) {
                    return regionStat.getClientList();
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

    private class InnerRegionStat {
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
