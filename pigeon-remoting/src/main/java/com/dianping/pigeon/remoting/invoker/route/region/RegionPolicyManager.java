package com.dianping.pigeon.remoting.invoker.route.region;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.exception.InvalidParameterException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.exception.RegionException;
import com.dianping.pigeon.util.ClassUtils;
import com.dianping.pigeon.util.ServiceUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenchongze on 16/4/14.
 */
public enum RegionPolicyManager {

    INSTANCE;

    private RegionPolicyManager () {
        register(AutoSwitchRegionPolicy.NAME, null, AutoSwitchRegionPolicy.INSTANCE);
        register(WeightBasedRegionPolicy.NAME, null, WeightBasedRegionPolicy.INSTANCE);
    }

    private final Logger logger = LoggerLoader.getLogger(this.getClass());

    public static final String DEFAULT_REGIONPOLICY = ConfigManagerLoader.getConfigManager().getStringValue(
            Constants.KEY_REGIONPOLICY, AutoSwitchRegionPolicy.NAME);

    private Map<String, RegionPolicy> regionPolicyMap = new ConcurrentHashMap<String, RegionPolicy>();

    private void checkClientsNotNull(List<Client> clientList, InvokerConfig<?> invokerConfig) {
        if(clientList == null) {
            throw new RegionException("no available clientList in region policy for service[" + invokerConfig + "], env:"
                    + ConfigManagerLoader.getConfigManager().getEnv());
        }
    }

    public List<Client> getPreferRegionClients(List<Client> clientList, InvokerConfig<?> invokerConfig) {
        RegionPolicy regionPolicy = getRegionPolicy(invokerConfig);

        if(regionPolicy == null) {
            regionPolicy = AutoSwitchRegionPolicy.INSTANCE;
        }

        clientList = regionPolicy.getPreferRegionClients(clientList, invokerConfig);
        checkClientsNotNull(clientList, invokerConfig);

        return clientList;
    }

    private RegionPolicy getRegionPolicy(InvokerConfig<?> invokerConfig) {
        String serviceId = ServiceUtils.getServiceId(invokerConfig.getUrl(), invokerConfig.getGroup());
        RegionPolicy regionPolicy = regionPolicyMap.get(serviceId);
        if (regionPolicy != null) {
            return regionPolicy;
        }
        regionPolicy = regionPolicyMap.get(invokerConfig.getRegionPolicy());
        if (regionPolicy != null) {
            return regionPolicy;
        }
        if (DEFAULT_REGIONPOLICY != null) {
            regionPolicy = regionPolicyMap.get(DEFAULT_REGIONPOLICY);
            if (regionPolicy != null) {
                regionPolicyMap.put(invokerConfig.getRegionPolicy(), regionPolicy);
                return regionPolicy;
            } else {
                logger.warn("the regionPolicy[" + DEFAULT_REGIONPOLICY + "] is invalid, only support "
                                + regionPolicyMap.keySet() + ".");
            }
        }
        return null;
    }

    /**
     * 注册RegionPolicy
     * @param serviceName
     * @param group
     * @param regionPolicy
     */
    @SuppressWarnings("unchecked")
    public void register(String serviceName, String group, Object regionPolicy) {
        String serviceId = ServiceUtils.getServiceId(serviceName, group);
        RegionPolicy regionPolicyObj = null;
        if(regionPolicy instanceof RegionPolicy) {
            regionPolicyObj = (RegionPolicy) regionPolicy;
        } else if (regionPolicy instanceof String && StringUtils.isNotBlank((String) regionPolicy)) {
            if (!regionPolicyMap.containsKey(regionPolicy)) {
                try {
                    Class<? extends RegionPolicy> regionPolicyClass = (Class<? extends RegionPolicy>) ClassUtils
                            .loadClass((String) regionPolicy);
                    regionPolicyObj = regionPolicyClass.newInstance();
                } catch (Throwable e) {
                    throw new InvalidParameterException("failed to register regionPolicy[service=" + serviceId
                            + ",class=" + regionPolicy + "]", e);
                }
            } else {
                regionPolicyObj = regionPolicyMap.get(regionPolicy);
            }
        } else if (regionPolicy instanceof Class) {
            try {
                Class<? extends RegionPolicy> regionPolicyClass = (Class<? extends RegionPolicy>) regionPolicy;
                regionPolicyObj = regionPolicyClass.newInstance();
            } catch (Throwable e) {
                throw new InvalidParameterException("failed to register regionPolicy[service=" + serviceId + ",class="
                        + regionPolicy + "]", e);
            }
        }
        if (regionPolicyObj != null) {
            regionPolicyMap.put(serviceId, regionPolicyObj);
        }
    }
}
