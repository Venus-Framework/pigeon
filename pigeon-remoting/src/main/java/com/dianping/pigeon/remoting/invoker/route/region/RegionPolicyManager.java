package com.dianping.pigeon.remoting.invoker.route.region;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.exception.RegionException;
import com.dianping.pigeon.util.ServiceUtils;

import java.util.List;

/**
 * Created by chenchongze on 16/4/14.
 */
public class RegionPolicyManager {


    private void checkClientsNotNull(List<Client> clientList, InvokerConfig<?> invokerConfig) {
        if(clientList == null) {
            throw new RegionException("no available clientList in region policy for service[" + invokerConfig + "], env:"
                    + ConfigManagerLoader.getConfigManager().getEnv());
        }
    }

    /**
     * 注册RegionPolicy
     * @param serviceName
     * @param group
     * @param regionPolicy
     */
    public static void register(String serviceName, String group, Object regionPolicy) {
        String serviceId = ServiceUtils.getServiceId(serviceName, group);

    }
}
