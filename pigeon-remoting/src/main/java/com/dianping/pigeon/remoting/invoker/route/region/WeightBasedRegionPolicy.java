package com.dianping.pigeon.remoting.invoker.route.region;

import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.exception.RegionException;

import java.util.*;

/**
 * Created by chenchongze on 16/4/15.
 */
public class WeightBasedRegionPolicy implements RegionPolicy {

    public final static WeightBasedRegionPolicy INSTANCE = new WeightBasedRegionPolicy();

    private WeightBasedRegionPolicy(){}

    public static final String NAME = "weightBased";

    private final RegionPolicyManager regionPolicyManager = RegionPolicyManager.INSTANCE;

    private Random random = new Random();

    @Override
    public List<Client> getPreferRegionClients(List<Client> clientList, InvokerConfig<?> invokerConfig) {

        return getRegionActiveClients(clientList);
    }

    private List<Client> getRegionActiveClients(List<Client> clientList) {
        Region region = getActiveRegionByWeight();

        if(region != null) {
            List<Client> _clientList = new ArrayList<Client>();
            for(Client client : clientList) {
                if(client.getRegion().equals(region)){
                    _clientList.add(client);
                }
            }
            return _clientList;
        }

        return clientList;
    }

    private class InnerRegionWeight {
        private int weight = 1;

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }
    }

    private Region getActiveRegionByWeight() {
        //TODO 从配置读取相应的权重，更新regionWeights，ps：权重为0时，即不选择该region
        Map<Region, InnerRegionWeight> regionWeights = new HashMap<Region, InnerRegionWeight>();
        for(Region region : regionPolicyManager.getRegionArray()) {
            regionWeights.put(region, new InnerRegionWeight());
        }

        Integer weightSum = 0;
        for (Region region : regionWeights.keySet()) {
            weightSum += regionWeights.get(region).getWeight();
        }

        if (weightSum <= 0) {
            throw new RegionException("Error: weightSum=" + weightSum.toString());
        }

        Integer n = random.nextInt(weightSum); // n in [0, weightSum)
        Integer m = 0;
        for (Region region : regionWeights.keySet()) {
            int weight = regionWeights.get(region).getWeight();
            if (m <= n && n < m + weight) {
                return region;
            }
            m += weight;
        }

        return null;
    }
}
