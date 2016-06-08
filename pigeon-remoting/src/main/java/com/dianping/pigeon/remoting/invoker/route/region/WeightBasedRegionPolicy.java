package com.dianping.pigeon.remoting.invoker.route.region;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.Client;
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
    public List<Client> getPreferRegionClients(List<Client> clientList, InvocationRequest request) {
        return getRegionActiveClients(clientList);
    }

    private List<Client> getRegionActiveClients(List<Client> clientList) {
        // 分region存储clients
        Map<Region, List<Client>> regionClients = new HashMap<Region, List<Client>>();
        for(Region region : regionPolicyManager.getRegionArray()) {
            regionClients.put(region, new ArrayList<Client>());
        }

        for(Client client : clientList) {
            if(regionClients.containsKey(client.getRegion())) {
                regionClients.get(client.getRegion()).add(client);
            }
        }

        // 初始化region中存在可用client的权重和
        Integer weightSum = 0;
        Set<Region> regionSet = new HashSet<Region>();
        for(Region region : regionClients.keySet()) {
            if(regionClients.get(region).size() > 0) {
                weightSum += region.getWeight();
                regionSet.add(region);
            }
        }

        if (weightSum <= 0) {
            throw new RegionException("Error: weightSum=" + weightSum.toString());
        }

        // 权重随机算法
        Integer n = random.nextInt(weightSum); // n in [0, weightSum)
        Integer m = 0;
        for (Region region : regionSet) {
            int weight = region.getWeight();
            if (m <= n && n < m + weight) {
                return regionClients.get(region);
            }
            m += weight;
        }

        return null;
    }

}
