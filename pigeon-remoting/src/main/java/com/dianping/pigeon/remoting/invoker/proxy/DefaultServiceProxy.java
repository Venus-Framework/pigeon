package com.dianping.pigeon.remoting.invoker.proxy;

import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;

/**
 * Created by chenchongze on 15/12/16.
 */
public class DefaultServiceProxy extends AbstractServiceProxy implements ServiceProxy {

    @Override
    public <T> T getProxy(InvokerConfig<T> invokerConfig) {
        return super.getProxy(invokerConfig);
    }
}
