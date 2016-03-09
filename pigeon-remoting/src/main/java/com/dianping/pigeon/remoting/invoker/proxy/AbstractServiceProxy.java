package com.dianping.pigeon.remoting.invoker.proxy;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegionManager;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.InvokerBootStrap;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenchongze on 15/12/17.
 */
public abstract class AbstractServiceProxy implements ServiceProxy {

    protected static Map<InvokerConfig<?>, Object> services = new ConcurrentHashMap<InvokerConfig<?>, Object>();
    protected Logger logger = LoggerLoader.getLogger(this.getClass());

    private RegionManager regionManager = RegionManager.getInstance();


    @Override
    public void init() {

    }

    @Override
    public <T> T getProxy(InvokerConfig<T> invokerConfig) {
        if (invokerConfig.getServiceInterface() == null) {
            throw new IllegalArgumentException("service interface is required");
        }
        if (StringUtils.isBlank(invokerConfig.getUrl())) {
            invokerConfig.setUrl(ServiceFactory.getServiceUrl(invokerConfig));
        }
        if (!StringUtils.isBlank(invokerConfig.getProtocol())
                && !invokerConfig.getProtocol().equalsIgnoreCase(Constants.PROTOCOL_DEFAULT)) {
            String protocolPrefix = "@" + invokerConfig.getProtocol().toUpperCase() + "@";
            if (!invokerConfig.getUrl().startsWith(protocolPrefix)) {
                invokerConfig.setUrl(protocolPrefix + invokerConfig.getUrl());
            }
        }
        Object service = null;
        service = services.get(invokerConfig);
        if (service == null) {
            try {
                InvokerBootStrap.startup();
                service = SerializerFactory.getSerializer(invokerConfig.getSerialize()).proxyRequest(invokerConfig);
                if (StringUtils.isNotBlank(invokerConfig.getLoadbalance())) {
                    LoadBalanceManager.register(invokerConfig.getUrl(), invokerConfig.getGroup(),
                            invokerConfig.getLoadbalance());
                }
            } catch (Throwable t) {
                throw new RpcException("error while trying to get service:" + invokerConfig, t);
            }

            //TODO 考虑这里开始添加动态监控region的任务
            try {
                if(regionManager.isEnableRegionAutoSwitch()) {
                    regionManager.register(invokerConfig.getUrl());
                }
            } catch (Throwable t) {
                logger.warn("error while setup region manager: " + invokerConfig, t);
            }

            try {
                ClientManager.getInstance().registerClients(invokerConfig.getUrl(), invokerConfig.getGroup(),
                        invokerConfig.getVip());
            } catch (Throwable t) {
//				try {
//					ClientManager.getInstance().registerClients(invokerConfig.getUrl(),
//							invokerConfig.getGroup(), invokerConfig.getVip());
//				} catch (Throwable t2) {
//					logger.warn("error while trying to setup service client:" + invokerConfig, t2);
//				}
                logger.warn("error while trying to setup service client:" + invokerConfig, t);
            }
            services.put(invokerConfig, service);
        }
        return (T) service;
    }

    @Override
    public Map<InvokerConfig<?>, Object> getAllServiceInvokers() {
        return services;
    }
}
