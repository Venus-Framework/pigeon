package com.dianping.pigeon.remoting.invoker.proxy;

import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.util.ClassUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Created by chenchongze on 16/9/27.
 */
public class MockInvocationUtils {

    public static Object getProxy(InvokerConfig invokerConfig, InvocationHandler proxyObject) {
        return Proxy.newProxyInstance(ClassUtils.getCurrentClassLoader(invokerConfig.getClassLoader()),
                new Class[] { invokerConfig.getServiceInterface() }, proxyObject);
    }
}
