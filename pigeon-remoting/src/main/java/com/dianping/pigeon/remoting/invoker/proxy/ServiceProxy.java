package com.dianping.pigeon.remoting.invoker.proxy;

import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenchongze on 15/12/16.
 */
public interface ServiceProxy {

    public void init();

    public <T> T getProxy(InvokerConfig<T> invokerConfig);

    public Map<InvokerConfig<?>, Object> getAllServiceInvokers();
}
