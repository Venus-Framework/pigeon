package com.dianping.pigeon.remoting.invoker.route.region;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.route.quality.RequestQualityManager;
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

    private final RequestQualityManager requestQualityManager = RequestQualityManager.INSTANCE;

    private final ClientManager clientManager = ClientManager.getInstance();

    private final RegistryManager registryManager = RegistryManager.getInstance();

    private Map<String, Set<HostInfo>> serviceHostInfos = clientManager.getServiceHosts();
    private Map<String, Client> allClients = clientManager.getClusterListener().getAllClients();
    private final ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private AutoSwitchRegionPolicy() {}

    @Override
    public List<Client> getPreferRegionClients(List<Client> clientList, InvocationRequest request) {
        return getRegionActiveClients(clientList, request);
    }

    private List<Client> getRegionActiveClients(List<Client> clientList, InvocationRequest request) {
        Map<Region, InnerRegionStat> regionStats = new HashMap<Region, InnerRegionStat>();
        for(Region region : regionPolicyManager.getRegionArray()) {
            regionStats.put(region, new InnerRegionStat());
        }

        for(Client client : clientList) {
            try {
                InnerRegionStat regionStat = regionStats.get(client.getRegion());
                regionStat.addTotal();
                if(client.isActive() && registryManager.getServiceWeightFromCache(client.getAddress()) > 0) {
                    regionStat.addActive();
                    regionStat.addClient(client);
                }
            } catch (Throwable t) {
                logger.error(t);
            }
        }

        for (Region region : regionPolicyManager.getRegionArray()) {// 优先级大小按数组大小排列
            try {
                InnerRegionStat regionStat = regionStats.get(region);
                int total = regionStat.getTotal();
                int active = regionStat.getActive();
                List<Client> regionClientList = regionStat.getClientList();
                float least = configManager.getFloatValue("pigeon.regions.switchratio", 0.5f) * total;

                if (total > 0 && active > 0 && active >= least) {
                    if (requestQualityManager.isEnableRequestQualityRoute()) {
                        List<Client> filterClients = requestQualityManager.getQualityPreferClients(
                                regionClientList, request, least);
                        if (filterClients.size() >= least) {
                            return filterClients;
                        }
                    } else {
                        return regionClientList;
                    }
                }

            } catch (Throwable t) {
                logger.error(t);
            }
        }

        return clientList;
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
